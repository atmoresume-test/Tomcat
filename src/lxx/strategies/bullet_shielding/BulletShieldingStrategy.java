package lxx.strategies.bullet_shielding;

import lxx.MySnapshot;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.strategies.FirePowerSelector;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.Movement;
import lxx.strategies.duel.DuelStrategy;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.APoint;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * User: Aleksey Zhidkov
 * Date: 21.06.12
 */
public class BulletShieldingStrategy extends DuelStrategy {

    private LXXBullet bulletToIntercept;

    public BulletShieldingStrategy(Tomcat robot, Movement withBulletsMovement, Gun gun, FirePowerSelector firePowerSelector, TargetManager targetManager, EnemyBulletManager enemyBulletManager, Office office) {
        super(robot, withBulletsMovement, gun, firePowerSelector, targetManager, enemyBulletManager, office);
    }

    @Override
    public boolean match() {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(0);
        if (bulletsOnAir.size() == 0) {
            return false;
        }

        bulletToIntercept = bulletsOnAir.get(0);

        return super.match() && bulletToIntercept.getBullet().getPower() > 0.2 &&
                (target == null || target.getEnergy() > robot.getEnergy() * 1.2);

    }

    @Override
    protected GunDecision getGunDecision(Target target, double firePower) {
        firePower = 0.1;
        final double ebSpeed = bulletToIntercept.getSpeed();

        final MySnapshot currentSnapshot = robot.getCurrentSnapshot();
        double ebTravelledDist = bulletToIntercept.getTravelledDistance() + ebSpeed * currentSnapshot.getTurnsToGunCool();
        final LXXPoint ebFirePos = bulletToIntercept.getFirePosition();
        final double ebHeading = ebFirePos.angleTo(wsMovement.getDestination());
        final APoint firePos = currentSnapshot.project(currentSnapshot.getAbsoluteHeadingRadians(), currentSnapshot.getSpeed() * currentSnapshot.getTurnsToGunCool());

        final double mbSpeed = Rules.getBulletSpeed(0.1);
        double mbTravelledDist = 0;

        List<Object[]> debug = new LinkedList<Object[]>();
        double alpha;
        APoint[] intersection;
        while (true) {
            final APoint ebPos = ebFirePos.project(ebHeading, ebTravelledDist);
            final APoint ebNextPos = ebFirePos.project(ebHeading, ebTravelledDist + ebSpeed);
            final double mbNextDist = mbTravelledDist + mbSpeed;

            APoint pnt1 = null;
            APoint pnt2 = null;
            if (firePos.aDistance(ebPos) > mbNextDist && firePos.aDistance(ebNextPos) < mbTravelledDist) {
                pnt1 = LXXUtils.intersection(ebPos, ebNextPos, firePos, mbTravelledDist)[0];
                pnt2 = LXXUtils.intersection(ebPos, ebNextPos, firePos, mbNextDist)[0];
            } else if (firePos.aDistance(ebPos) < mbNextDist && firePos.aDistance(ebNextPos) > mbTravelledDist) {
                pnt1 = ebPos;
                pnt2 = ebNextPos;
            } else if (firePos.aDistance(ebPos) > mbNextDist && firePos.aDistance(ebNextPos) < mbNextDist) {
                pnt1 = ebNextPos;
                pnt2 = LXXUtils.intersection(ebPos, ebNextPos, firePos, mbNextDist)[0];
            } else if (firePos.aDistance(ebPos) < mbNextDist && firePos.aDistance(ebNextPos) < mbTravelledDist) {
                pnt1 = ebPos;
                pnt2 = LXXUtils.intersection(ebPos, ebNextPos, firePos, mbTravelledDist)[0];
            }

            /*debug.add(new Object[]{pnt1, pnt2, mbTravelledDist, ebTravelledDist});
            if (ebTravelledDist > 1700) {
                for (Object[] o : debug) {
                    final Double enemyBulletTravelledDistance = (Double) o[3];
                    final Double myBulletTravelledDistance = (Double) o[2];
                    final Double totalDistance = ebFirePos.aDistance(firePos);
                    final Double myDistToPnt1 = firePos.aDistance((APoint) o[0]);
                    final Double myDistToPnt2 = firePos.aDistance((APoint) o[1]);

                    System.out.println(enemyBulletTravelledDistance);
                }
                break;
            }*/

            ebTravelledDist += ebSpeed;
            mbTravelledDist += mbSpeed;
            if (pnt1 != null) {
                final LXXGraphics g = robot.getLXXGraphics();
                g.setColor(new Color(0, 255, 0, 100));
                g.drawCircle(firePos, mbTravelledDist);
                g.setColor(new Color(255, 0, 0, 100));
                g.fillCircle(pnt1, 5);
                g.fillCircle(pnt2, 5);

                alpha = Utils.normalAbsoluteAngle((firePos.angleTo(pnt1) + firePos.angleTo(pnt2)) / 2);
                g.setColor(new Color(255, 0, 0, 100));
                g.drawLine(firePos, firePos.project(alpha, mbTravelledDist + mbSpeed));
                intersection = new APoint[]{pnt1, pnt2};
                break;
            }
        }

        robot.getLXXGraphics().drawCircle(ebFirePos, bulletToIntercept.getTravelledDistance());

        return new GunDecision(Utils.normalRelativeAngle(alpha - robot.getGunHeadingRadians()), new BSAimingPredictionData(firePos, intersection, bulletToIntercept));
    }

    @Override
    protected double selectFirePower(Target target) {
        if (bulletToIntercept.getFlightTime(wsMovement.getDestination()) < robot.getTurnsToGunCool() + 3) {
            return 0;
        }
        return 0.1;
    }
}
