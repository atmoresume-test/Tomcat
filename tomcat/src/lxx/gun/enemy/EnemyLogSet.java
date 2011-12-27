/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.enemy;

import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.bullets.enemy.BulletShadow;
import lxx.bullets.enemy.UndirectedGuessFactor;
import lxx.data_analysis.LxxDataPoint;
import lxx.gun.Log;
import lxx.gun.LogPrediction;
import lxx.gun.LogSet;
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

    public EnemyLogSet() {
        super(createLogSet(), new EnemyLogsEfficiencyFactory(), 3);
    }

    public void processHit(LXXBullet bullet) {
        updateLogEfficiencies(bullet);

        /*logSet.learn(bullet, true);
        if (office.getStatisticsManager().getEnemyHitRate().getHitCount() == 4) {
            logSet.shortLogs.addAll(logSet.visitLogsSet);
            logSet.midLogs.addAll(logSet.visitLogsSet);
            logSet.longLogs.addAll(logSet.visitLogsSet);
            logSet.enemyHitRateLogs.addAll(logSet.visitLogsSet);
        }*/
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

        List<EnemyGunRTreeLog> hitLogs = createLogs(hitLogsPossibleAttributes, AttributesManager.myLateralSpeed);
        List<EnemyGunRTreeLog> visitLogs = createLogs(visitLogsPossibleAttributes);

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

    private static List<EnemyGunRTreeLog> createLogs(Attribute[] possibleAttributes, Attribute... requiredAttributes) {
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
            logs.add(new EnemyGunRTreeLog(logRangeSizes));
        }
        return logs;
    }

}
