package lxx.strategies.bullet_shielding;

import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.ts_log.TurnSnapshot;
import lxx.utils.APoint;
import lxx.utils.AimingPredictionData;

import java.awt.*;

import static java.lang.Math.max;
import static java.lang.Math.random;

/**
 * User: jdev
 * Date: 21.06.12
 */
public class BSAimingPredictionData implements AimingPredictionData {

    private final APoint firePos;
    private final APoint[] intersection;
    private final LXXBullet interceptBullet;

    public BSAimingPredictionData(APoint firePos, APoint[] intersection, LXXBullet interceptBullet) {
        this.firePos = firePos;
        this.intersection = intersection;
        this.interceptBullet = interceptBullet;
    }

    public void paint(LXXGraphics g, LXXBullet bullet) {
        if (max(firePos.aDistance(intersection[0]), firePos.aDistance(intersection[1])) < bullet.getTravelledDistance()) {
            return;
        }
        g.setColor(new Color((int) (255 * random()), (int) (255 * random()), (int) (255 * random()), 150));
        g.drawCircle(bullet.getFirePosition(), bullet.getTravelledDistance());
        g.drawCircle(interceptBullet.getFirePosition(), interceptBullet.getTravelledDistance() - 10);
        g.fillCircle(firePos, 7);

        for (APoint iPnt : intersection) {
            g.fillCircle(iPnt, 3);
        }

        g.drawLine(intersection[0], intersection[1]);
    }

    public TurnSnapshot getTs() {
        return null;
    }

    public long getPredictionRoundTime() {
        return 0;
    }

}
