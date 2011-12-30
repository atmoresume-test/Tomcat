/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;

import java.util.List;

public class LogPrediction {

    private final List<BearingOffsetDanger> bearingOffsets;

    public boolean used = false;
    private double rate;

    public LogPrediction(List<BearingOffsetDanger> bearingOffsets) {
        this.bearingOffsets = bearingOffsets;
    }

    public List<BearingOffsetDanger> getBearingOffsets() {
        return bearingOffsets;
    }

    public void calculateEfficiency(LXXBullet bullet) {
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
        System.out.println("Ival: " + effectiveInterval);

        double totalDanger = 0;
        double realDanger = 0;
        for (BearingOffsetDanger bo : bearingOffsets) {
            System.out.println("Bo: " + bo.bearingOffset);
            totalDanger += bo.danger;
            if (effectiveInterval.contains(bo.bearingOffset)) {
                realDanger += bo.danger;
            }
        }
        System.out.println("Total bos: " + totalDanger);

        if (totalDanger == 0) {
            rate = 0;
        } else {
            rate = realDanger / totalDanger;
        }

        System.out.println("Efficiency: " + rate);
        System.out.println("================================================");
    }

    public double getRate() {
        return rate;
    }
}
