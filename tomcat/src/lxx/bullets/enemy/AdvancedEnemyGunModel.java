/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobot;
import lxx.RobotListener;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.office.Office;
import lxx.targeting.GunType;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.*;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import robocode.Event;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.StrictMath.signum;

public class AdvancedEnemyGunModel implements BulletManagerListener, WaveCallback, RobotListener {

    public static final int FIRE_DETECTION_LATENCY = 2;

    private static final Map<String, LogSet> logSets = new HashMap<String, LogSet>();

    private final Map<LXXBullet, PSTreeEntry<UndirectedGuessFactor>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<UndirectedGuessFactor>>();
    private final Map<Wave, LXXBullet> bulletsByWaves = new HashMap<Wave, LXXBullet>();

    private final TurnSnapshotsLog turnSnapshotsLog;
    private final Office office;

    public AdvancedEnemyGunModel(TurnSnapshotsLog turnSnapshotsLog, Office office) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.office = office;
        office.getRobot().addListener(this);
    }

    public EnemyBulletPredictionData getPredictionData(LXXRobot t, final TurnSnapshot turnSnapshot, Collection<BulletShadow> bulletShadows) {
        return getLogSet(t.getName()).getPredictionData(turnSnapshot, t, bulletShadows);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(turnSnapshotsLog.getLastSnapshot(bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
        final Wave wave = bullet.getWave();
        office.getWaveManager().addCallback(this, wave);
        bulletsByWaves.put(wave, bullet);
    }

    public void bulletHit(LXXBullet bullet) {
        final LogSet logSet = getLogSet(bullet.getOwner().getName());

        final AEGMPredictionData aimPredictionData = (AEGMPredictionData) bullet.getAimPredictionData();
        updateEnemyHitRate(logSet, aimPredictionData, true);

        logSet.learn(bullet, entriesByBullets.get(bullet).predicate, true);
        if (office.getStatisticsManager().getEnemyHitRate().getHitCount() == 4) {
            logSet.shortLogs.addAll(logSet.visitLogsSet);
            logSet.midLogs.addAll(logSet.visitLogsSet);
            logSet.longLogs.addAll(logSet.visitLogsSet);
            logSet.enemyHitRateLogs.addAll(logSet.visitLogsSet);
        }
    }

    private void updateEnemyHitRate(LogSet logSet, AEGMPredictionData aimPredictionData, boolean isHit) {
        for (Log log : logSet.hitLogsSet) {
            if (aimPredictionData.getBearingOffsets(log) != null) {
                if (isHit) {
                    log.enemyHitRate.hit();
                } else {
                    log.enemyHitRate.miss();
                }
            }
        }
        for (Log log : logSet.visitLogsSet) {
            if (aimPredictionData.getBearingOffsets(log) != null) {
                if (isHit) {
                    log.enemyHitRate.hit();
                } else {
                    log.enemyHitRate.miss();
                }
            }
        }
    }

    public void bulletIntercepted(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate, true);
    }

    public void bulletMiss(LXXBullet bullet) {
        final LogSet logSet = getLogSet(bullet.getOwner().getName());
        final AEGMPredictionData aimPredictionData = (AEGMPredictionData) bullet.getAimPredictionData();
        updateEnemyHitRate(logSet, aimPredictionData, false);
        logSet.learn(bullet, entriesByBullets.get(bullet).predicate, false);
    }

    private LogSet getLogSet(String enemyName) {
        LogSet logSet = logSets.get(enemyName);
        if (logSet == null) {
            logSet = createLogSet();
            logSets.put(enemyName, logSet);
        }
        return logSet;
    }

    public void waveBroken(Wave w) {
        final LXXBullet bullet = bulletsByWaves.remove(w);
        final PSTreeEntry<UndirectedGuessFactor> entry = entriesByBullets.get(bullet);
        final double direction = bullet.getTargetLateralDirection();
        double undirectedGuessFactor = w.getHitBearingOffsetInterval().center() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
        getLogSet(bullet.getOwner().getName()).learn(entry);
    }

    public void wavePassing(Wave w) {
    }

    public void bulletPassing(LXXBullet bullet) {
    }

    @Override
    public void onEvent(Event event) {
        /*if (event instanceof TickEvent) {
            for (LXXBullet bullet : bulletsByWaves.values()) {
                final LogSet logSet = getLogSet(bullet.getOwner().getName());
                int totalLogsToCalculate = logSet.hitLogsSet.size() + logSet.hitLogsSet.size();
                final AEGMPredictionData aimPredictionData = (AEGMPredictionData) bullet.getAimPredictionData();
                int logsToCalculate = (int) ((totalLogsToCalculate - aimPredictionData.getLogsCount()) / bullet.getFlightTime(office.getRobot()));
                int calculatedLogs = 0;
                List<Log> allLogs = new ArrayList<Log>(logSet.hitLogsSet);
                allLogs.addAll(logSet.visitLogsSet);
                for (Log log : allLogs) {
                    if (aimPredictionData.getBearingOffsets(log) != null) {
                        continue;
                    }
                    List<PastBearingOffset> bearingOffsets = log.getBearingOffsets(aimPredictionData.getTs(), bullet.getBullet().getPower(), bullet.getBulletShadows(), aimPredictionData.getPredictionRoundTime());
                    aimPredictionData.addBearingOffsets(log, bearingOffsets);
                    if (calculatedLogs++ == logsToCalculate) {
                        break;
                    }
                }
            }
        }*/
    }

    class Log {

        private PSTree<UndirectedGuessFactor> log;
        private Map<Attribute, Double> halfSideLength = LXXUtils.toMap(
                AttributesManager.myLateralSpeed, 2D,
                AttributesManager.myAcceleration, 0D,
                AttributesManager.distBetween, 75D,
                AttributesManager.myDistToForwardWall, 50D,
                AttributesManager.myDistLast10Ticks, 20D);

        private final AvgValue shortAvgHitRate = new AvgValue(3);
        private final AvgValue midAvgHitRate = new AvgValue(11);
        private final AvgValue longAvgHitRate = new AvgValue(100);

        private final AvgValue shortAvgMissRate = new AvgValue(3);
        private final AvgValue midAvgMissRate = new AvgValue(11);
        private final AvgValue longAvgMissRate = new AvgValue(100);

        private final HitRate enemyHitRate = new HitRate();

        private Attribute[] attrs;
        public int usage = 0;

        private Log(Attribute[] attrs) {
            this.attrs = attrs;
            this.log = new PSTree<UndirectedGuessFactor>(this.attrs, 2, 0.0001);
        }

        private List<PastBearingOffset> getBearingOffsets(TurnSnapshot predicate, double firePower, Collection<BulletShadow> bulletShadows, long roundTimeLimit) {
            final PSTreeEntry<UndirectedGuessFactor>[] entries = log.getSimilarEntries(getLimits(predicate));

            final double lateralDirection = LXXUtils.lateralDirection(predicate.getEnemyImage(), predicate.getMeImage());
            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);

            final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
            int i = 0;
            for (PSTreeEntry<UndirectedGuessFactor> entry : entries) {
                if (entry.predicate.roundTime > roundTimeLimit) {
                    continue;
                }
                if (i++ == 5) {
                    break;
                }
                if (entry.result.lateralDirection != 0 && lateralDirection != 0) {

                    final double bearingOffset = entry.result.guessFactor * entry.result.lateralDirection * lateralDirection * maxEscapeAngleQuick;
                    if (isShadowed(bearingOffset, bulletShadows)) {
                        i--;
                        continue;
                    }
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, bearingOffset, 1));
                } else {
                    final double bearingOffset1 = entry.result.guessFactor * maxEscapeAngleQuick;
                    if (isShadowed(bearingOffset1, bulletShadows)) {
                        i--;
                        continue;
                    }
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, bearingOffset1, 1));

                    final double bearingOffset2 = -entry.result.guessFactor * maxEscapeAngleQuick;
                    if (isShadowed(bearingOffset2, bulletShadows)) {
                        i--;
                        continue;
                    }
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, bearingOffset2, 1));
                }
            }

            return bearingOffsets;
        }

        private boolean isShadowed(double bearingOffset, Collection<BulletShadow> shadows) {
            for (BulletShadow shadow : shadows) {
                if (shadow.contains(bearingOffset)) {
                    return true;
                }
            }

            return false;
        }

        private Interval[] getLimits(TurnSnapshot center) {
            final Interval[] res = new Interval[AttributesManager.attributesCount()];
            for (Attribute attr : attrs) {
                double delta = halfSideLength.get(attr);
                res[attr.id] = new Interval((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                        (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta)));
            }

            return res;
        }

    }

    private class LogSet {

        private final List<Log> hitLogsSet = new ArrayList<Log>();
        private final List<Log> visitLogsSet = new ArrayList<Log>();

        private final List<Log> shortLogs = new ArrayList<Log>();
        private final List<Log> midLogs = new ArrayList<Log>();
        private final List<Log> longLogs = new ArrayList<Log>();
        private final List<Log> enemyHitRateLogs = new ArrayList<Log>();
        private final List[] bestLogs = {shortLogs, midLogs, longLogs, enemyHitRateLogs};

        public void learn(PSTreeEntry<UndirectedGuessFactor> entry) {
            for (Log log : visitLogsSet) {
                log.log.addEntry(entry);
            }
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, LXXRobot t, Collection<BulletShadow> bulletShadows) {
            final List<PastBearingOffset> bearingOffsets = new ArrayList<PastBearingOffset>();

            final Map<Log, List<PastBearingOffset>> bestLogsBearingOffsets = new HashMap<Log, List<PastBearingOffset>>();
            final long roundTime = LXXUtils.getRoundTime(t.getTime(), t.getRound());
            for (List<Log> logs : bestLogs) {
                int i = 0;
                for (Log log : logs) {
                    if (i++ == 3) {
                        break;
                    }
                    if (bestLogsBearingOffsets.containsKey(log)) {
                        continue;
                    }
                    final List<PastBearingOffset> logBOs = log.getBearingOffsets(ts, t.getFirePower(), bulletShadows, roundTime);
                    bestLogsBearingOffsets.put(log, logBOs);
                    bearingOffsets.addAll(logBOs);
                    log.usage++;
                }
            }

            if (bearingOffsets.size() == 0) {
                final GunType enemyGunType = office.getTomcatEyes().getEnemyGunType(t);
                fillWithSimpleBOs(ts, t, bearingOffsets, enemyGunType);
            }

            return new AEGMPredictionData(bearingOffsets, roundTime, bestLogsBearingOffsets, ts);
        }

        private void updateBestLogs() {
            Collections.sort(shortLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.shortAvgHitRate.getCurrentValue() - o2.shortAvgMissRate.getCurrentValue()) -
                            (o1.shortAvgHitRate.getCurrentValue() - o1.shortAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(midLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.midAvgHitRate.getCurrentValue() - o2.midAvgMissRate.getCurrentValue()) -
                            (o1.midAvgHitRate.getCurrentValue() - o1.midAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(longLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.longAvgHitRate.getCurrentValue() - o2.longAvgMissRate.getCurrentValue()) -
                            (o1.longAvgHitRate.getCurrentValue() - o1.longAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(enemyHitRateLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    if (o1.enemyHitRate.getFireCount() == 0) {
                        return 1;
                    } else if (o2.enemyHitRate.getFireCount() == 0) {
                        return -1;
                    }
                    return (int) signum(o1.enemyHitRate.getHitRate() - o2.enemyHitRate.getHitRate());
                }
            });
        }

        private void fillWithSimpleBOs(TurnSnapshot ts, LXXRobot t, List<PastBearingOffset> bearingOffsets, GunType enemyGunType) {
            final double lateralDirection = LXXUtils.lateralDirection(ts.getEnemyImage(), ts.getMeImage());
            final double bulletSpeed = Rules.getBulletSpeed(t.getFirePower());
            final double maxEscapeAngleAcc = LXXUtils.getMaxEscapeAngle(t, office.getRobot().getState(), bulletSpeed);
            if (enemyGunType != GunType.HEAD_ON) {
                if (lateralDirection != 0) {
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * lateralDirection, 1));
                } else {
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * 1, 1));
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * -1, 1));
                }
            }
            if (enemyGunType == GunType.UNKNOWN || enemyGunType == GunType.HEAD_ON) {
                bearingOffsets.add(new PastBearingOffset(ts, 0D, 1));
            }
        }

        public void learn(LXXBullet bullet, TurnSnapshot predicate, boolean isHit) {
            recalculateLogSetEfficiency(bullet, visitLogsSet, isHit);
            recalculateLogSetEfficiency(bullet, hitLogsSet, isHit);
            updateBestLogs();
            if (isHit) {
                final double direction = bullet.getTargetLateralDirection();
                final double undirectedGuessFactor = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
                final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(predicate);
                entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
                for (Log log : hitLogsSet) {
                    log.log.addEntry(entry);
                }
            }
        }

        private void recalculateLogSetEfficiency(LXXBullet bullet, List<Log> logSet, boolean isHit) {
            for (Log log : logSet) {
                final AEGMPredictionData ebpd = (AEGMPredictionData) bullet.getAimPredictionData();
                List<PastBearingOffset> bearingOffsets = ebpd.getBearingOffsets(log);
                if (bearingOffsets == null) {
                    bearingOffsets = log.getBearingOffsets(ebpd.getTs(), bullet.getBullet().getPower(), bullet.getBulletShadows(), ebpd.getPredictionRoundTime());
                }
                double logEfficiency = calculateEfficiency(bullet, bearingOffsets, isHit);
                if (isHit) {
                    log.shortAvgHitRate.addValue(logEfficiency);
                    log.midAvgHitRate.addValue(logEfficiency);
                    log.longAvgHitRate.addValue(logEfficiency);
                } else {
                    log.shortAvgMissRate.addValue(logEfficiency);
                    log.midAvgMissRate.addValue(logEfficiency);
                    log.longAvgMissRate.addValue(logEfficiency);
                }
            }
        }

        private double calculateEfficiency(LXXBullet bullet, List<PastBearingOffset> bearingOffsets, boolean isHit) {
            final IntervalDouble effectiveInterval;
            if (isHit) {
                final double robotHalfSizeRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), bullet.getTarget()) / 2;
                final double currentBO = bullet.getRealBearingOffsetRadians();
                effectiveInterval = new IntervalDouble(currentBO - robotHalfSizeRadians, currentBO + robotHalfSizeRadians);
            } else {
                final IntervalDouble hitInterval = bullet.getWave().getHitBearingOffsetInterval();
                effectiveInterval = new IntervalDouble(hitInterval.center() - hitInterval.getLength() * 0.4,
                        hitInterval.center() + hitInterval.getLength() * 0.4);
            }

            double totalDanger = 0;
            double realDanger = 0;
            for (PastBearingOffset pastBo : bearingOffsets) {
                totalDanger += pastBo.danger;
                if (effectiveInterval.contains(pastBo.bearingOffset)) {
                    realDanger += pastBo.danger;
                }
            }

            if (totalDanger == 0) {
                return 0;
            }

            return realDanger / totalDanger;
        }

    }

    private LogSet createLogSet() {
        final LogSet res = new LogSet();

        res.visitLogsSet.addAll(createVisitLogs());
        res.hitLogsSet.addAll(createHitLogs());

        res.shortLogs.addAll(res.hitLogsSet);
        res.midLogs.addAll(res.hitLogsSet);
        res.longLogs.addAll(res.hitLogsSet);
        res.enemyHitRateLogs.addAll(res.hitLogsSet);

        res.updateBestLogs();

        return res;
    }

    private List<Log> createVisitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myLateralSpeed,
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
        };

        return createLogs(possibleAttributes, new Attribute[0], 1, Integer.MAX_VALUE);
    }

    private List<Log> createHitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
                AttributesManager.myDistLast10Ticks,
        };
        return createLogs(possibleAttributes, new Attribute[]{AttributesManager.myLateralSpeed}, 1, Integer.MAX_VALUE);
    }

    private List<Log> createLogs(Attribute[] possibleAttributes, Attribute[] requiredAttributes,
                                 int minElements, int maxElements) {
        final List<Log> logs = new ArrayList<Log>();
        for (int i = 0; i < pow(2, possibleAttributes.length); i++) {
            final List<Attribute> attrs = new LinkedList<Attribute>();
            attrs.addAll(Arrays.asList(requiredAttributes));
            for (int bit = 0; bit < possibleAttributes.length; bit++) {
                if ((i & (1 << bit)) != 0) {
                    attrs.add(possibleAttributes[bit]);
                }
            }

            if (attrs.size() < minElements || attrs.size() > maxElements) {
                continue;
            }
            logs.add(new Log(attrs.toArray(new Attribute[attrs.size()])));
        }
        return logs;
    }

}
