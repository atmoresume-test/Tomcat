/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.bullets.enemy.BulletShadow;
import lxx.data_analysis.DataPoint;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.LXXUtils;

import java.util.*;

public abstract class LogSet<L extends Log<E>, E extends DataPoint> {

    protected final Map<Log, LogEfficiency[]> logEfficiencies = new HashMap<Log, LogEfficiency[]>();

    protected final List<L> allLogs;

    private final List<Log<E>>[] bestLogs;
    private final int logsEfficienciesCount;
    private final int bestLogsPerCategoty;

    public LogSet(List<L> allLogs, LogEfficienciesFactory logEfficienciesFactory, int bestLogsPerCategoty) {
        this.allLogs = allLogs;
        this.bestLogsPerCategoty = bestLogsPerCategoty;

        for (Log log : allLogs) {
            logEfficiencies.put(log, logEfficienciesFactory.createEfficiencies());
        }

        logsEfficienciesCount = logEfficienciesFactory.getEfficienciesCount();
        bestLogs = new List[logsEfficienciesCount];
        for (int i = 0; i < logsEfficienciesCount; i++) {
            bestLogs[i] = new ArrayList<Log<E>>(allLogs);
        }
    }

    public void updateLogEfficiencies(LXXBullet bullet) {
        TurnSnapshot query = bullet.getAimPredictionData().getTs();
        for (Log log : logEfficiencies.keySet()) {
            final LogEfficiency[] les = logEfficiencies.get(log);
            final PD pd = (PD) bullet.getAimPredictionData();
            for (LogEfficiency le : les) {
                LogPrediction logPrediction = pd.getLogPrediction(log);
                if (logPrediction == null) {
                    logPrediction = getLogPrediction(query, log.getRecordsIterator(query), bullet.getSpeed(), bullet.getBulletShadows());
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
        for (Log<E> log : getBestLogs()) {
            final LogPrediction logPrediction = getLogPrediction(currentTS, log.getRecordsIterator(currentTS), bulletSpeed, bulletShadows);
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
                    if (o1.isEnabled() && !o2.isEnabled()) {
                        return 1;
                    } else if (!o1.isEnabled() && o2.isEnabled()) {
                        return -1;
                    }
                    return logEfficiencies.get(o1)[idx].compareTo(logEfficiencies.get(o2)[idx]);
                }

            });
        }
    }

    protected List<L> getBestLogs() {
        final List<L> bestLogs = new ArrayList<L>();

        for (int i = 0; i < logsEfficienciesCount; i++) {
            for (int j = 0; j < bestLogsPerCategoty; j++) {
                bestLogs.add((L) this.bestLogs[i].get(j));
            }
        }

        return bestLogs;
    }

    protected abstract LogPrediction getLogPrediction(TurnSnapshot query, Iterator<E> recordsIterator, double bulletSpeed, Collection<BulletShadow> bulletShadows);

}
