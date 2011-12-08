/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.paint.Painter;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.RobotImage;
import robocode.Rules;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaveSurfingMovement implements Movement, Painter {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final PointsGenerator pointsGenerator;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;

    public WaveSurfingMovement(Office office) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();

        pointsGenerator = new PointsGenerator(new DistanceController(office.getTargetManager()), robot.getState().getBattleField());
    }

    public MovementDecision getMovementDecision() {
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        selectOrbitDirection(lxxBullets);

        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final LXXPoint surfPoint = pointsGenerator.getSurfPoint(opponent, lxxBullets.get(0));

        final OrbitDirection orbitDirection = getOrbitDirection();
        final int desiredSpeed = (prevPrediction.minDangerPoint.isStop && prevPrediction.nextOrbitDirection == null)
                ? 0
                : 8;
        return pointsGenerator.getMovementDecision(surfPoint, orbitDirection, robot.getState(), opponent, desiredSpeed);
    }

    private OrbitDirection getOrbitDirection() {
        return (prevPrediction.minDangerPoint.followPointsCount <= 1 || prevPrediction.nextOrbitDirection == null)
                ? prevPrediction.minDangerPoint.orbitDirection
                : prevPrediction.nextOrbitDirection.getOpposite();
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        MovementDirectionPrediction nextPrediction = new MovementDirectionPrediction();
        nextPrediction.bullets = lxxBullets;
        nextPrediction.cwPoints = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
        nextPrediction.ccwPoints = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
        final List<WSPoint> futurePoses = new ArrayList<WSPoint>();
        futurePoses.addAll(nextPrediction.cwPoints);
        futurePoses.addAll(nextPrediction.ccwPoints);
        Collections.sort(futurePoses);
        nextPrediction.minDangerPoint = futurePoses.get(0);
        if (nextPrediction.minDangerPoint.followPointsCount > 1 && lxxBullets.size() >= 2) {
            nextPrediction.nextOrbitDirection = getSecondBulletOD(new RobotImage(robot.getState()), lxxBullets, futurePoses.get(0));
        }
        prevPrediction = nextPrediction;
    }

    private OrbitDirection getSecondBulletOD(RobotImage robotImage, List<LXXBullet> lxxBullets, LXXPoint dst) {
        final int time = (int) lxxBullets.get(0).getFlightTime(dst);

        final List<LXXBullet> secondBullets = lxxBullets.subList(1, lxxBullets.size());
        final List<WSPoint> secondWavePoints = new ArrayList<WSPoint>();

        final APoint surfPoint = secondBullets.get(0).getFirePosition();

        RobotImage robotImg = new RobotImage(dst, Rules.MAX_VELOCITY, surfPoint.angleTo(dst) + LXXConstants.RADIANS_90, robotImage.getBattleField(), 0, robotImage.getEnergy());
        final List<WSPoint> secondCWPoints = pointsGenerator.generatePoints(OrbitDirection.CLOCKWISE, secondBullets.get(0), robotImg, null, time);
        postProcessPoints(OrbitDirection.CLOCKWISE, secondCWPoints, prevPrediction != null && prevPrediction.minDangerPoint.orbitDirection == OrbitDirection.CLOCKWISE);

        robotImg = new RobotImage(dst, Rules.MAX_VELOCITY, surfPoint.angleTo(dst) - LXXConstants.RADIANS_90, robotImage.getBattleField(), 0, robotImage.getEnergy());
        final List<WSPoint> secondCCWPoints = pointsGenerator.generatePoints(OrbitDirection.COUNTER_CLOCKWISE, secondBullets.get(0), robotImg, null, time);
        postProcessPoints(OrbitDirection.COUNTER_CLOCKWISE, secondCCWPoints, prevPrediction != null && prevPrediction.minDangerPoint.orbitDirection == OrbitDirection.COUNTER_CLOCKWISE);

        secondWavePoints.addAll(secondCWPoints);
        secondWavePoints.addAll(secondCCWPoints);

        Collections.sort(secondWavePoints);
        return secondWavePoints.get(0).orbitDirection;
    }

    private List<WSPoint> predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection, RobotImage robotImage, RobotImage opponentImg) {
        final List<WSPoint> wsPoints = pointsGenerator.generatePoints(orbitDirection, lxxBullets.get(0), new RobotImage(robotImage), opponentImg != null ? new RobotImage(opponentImg) : opponentImg, 0);
        final boolean isSameDirection = prevPrediction != null && orbitDirection == prevPrediction.minDangerPoint.orbitDirection;
        postProcessPoints(orbitDirection, wsPoints, isSameDirection);
        return wsPoints;
    }

    private void postProcessPoints(OrbitDirection orbitDirection, List<WSPoint> wsPoints, boolean sameDirection) {
        int idx = 0;
        for (WSPoint pnt : wsPoints) {
            pnt.orbitDirection = orbitDirection;
            if (sameDirection) {
                pnt.pointDanger.setDangerMultiplier(0.95);
            }
            pnt.pointDanger.calculateDanger();
            pnt.followPointsCount = wsPoints.size() - 1 - idx;
            idx++;
        }
    }

    private List<LXXBullet> getBullets() {
        List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(2);
        if (bulletsOnAir.size() < 2 && duelOpponent != null) {
            bulletsOnAir.add(enemyBulletManager.createFutureBullet(duelOpponent));
        }
        if (bulletsOnAir.size() == 0) {
            bulletsOnAir = enemyBulletManager.getAllBulletsOnAir();
        }
        return bulletsOnAir;
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        drawPath(g, prevPrediction.cwPoints, new Color(0, 255, 0, 200));
        drawPath(g, prevPrediction.ccwPoints, new Color(255, 0, 0, 200));

        g.setColor(new Color(0, 255, 0, 200));
        g.drawCircle(prevPrediction.minDangerPoint, 16);
        g.drawCross(prevPrediction.minDangerPoint, 16);

        g.setColor(Color.GREEN);
        if (prevPrediction.nextOrbitDirection != null) {
            LXXPoint secondBulletFirePos = prevPrediction.bullets.get(1).getFirePosition();
            final double angleToDst = secondBulletFirePos.angleTo(prevPrediction.minDangerPoint);
            final LXXPoint pnt1 = secondBulletFirePos.project(angleToDst, secondBulletFirePos.aDistance(prevPrediction.minDangerPoint) + 15);
            g.drawArrow(pnt1, angleToDst + LXXConstants.RADIANS_90 * prevPrediction.nextOrbitDirection.sign, 25);
        }

        final OrbitDirection orbitDirection = getOrbitDirection();
        LXXPoint firstBulletFirePos = prevPrediction.bullets.get(0).getFirePosition();
        final double angleToRobot = firstBulletFirePos.angleTo(robot);
        final LXXPoint pnt1 = firstBulletFirePos.project(angleToRobot, firstBulletFirePos.aDistance(prevPrediction.minDangerPoint) - 15);
        g.drawArrow(pnt1, angleToRobot + LXXConstants.RADIANS_90 * orbitDirection.sign, 25);
    }

    private void drawPath(LXXGraphics g, List<WSPoint> points, Color color) {
        g.setColor(color);
        for (WSPoint pnt : points) {
            g.drawCircle(pnt, 4);
        }
    }

    public class MovementDirectionPrediction {

        public List<WSPoint> cwPoints;
        public List<WSPoint> ccwPoints;
        public WSPoint minDangerPoint;
        public OrbitDirection nextOrbitDirection;
        public List<LXXBullet> bullets;
    }

}
