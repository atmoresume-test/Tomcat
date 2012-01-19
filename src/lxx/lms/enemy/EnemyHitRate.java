/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.lms.LogEfficiency;
import lxx.lms.LogPrediction;
import lxx.utils.HitRate;

import static java.lang.StrictMath.signum;

public class EnemyHitRate implements LogEfficiency<EnemyHitRate> {

    private final HitRate hitRate = new HitRate();

    public void update(LXXBullet bullet, LogPrediction prediction) {
        // todo(zhidkov): if (prediction.used)
        if (bullet.getState() == LXXBulletState.HITTED) {
            hitRate.hit();
        } else if (bullet.getState() == LXXBulletState.MISSED) {
            hitRate.miss();
        }
    }

    public int compareTo(EnemyHitRate o) {
        if (hitRate.getFireCount() == 0) {
            return 1;
        } else if (o.hitRate.getFireCount() == 0) {
            return -1;
        }
        return (int) signum(hitRate.getHitRate() - o.hitRate.getHitRate());
    }
}
