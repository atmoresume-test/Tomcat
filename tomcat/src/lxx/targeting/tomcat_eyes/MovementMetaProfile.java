/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import lxx.utils.Median;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class MovementMetaProfile {

    private static final int DISTANCE_SEGMENTS = 25;
    private final Median[] distancesMedianAngles = new Median[1700 / DISTANCE_SEGMENTS];

    private int enemyPreferredDistance = -1;
    private boolean rammer = false;

    public void update(LXXRobot owner, LXXRobot viewPoint) {
        final double distanceBetween = owner.aDistance(viewPoint);

        if (owner.getState().getVelocityModule() > 0) {
            int idx = (int) round(distanceBetween / DISTANCE_SEGMENTS);
            if (distancesMedianAngles[idx] == null) {
                distancesMedianAngles[idx] = new Median(2000);
            }
            final double angle = toDegrees(LXXUtils.anglesDiff(viewPoint.angleTo(owner), owner.getState().getAbsoluteHeadingRadians()));
            distancesMedianAngles[idx].addValue((int) angle);
            if (owner.getTime() % 10 == 0) {
                checkRammer();
            }
        }
    }

    private void checkRammer() {
        rammer = true;
        for (Median distancesMedianAngle : distancesMedianAngles) {
            if (distancesMedianAngle == null) {
                continue;
            }
            rammer &= distancesMedianAngle.getMedian() > 88;
        }
    }

    public int getPreferredDistance() {
        for (int i = 0; i < distancesMedianAngles.length - 1; i++) {
            if (distancesMedianAngles[i] == null ||
                    distancesMedianAngles[i + 1] == null) {
                continue;
            }
            double m1 = distancesMedianAngles[i].getMedian();
            double m2 = distancesMedianAngles[i + 1].getMedian();
            if (m1 > 75 && m1 < 90 &&
                    m2 > 90 && m2 < 100) {
                enemyPreferredDistance = (i + 1) * DISTANCE_SEGMENTS;
                break;
            }
        }

        return enemyPreferredDistance;
    }

    public boolean isRammer() {
        return rammer;
    }
}
