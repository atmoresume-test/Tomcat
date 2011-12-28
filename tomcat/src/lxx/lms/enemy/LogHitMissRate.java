/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.lms.LogEfficiency;
import lxx.lms.LogPrediction;
import lxx.utils.AvgValue;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;

import static java.lang.StrictMath.signum;

public class LogHitMissRate implements LogEfficiency<LogHitMissRate> {

    private final AvgValue avgHitRate;
    private final AvgValue avgMissRate;

    public LogHitMissRate(int depth) {
        avgHitRate = new AvgValue(depth);
        avgMissRate = new AvgValue(depth);
    }

    public void update(LXXBullet bullet, LogPrediction prediction) {
        final IntervalDouble effectiveInterval;
        boolean isHit = bullet.getState() == LXXBulletState.HITTED || bullet.getState() == LXXBulletState.INTERCEPTED;
        if (isHit) {
            final double robotHalfSizeRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), bullet.getTarget()) / 2;
            final double currentBO = bullet.getRealBearingOffsetRadians();
            effectiveInterval = new IntervalDouble(currentBO - robotHalfSizeRadians, currentBO + robotHalfSizeRadians);
        } else {
            final IntervalDouble hitInterval = bullet.getWave().getHitBearingOffsetInterval();
            effectiveInterval = new IntervalDouble(hitInterval.center() - hitInterval.getLength() * 0.4,
                    hitInterval.center() + hitInterval.getLength() * 0.4);
        }

        double totalDanger = 0;
        double realDanger = 0;
        for (BearingOffsetDanger pastBo : prediction.getBearingOffsets()) {
            totalDanger += pastBo.danger;
            if (effectiveInterval.contains(pastBo.bearingOffset)) {
                realDanger += pastBo.danger;
            }
        }

        if (totalDanger != 0) {
            if (isHit) {
                avgHitRate.addValue(realDanger / totalDanger);
            } else {
                avgMissRate.addValue(realDanger / totalDanger);
            }
        }
    }

    public int compareTo(LogHitMissRate o) {
        return (int) signum((avgHitRate.getCurrentValue() - avgMissRate.getCurrentValue()) -
                (o.avgHitRate.getCurrentValue() - o.avgHitRate.getCurrentValue()));
    }
}
