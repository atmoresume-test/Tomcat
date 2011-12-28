/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.bullets.enemy.BulletShadow;
import lxx.bullets.enemy.UndirectedGuessFactor;
import lxx.data_analysis.LxxDataPoint;
import lxx.lms.Log;
import lxx.lms.LogPrediction;
import lxx.lms.LogSet;
import lxx.lms.PD;
import lxx.office.Office;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.LXXUtils;

import java.util.*;

import static java.lang.Math.pow;

public class EnemyLogSet extends LogSet<EnemyGunRTreeLog, LxxDataPoint<UndirectedGuessFactor>> {

    private static final int BULLETS_PER_LOG = 5;

    private static final Attribute[] hitLogsPossibleAttributes = new Attribute[]{
            AttributesManager.myAcceleration,
            AttributesManager.distBetween,
            AttributesManager.myDistToForwardWall,
            AttributesManager.myDistLast10Ticks,
    };

    private static final Attribute[] visitLogsPossibleAttributes = {
            AttributesManager.myLateralSpeed,
            AttributesManager.myAcceleration,
            AttributesManager.distBetween,
            AttributesManager.myDistToForwardWall,
    };

    private static final Map<Attribute, Double> halfSideLength = LXXUtils.toMap(
            AttributesManager.myLateralSpeed, 2D,
            AttributesManager.myAcceleration, 0D,
            AttributesManager.distBetween, 75D,
            AttributesManager.myDistToForwardWall, 50D,
            AttributesManager.myDistLast10Ticks, 20D);

    private final Map<TurnSnapshot, GfFireAngleReconstructor> fireAngleReconstructors = new HashMap<TurnSnapshot, GfFireAngleReconstructor>();
    private final Office office;

    public EnemyLogSet(Office office) {
        super(createLogSet(), new EnemyLogsEfficiencyFactory(), 3);
        this.office = office;
    }

    public void processBullet(LXXBullet bullet) {
        updateLogEfficiencies(bullet);
        if (bullet.getState() == LXXBulletState.MISSED) {
            return;
        }

        updateLogs(bullet, EnemyGunRTreeLog.LogType.HIT_LOG);
    }

    public void processVisit(LXXBullet bullet) {
        updateLogs(bullet, EnemyGunRTreeLog.LogType.VISIT_LOG);
    }

    private void updateLogs(LXXBullet bullet, EnemyGunRTreeLog.LogType logType) {
        final double direction = bullet.getTargetLateralDirection();
        final double undirectedGuessFactor = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        final boolean isVisitLogsEnabled = office.getStatisticsManager().getEnemyHitRate().getHitCount() >= 4;
        final UndirectedGuessFactor payload = new UndirectedGuessFactor(undirectedGuessFactor, direction);

        for (EnemyGunRTreeLog log : allLogs) {
            if (log.getLogType() == logType) {
                log.addEntry(LxxDataPoint.createPlainPoint(bullet.getAimPredictionData().getTs(), payload, log.getAttributes()));
            }

            if (logType == EnemyGunRTreeLog.LogType.VISIT_LOG) {
                log.setEnabled(isVisitLogsEnabled);
            }
        }

        fireAngleReconstructors.remove(bullet.getAimPredictionData().getTs());
    }

    public void updateBulletPredictionData(LXXBullet bullet) {
        final LXXRobot owner = bullet.getOwner();
        final long roundTime = LXXUtils.getRoundTime(owner.getTime(), owner.getRound());
        updateOldData(bullet);
        calculateNewData(bullet, roundTime);
    }

    private void updateOldData(LXXBullet bullet) {
        final PD aimPredictionData = bullet.getPD();
        final boolean isShadowsChanged = bullet.getBulletShadows().size() != aimPredictionData.getBulletShadows().size();
        for (Log log : aimPredictionData.getLogs()) {
            aimPredictionData.getLogPrediction(log).used = false;
            if (!isNeedInUpdate((EnemyGunRTreeLog) log, bullet, aimPredictionData, isShadowsChanged)) {
                continue;
            }
            aimPredictionData.addLogPrediction(log,
                    getLogPrediction(aimPredictionData.getTs(), log.getRecordsIterator(aimPredictionData.getTs()), bullet.getBullet().getPower(), bullet.getBulletShadows()));
        }
        if (isShadowsChanged) {
            aimPredictionData.setBulletShadows(bullet.getBulletShadows());
        }
    }

    private void calculateNewData(LXXBullet bullet, long roundTime) {
        final PD aimPredictionData = bullet.getPD();
        final List<BearingOffsetDanger> bearingOffsets = new ArrayList<BearingOffsetDanger>();
        for (EnemyGunRTreeLog log : getBestLogs()) {
            LogPrediction logPrediction = aimPredictionData.getLogPrediction(log);
            List<BearingOffsetDanger> logBearingOffsets = null;
            if (logPrediction == null) {
                logPrediction = getLogPrediction(aimPredictionData.getTs(), log.getRecordsIterator(aimPredictionData.getTs()), bullet.getBullet().getPower(), bullet.getBulletShadows());
                logBearingOffsets = logPrediction.getBearingOffsets();
                aimPredictionData.addLogPrediction(log, logPrediction);
            }
            logPrediction.used = true;
            bearingOffsets.addAll(logBearingOffsets);
        }

        if (bearingOffsets.size() != 0) {
            aimPredictionData.setPredictedBearingOffsets(bearingOffsets);
            aimPredictionData.setPredictionRoundTime(roundTime);
        }
    }

    private boolean isNeedInUpdate(EnemyGunRTreeLog log, LXXBullet bullet, PD aimPredictionData, boolean isShadowsChanged) {
        return (isShadowsChanged ||
                (log.getLogType() == EnemyGunRTreeLog.LogType.HIT_LOG && log.getLastUpdateRoundTime() > aimPredictionData.getPredictionRoundTime()) ||
                hasShadowedBOs(aimPredictionData.getLogPrediction(log), bullet.getBulletShadows()));
    }

    private boolean hasShadowedBOs(LogPrediction lp, Collection<BulletShadow> shadows) {
        for (BulletShadow shadow : shadows) {
            for (BearingOffsetDanger bo : lp.getBearingOffsets()) {
                if (shadow.contains(bo.bearingOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected LogPrediction getLogPrediction(TurnSnapshot query, Iterator<LxxDataPoint<UndirectedGuessFactor>> recordsIterator, double bulletSpeed, Collection<BulletShadow> bulletShadows) {
        GfFireAngleReconstructor far = fireAngleReconstructors.get(query);

        if (far == null) {
            far = new GfFireAngleReconstructor(query, bulletSpeed, bulletShadows);

            fireAngleReconstructors.put(query, far);
        }

        final ArrayList<BearingOffsetDanger> bearingOffsets = new ArrayList<BearingOffsetDanger>();

        int boCnt = 0;
        while (recordsIterator.hasNext()) {
            if (boCnt == BULLETS_PER_LOG) {
                break;
            }

            final BearingOffsetDanger[] bos = far.getBearingOffsets(recordsIterator.next());
            if (bos.length > 0) {
                boCnt++;
            }
            bearingOffsets.addAll(Arrays.asList(bos));
        }

        return new LogPrediction(bearingOffsets);
    }

    private static List<EnemyGunRTreeLog> createLogSet() {
        final List<EnemyGunRTreeLog> res = new ArrayList<EnemyGunRTreeLog>();

        List<EnemyGunRTreeLog> hitLogs = createLogs(hitLogsPossibleAttributes, EnemyGunRTreeLog.LogType.HIT_LOG, AttributesManager.myLateralSpeed);
        List<EnemyGunRTreeLog> visitLogs = createLogs(visitLogsPossibleAttributes, EnemyGunRTreeLog.LogType.VISIT_LOG);

        for (Log l : hitLogs) {
            l.setEnabled(true);
        }

        for (Log l : visitLogs) {
            l.setEnabled(false);
        }

        res.addAll(hitLogs);
        res.addAll(visitLogs);

        return res;
    }

    private static List<EnemyGunRTreeLog> createLogs(Attribute[] possibleAttributes, EnemyGunRTreeLog.LogType logType, Attribute... requiredAttributes) {
        final List<EnemyGunRTreeLog> logs = new ArrayList<EnemyGunRTreeLog>();
        for (int i = 0; i < pow(2, possibleAttributes.length); i++) {
            final List<Attribute> attrs = new LinkedList<Attribute>();
            attrs.addAll(Arrays.asList(requiredAttributes));
            for (int bit = 0; bit < possibleAttributes.length; bit++) {
                if ((i & (1 << bit)) != 0) {
                    attrs.add(possibleAttributes[bit]);
                }
            }

            if (attrs.size() < 1) {
                continue;
            }
            final Map<Attribute, Double> logRangeSizes = new HashMap<Attribute, Double>();
            for (Attribute attr : attrs) {
                logRangeSizes.put(attr, halfSideLength.get(attr));
            }
            logs.add(new EnemyGunRTreeLog(logRangeSizes, logType));
        }
        return logs;
    }

}
