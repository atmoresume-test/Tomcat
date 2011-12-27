/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.enemy;

import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.bullets.enemy.BulletShadow;
import lxx.bullets.enemy.UndirectedGuessFactor;
import lxx.data_analysis.LxxDataPoint;
import lxx.gun.FireAngleReconstructor;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.LXXUtils;

import java.util.*;

public class GfFireAngleReconstructor implements FireAngleReconstructor<LxxDataPoint<UndirectedGuessFactor>> {

    private final Map<TurnSnapshot, BearingOffsetDanger[]> cache = new HashMap<TurnSnapshot, BearingOffsetDanger[]>();

    private final Collection<BulletShadow> bulletShadows;
    private final double lateralDirection;
    private final double maxEscapeAngleQuick;

    public GfFireAngleReconstructor(TurnSnapshot fireTimeTS, double bulletSpeed, Collection<BulletShadow> bulletShadows) {
        this.bulletShadows = bulletShadows;
        lateralDirection = LXXUtils.lateralDirection(fireTimeTS.getEnemyImage(), fireTimeTS.getMeImage());
        maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);
    }

    public BearingOffsetDanger[] getBearingOffsets(LxxDataPoint<UndirectedGuessFactor> logRecord) {
        BearingOffsetDanger[] res = cache.get(logRecord.ts);

        if (res == null) {

            final List<BearingOffsetDanger> bearingOffsets = new ArrayList<BearingOffsetDanger>(2);
            if (logRecord.payload.lateralDirection != 0 && lateralDirection != 0) {

                final double bearingOffset = logRecord.payload.guessFactor * logRecord.payload.lateralDirection * lateralDirection * maxEscapeAngleQuick;
                if (!isShadowed(bearingOffset, bulletShadows)) {
                    bearingOffsets.add(new BearingOffsetDanger(bearingOffset, 1));
                }
            } else {
                final double bearingOffset1 = logRecord.payload.guessFactor * 1 * maxEscapeAngleQuick;
                if (!isShadowed(bearingOffset1, bulletShadows)) {
                    bearingOffsets.add(new BearingOffsetDanger(bearingOffset1, 1));
                }

                final double bearingOffset2 = logRecord.payload.guessFactor * -1 * maxEscapeAngleQuick;
                if (!isShadowed(bearingOffset2, bulletShadows)) {
                    bearingOffsets.add(new BearingOffsetDanger(bearingOffset2, 1));
                }
            }

            res = new BearingOffsetDanger[bearingOffsets.size()];
            bearingOffsets.toArray(res);

            cache.put(logRecord.ts, res);
        }

        return res;
    }

    private boolean isShadowed(double bearingOffset, Collection<BulletShadow> shadows) {
        for (BulletShadow shadow : shadows) {
            if (shadow.contains(bearingOffset)) {
                return true;
            }
        }

        return false;
    }

}
