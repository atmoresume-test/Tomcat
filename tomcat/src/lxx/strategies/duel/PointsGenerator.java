/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.bullets.enemy.EnemyBulletPredictionData;
import lxx.strategies.MovementDecision;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class PointsGenerator {

    private final DistanceController distanceController;
    private final BattleField battleField;

    public PointsGenerator(DistanceController distanceController, BattleField battleField) {
        this.distanceController = distanceController;
        this.battleField = battleField;
    }

    private PointDanger getPointDanger(LXXBullet lxxBullet, LXXPoint robotPos) {
        return new PointDanger(lxxBullet, lxxBullet != null ? getWaveDanger(robotPos, lxxBullet) : 0, battleField.center.aDistance(robotPos));
    }

    private double getWaveDanger(LXXPoint pnt, LXXBullet bullet) {
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        final List<PastBearingOffset> predictedBearingOffsets = aimPredictionData.getPredictedBearingOffsets();
        if (predictedBearingOffsets.size() == 0) {
            return 0;
        }
        final LXXPoint firePos = bullet.getFirePosition();
        final double alpha = LXXUtils.angle(firePos.x, firePos.y, pnt.x, pnt.y);
        final double bearingOffset = Utils.normalRelativeAngle(alpha - bullet.noBearingOffset());
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(alpha, firePos.aDistance(pnt));

        double bulletsDanger = 0;
        final double hiEffectDist = robotWidthInRadians * 0.75;
        final double lowEffectDist = robotWidthInRadians * 2.55;
        for (PastBearingOffset bo : predictedBearingOffsets) {
            if (bo.bearingOffset < bearingOffset - lowEffectDist) {
                continue;
            } else if (bo.bearingOffset > bearingOffset + lowEffectDist) {
                break;
            }
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < hiEffectDist) {
                bulletsDanger += (2 - (dist / hiEffectDist)) * bo.danger;
            } else if (dist < lowEffectDist) {
                bulletsDanger += (1 - (dist / lowEffectDist)) * bo.danger;
            }
        }

        double intersection = 0;
        final double halfRobotWidthInRadians = robotWidthInRadians / 2;
        final IntervalDouble robotIval = new IntervalDouble(bearingOffset - halfRobotWidthInRadians, bearingOffset + halfRobotWidthInRadians);
        for (IntervalDouble shadow : bullet.getMergedShadows()) {
            if (robotIval.intersects(shadow)) {
                intersection += robotIval.intersection(shadow);
            }
        }
        bulletsDanger *= 1 - intersection / robotWidthInRadians;

        return bulletsDanger;
    }

    public List<WSPoint> generatePoints(OrbitDirection orbitDirection, LXXBullet bullet, RobotImage robotImg, RobotImage opponentImg, int time) {
        final List<WSPoint> points = new ArrayList<WSPoint>();
        LXXPoint robotImgPos = robotImg.getPosition();
        if (robotImg.getSpeed() == 0) {
            points.add(new WSPoint(robotImg, getPointDanger(bullet, robotImgPos)));
            points.get(0).isStop = true;
        }
        final LXXPoint surfPoint = getSurfPoint(opponentImg, bullet);
        final double bulletSpeed = bullet.getSpeed();
        double travelledDistance = bullet.getTravelledDistance() + bulletSpeed * time;
        final MovementDecision enemyMd;
        if (opponentImg != null) {
            enemyMd = new MovementDecision(Rules.MAX_VELOCITY * signum(opponentImg.getVelocity()), 0);
        } else {
            enemyMd = new MovementDecision(0, 0);
        }
        final LXXPoint firePosition = bullet.getFirePosition();
        do {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, opponentImg, 8);
            robotImg.apply(md);
            robotImgPos = robotImg.getPosition();
            points.add(new WSPoint(robotImg, getPointDanger(bullet, robotImgPos)));
            if (opponentImg != null) {
                opponentImg.apply(enemyMd);
                final LXXPoint oppPos = opponentImg.getPosition();
                for (WSPoint prevPoint : points) {
                    prevPoint.pointDanger.distToEnemy = min(prevPoint.pointDanger.distToEnemy, prevPoint.aDistanceSq(oppPos));
                }
            }
            travelledDistance += bulletSpeed;
        } while (firePosition.aDistance(robotImgPos) > travelledDistance);

        return points;
    }

    public MovementDecision getMovementDecision(LXXPoint surfPoint, OrbitDirection orbitDirection,
                                                LXXRobotState robot, LXXRobotState opponent, double desiredSpeed) {
        final LXXPoint robotPos = robot.getPosition();
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robotPos, orbitDirection);
        if (robotPos.x < battleField.noSmoothX.a || robotPos.x > battleField.noSmoothX.b ||
                robotPos.y < battleField.noSmoothY.a || robotPos.y > battleField.noSmoothY.b) {
            desiredHeading = battleField.smoothWalls(robotPos, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);
        }
        if (opponent != null) {
            final LXXPoint oppPos = opponent.getPosition();
            double angleToOpponent = LXXUtils.angle(robotPos.x, robotPos.y, oppPos.x, oppPos.y);
            if (((LXXUtils.anglesDiff(desiredHeading, angleToOpponent) < LXXUtils.getRobotWidthInRadians(angleToOpponent, robot.aDistance(opponent)) * 1.2))) {
                desiredSpeed = 0;
            }
        }

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public LXXPoint getSurfPoint(LXXRobotState duelOpponent, LXXBullet bullet) {
        if (duelOpponent == null) {
            return bullet.getFirePosition();
        }

        return new LXXPoint(duelOpponent);
    }

}
