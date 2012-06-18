package lxx.plugins;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;
import robocode.RobocodeFileOutputStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * User: jdev
 * Date: 15.06.12
 */
public class LogDumper implements Plugin, BulletManagerListener, WaveCallback {

    private static ObjectOutputStream objectOutputStream;

    private TurnSnapshotsLog turnSnapshotsLog;
    private TargetManager targetManager;
    private Tomcat robot;
    private WaveManager waveManager;

    public void roundStarted(Office office) {
        turnSnapshotsLog = office.getTurnSnapshotsLog();
        targetManager = office.getTargetManager();
        robot = office.getRobot();
        waveManager = office.getWaveManager();
        office.getBulletManager().addListener(this);
    }

    public void battleEnded() {
        try {
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tick() {
        if (targetManager.hasDuelOpponent()) {
            final Target duelOpponent = targetManager.getDuelOpponent();

            if (objectOutputStream == null) {
                try {
                    objectOutputStream = new ObjectOutputStream(new RobocodeFileOutputStream(robot.getDataFile(robot.getName() + "-" + duelOpponent.getName() + "-" + System.currentTimeMillis())));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            try {
                objectOutputStream.writeObject(turnSnapshotsLog.getLastSnapshot(duelOpponent));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void bulletFired(LXXBullet bullet) {
        waveManager.addCallback(this, bullet.getWave());
    }

    public void waveBroken(final Wave w) {
        try {
            final HashMap<TurnSnapshot, Double> wm = new HashMap<TurnSnapshot, Double>();
            wm.put(w.getCarriedBullet().getAimPredictionData().getTs(), w.getHitBearingOffsetInterval().center());
            objectOutputStream.writeObject(wm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bulletHit(LXXBullet bullet) {}

    public void bulletMiss(LXXBullet bullet) {}

    public void bulletIntercepted(LXXBullet bullet) {}

    public void bulletPassing(LXXBullet bullet) {}

    public void wavePassing(Wave w) {}

}
