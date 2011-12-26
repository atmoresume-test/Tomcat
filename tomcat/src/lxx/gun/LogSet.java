/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.bullets.enemy.BulletShadow;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.LXXUtils;

import java.util.*;

public abstract class LogSet {

    protected final Map<Log, LogEfficiency[]> logEfficiencies = new HashMap<Log, LogEfficiency[]>();

    protected final List<Log> allLogs;

    private final List<Log>[] bestLogs;
    private final int logsEfficienciesCount;
    private final int bestLogsPerCategoty;

    public LogSet(List<Log> allLogs, LogEfficienciesFactory logEfficienciesFactory, int bestLogsPerCategoty) {
        this.allLogs = allLogs;
        this.bestLogsPerCategoty = bestLogsPerCategoty;

        for (Log log : allLogs) {
            logEfficiencies.put(log, logEfficienciesFactory.createEfficiencies());
        }

        logsEfficienciesCount = logEfficienciesFactory.getEfficienciesCount();
        bestLogs = new List[logsEfficienciesCount];
        for (int i = 0; i < logsEfficienciesCount; i++) {
            bestLogs[i] = new ArrayList<Log>(allLogs);
        }
    }

    public void updateLogEfficiencies(LXXBullet bullet) {
        for (Log log : logEfficiencies.keySet()) {
            final LogEfficiency[] les = logEfficiencies.get(log);
            final PD pd = (PD) bullet.getAimPredictionData();
            for (LogEfficiency le : les) {
                LogPrediction logPrediction = pd.getLogPrediction(log);
                if (logPrediction == null) {
                    logPrediction = getLogPrediction(log.getRecordsIterator(bullet.getAimPredictionData().getTs()), bullet.getSpeed(), bullet.getBulletShadows());
                }
                le.update(bullet, logPrediction);
            }
        }

        updateBestLogs();
    }

    public PD getPredictionData(TurnSnapshot currentTS, double bulletSpeed, LXXRobot t, Collection<BulletShadow> bulletShadows) {
        final List<BearingOffsetDanger> bearingOffsets = new ArrayList<BearingOffsetDanger>();

        final Map<Log, LogPrediction> logPredictions = new HashMap<Log, LogPrediction>();
        final long roundTime = LXXUtils.getRoundTime(t.getTime(), t.getRound());
        for (Log log : getBestLogs()) {
            final LogPrediction logPrediction = getLogPrediction(log.getRecordsIterator(currentTS), bulletSpeed, bulletShadows);
            logPredictions.put(log, logPrediction);
            for (BearingOffsetDanger bod : logPrediction.getBearingOffsets()) {
                bearingOffsets.add(bod);
            }
        }

        // todo(zhidkov): implement me
        /*if (bearingOffsets.size() == 0) {
            final GunType enemyGunType = office.getTomcatEyes().getEnemyGunType(t);
            fillWithSimpleBOs(ts, t, bearingOffsets, enemyGunType);
        }*/

        return new PD(bearingOffsets, roundTime, logPredictions, currentTS, bulletShadows);
    }

    protected void updateBestLogs() {
        for (int i = 0; i < logsEfficienciesCount; i++) {
            final int idx = i;
            Collections.sort(bestLogs[i], new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return logEfficiencies.get(o1)[idx].compareTo(logEfficiencies.get(o2)[idx]);
                }
            });
        }
    }

    protected List<Log> getBestLogs() {
        final List<Log> bestLogs = new ArrayList<Log>();

        for (int i = 0; i < logsEfficienciesCount; i++) {
            for (int j = 0; j < bestLogsPerCategoty; j++) {
                bestLogs.add(this.bestLogs[i].get(j));
            }
        }

        return bestLogs;
    }

    protected abstract LogPrediction getLogPrediction(Iterator recordsIterator, double bulletSpeed, Collection<BulletShadow> bulletShadows);

    protected abstract FireAngleReconstructor getFireAngleReconstructor(TurnSnapshot query);

}
