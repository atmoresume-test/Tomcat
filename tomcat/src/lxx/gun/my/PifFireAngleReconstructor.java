/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.my;

import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.data_analysis.LxxDataPoint;
import lxx.gun.FireAngleReconstructor;
import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;

public class PifFireAngleReconstructor implements FireAngleReconstructor<LxxDataPoint> {

    private static final int AIMING_TIME = 2;

    private final Map<TurnSnapshot, BearingOffsetDanger[]> cache = new HashMap<TurnSnapshot, BearingOffsetDanger[]>();

    private final APoint robotPosAtFireTime;
    private final double bulletSpeed;
    private final double angleToTarget;
    private final Target target;

    public PifFireAngleReconstructor(APoint robotPosAtFireTime, double bulletSpeed, Target target) {
        this.robotPosAtFireTime = robotPosAtFireTime;
        this.bulletSpeed = bulletSpeed;
        this.target = target;
        angleToTarget = robotPosAtFireTime.angleTo(this.target);
    }

    public BearingOffsetDanger[] getBearingOffsets(LxxDataPoint logRecord) {
        BearingOffsetDanger[] res = cache.get(logRecord.ts);
        if (res == null) {
            final double angleToPnt = robotPosAtFireTime.angleTo(getFuturePos(logRecord.ts));
            res = new BearingOffsetDanger[]{new BearingOffsetDanger(Utils.normalRelativeAngle(angleToPnt - angleToTarget), 1)};
            cache.put(logRecord.ts, res);

        }
        return res;
    }

    private APoint getFuturePos(TurnSnapshot start) {
        final LXXPoint targetPos = target.getPosition();
        APoint futurePos = new LXXPoint(targetPos);

        TurnSnapshot currentSnapshot = start.next;
        currentSnapshot = skip(currentSnapshot, AIMING_TIME);
        final BattleField battleField = target.getState().getBattleField();
        final double absoluteHeadingRadians = target.getAbsoluteHeadingRadians();
        BulletState bs;
        final double speedSum = bulletSpeed + Rules.MAX_VELOCITY;
        long timeDelta;
        double bulletTravelledDistance = bulletSpeed;
        while ((bs = isBulletHitEnemy(futurePos, bulletTravelledDistance)) == BulletState.COMING) {
            if (currentSnapshot == null) {
                return null;
            }
            final DeltaVector dv = LXXUtils.getEnemyDeltaVector(start, currentSnapshot);
            final double alpha = absoluteHeadingRadians + dv.getAlphaRadians();
            futurePos = targetPos.project(alpha, dv.getLength());
            if (!battleField.contains(futurePos)) {
                return null;
            }
            timeDelta = currentSnapshot.getTime() - start.getTime() - AIMING_TIME;
            bulletTravelledDistance = timeDelta * bulletSpeed;
            int minBulletFlightTime = max((int) ((robotPosAtFireTime.aDistance(futurePos) - bulletTravelledDistance) / speedSum) - 1, 1);
            currentSnapshot = skip(currentSnapshot, minBulletFlightTime);
        }

        if (bs == BulletState.PASSED) {
            System.out.println("[WARN] Future pos calculation error");
        }

        return futurePos;
    }

    private TurnSnapshot skip(TurnSnapshot start, int count) {

        for (int i = 0; i < count; i++) {
            if (start == null) {
                return null;
            }
            start = start.next;
        }

        return start;
    }

    private BulletState isBulletHitEnemy(APoint predictedPos, double bulletTravelledDistance) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        if (enemyRectAtPredictedPos.contains(bulletPos)) {
            return BulletState.HITTING;
        } else if (bulletTravelledDistance > robotPosAtFireTime.aDistance(predictedPos) + LXXConstants.ROBOT_SIDE_HALF_SIZE) {
            return BulletState.PASSED;
        }
        return BulletState.COMING;
    }

    private enum BulletState {
        COMING,
        HITTING,
        PASSED
    }

}
