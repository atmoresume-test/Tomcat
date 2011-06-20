/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.LXXRobotState;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.io.Serializable;

import static java.lang.Math.*;

public class MovementDecision implements Serializable {

    private final double desiredVelocity;
    private final double turnRateRadians;

    public MovementDecision(double desiredVelocity, double turnRateRadians) {
        this.desiredVelocity = desiredVelocity;
        this.turnRateRadians = turnRateRadians;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getDesiredVelocity() {
        return desiredVelocity;
    }

    public static MovementDecision toMovementDecision(LXXRobotState robot, double desiredVelocityModule, double desiredHeading) {
        boolean wantToGoFront = LXXUtils.anglesDiff(robot.getHeadingRadians(), desiredHeading) < LXXConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LXXConstants.RADIANS_180);
        final double turnRateRadians =
                LXXUtils.limit(-Rules.getTurnRateRadians(robot.getVelocityModule()),
                        Utils.normalRelativeAngle(normalizedDesiredHeading - robot.getHeadingRadians()),
                        Rules.getTurnRateRadians(robot.getVelocityModule()));

        double movementHeading = robot.getVelocity() != 0
                ? robot.getAbsoluteHeadingRadians()
                : wantToGoFront ? robot.getHeadingRadians() : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180);
        movementHeading += turnRateRadians * signum(robot.getVelocity() != 0 ? robot.getVelocity() : wantToGoFront ? 1 : -1);
        if (!robot.getBattleField().contains(robot.project(movementHeading, LXXUtils.getStopDistance(abs(robot.getVelocity())) + 15))) {
            desiredVelocityModule = 0;
        }

        return new MovementDecision(min(desiredVelocityModule, LXXUtils.getVelocityByTurnRate(abs(turnRateRadians))) * (wantToGoFront ? 1 : -1), turnRateRadians);
    }

    public String toString() {
        return String.format("(desired velocity = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRateRadians));
    }

}
