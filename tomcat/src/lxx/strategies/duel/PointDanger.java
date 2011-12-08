/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.bullets.LXXBullet;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

class PointDanger {

    private final LXXBullet bullet;
    private final double dangerOnFirstWave;
    private final double distanceToCenter;

    private double dangerMultiplier = 1;
    public double danger;
    public double distToEnemy = Integer.MAX_VALUE;

    PointDanger(LXXBullet bullet, double dangerOnFirstWave, double distanceToCenter) {
        this.bullet = bullet;
        this.dangerOnFirstWave = dangerOnFirstWave;
        this.distanceToCenter = distanceToCenter;
    }

    public void setDangerMultiplier(double dangerMultiplier) {
        this.dangerMultiplier = dangerMultiplier;
    }

    public void calculateDanger() {
        double thisDanger = dangerOnFirstWave * 120 +
                distanceToCenter / 800 * 5 +
                max(0, (500 - sqrt(distToEnemy))) / distToEnemy * 15;
        if (bullet != null) {
            thisDanger = thisDanger * bullet.getBullet().getPower();
        }


        danger = thisDanger * dangerMultiplier;
    }

}
