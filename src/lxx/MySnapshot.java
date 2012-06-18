package lxx;

import lxx.bullets.BulletSnapshot;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class MySnapshot extends RobotSnapshot {

    private static final long serialVersionUID = 6973016020185992456L;

    private List<BulletSnapshot> bullets;
    private final double gunCoolingRate;
    private final double last10TicksDist;
    private double gunHeadingRadians;

    public MySnapshot(BasicRobot currentState) {
        super(currentState);
        last10TicksDist = 0;
        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
        gunHeadingRadians = currentState.getGunHeadingRadians();
    }

    public MySnapshot(MySnapshot prevState, BasicRobot currentState, double last10TicksDist) {
        super(prevState, currentState);
        this.last10TicksDist = last10TicksDist;

        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
        gunHeadingRadians = currentState.getGunHeadingRadians();
    }

    public MySnapshot(MySnapshot state1, MySnapshot state2, double interpolationK) {
        super(state1, state2, interpolationK);
        last10TicksDist = state1.getLast10TicksDist() + (state2.getLast10TicksDist() - state1.getLast10TicksDist()) * interpolationK;
        bullets = state2.getBulletsInAir();
        gunCoolingRate = state2.gunCoolingRate;
        gunHeadingRadians = state2.gunHeadingRadians;
    }

    public double getLast10TicksDist() {
        return last10TicksDist;
    }

    public double getAbsoluteHeadingRadians() {
        if (signum(velocity) == 1) {
            return headingRadians;
        } else if (signum(velocity) == -1) {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        } else if (lastDirection == 1) {
            return headingRadians;
        } else {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        }
    }

    public List<BulletSnapshot> getBulletsInAir() {
        return bullets;
    }

    public int getTurnsToGunCool() {
        return (int) round(gunHeat / gunCoolingRate);
    }

    public void setBullets(List<BulletSnapshot> bullets) {
        this.bullets = bullets;
    }

    public double getGunHeadingRadians() {
        return gunHeadingRadians;
    }
}
