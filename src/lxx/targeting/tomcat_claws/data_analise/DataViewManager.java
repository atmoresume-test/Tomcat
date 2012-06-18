/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import robocode.Event;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class DataViewManager implements RobotListener {

    private static final DataView mainDataView1 = new SingleSourceDataView2(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyTurnRate,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.lastVisitedGF1,
            AttributesManager.lastVisitedGF2,
    }, new double[]{0.94, 0.57, 0.95, 0.15, 0.07, 0.38}, "Main");

    private static final DataView mainDataView2 = new SingleSourceDataView2(new Attribute[]{
            AttributesManager.enemySpeed,
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyTimeSinceLastDirChange,
    }, new double[]{0.65, 0.49, 0.43, 0.79, 0.03}, "Main");

    /*private static final DataView asDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.enemyBearingOffsetOnSecondBullet,
    }, new double[]{0.75, 0.25}, "Anti-surfer #1");

    private static final DataView asDataView2 = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.firstBulletFlightTimeToEnemy,
            AttributesManager.lastVisitedGF1,
            AttributesManager.lastVisitedGF2,
    }, new double[]{0.75, 0.25}, "Anti-surfer #2");

    private static final DataView distanceDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.distBetween,
            AttributesManager.enemyTurnRate
    }, new double[]{0.5, 0.5}, "Distance");

    private static final DataView timeSinceDirChangeDataView = new SingleSourceDataView(new Attribute[]{
            AttributesManager.enemyAcceleration,
            AttributesManager.enemySpeed,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyTimeSinceLastDirChange,
            AttributesManager.enemyTurnRate
    }, new double[]{0.5, 0.5}, "Time since dir change");*/

    private DataView[] duelViews = {mainDataView1, mainDataView2/*, asDataView, asDataView2, distanceDataView, timeSinceDirChangeDataView*/};

    private final TargetManager targetManager;
    private final TurnSnapshotsLog turnSnapshotLog;

    public DataViewManager(TargetManager targetManager, TurnSnapshotsLog turnSnapshotLog) {
        this.targetManager = targetManager;
        this.turnSnapshotLog = turnSnapshotLog;
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Target t : targetManager.getAliveTargets()) {
                final TurnSnapshot lastSnapshot = turnSnapshotLog.getLastSnapshot(t);
                for (DataView view : duelViews) {
                    view.addEntry(lastSnapshot);
                }
            }
        }
    }

    public DataView[] getDuelDataViews() {
        return duelViews;
    }

}
