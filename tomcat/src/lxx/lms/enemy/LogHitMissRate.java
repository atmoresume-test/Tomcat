/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.lms.LogEfficiency;
import lxx.lms.LogPrediction;
import lxx.utils.AvgValue;

import static java.lang.StrictMath.signum;

public class LogHitMissRate implements LogEfficiency<LogHitMissRate> {

    private final AvgValue avgHitRate;
    private final AvgValue avgMissRate;

    public LogHitMissRate(int depth) {
        avgHitRate = new AvgValue(depth);
        avgMissRate = new AvgValue(depth);
    }

    public void update(LXXBullet bullet, LogPrediction prediction) {
        boolean isHit = bullet.getState() == LXXBulletState.HITTED || bullet.getState() == LXXBulletState.INTERCEPTED;

        if (isHit) {
            avgHitRate.addValue(prediction.getRate());
        } else {
            avgMissRate.addValue(prediction.getRate());
        }
    }

    public int compareTo(LogHitMissRate o) {
        return (int) signum((o.avgHitRate.getCurrentValue() - o.avgHitRate.getCurrentValue()) -
                (avgHitRate.getCurrentValue() - avgMissRate.getCurrentValue()));
    }
}
