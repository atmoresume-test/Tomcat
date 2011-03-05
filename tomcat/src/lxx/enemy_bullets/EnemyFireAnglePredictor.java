/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets;

import lxx.fire_log.EntryMatch;
import lxx.fire_log.FireLog;
import lxx.fire_log.FireLogEntry;
import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.office.BattleSnapshotManager;
import lxx.targeting.Target;
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import lxx.wave.Wave;
import robocode.Rules;
import robocode.util.Utils;

import java.util.*;

import static java.lang.Math.signum;

public class EnemyFireAnglePredictor {

    private static final double A = 0.02;
    private static final int B = 20;

    private static final int FIRE_DETECTION_LATENCY = 1;
    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static final Map<String, FireLog<Double>> logs = new HashMap<String, FireLog<Double>>();

    private final Map<Wave, FireLogEntry<Double>> entriesByWaves = new HashMap<Wave, FireLogEntry<Double>>();

    private final BattleSnapshotManager battleSnapshotManager;

    public EnemyFireAnglePredictor(BattleSnapshotManager battleSnapshotManager) {
        this.battleSnapshotManager = battleSnapshotManager;
    }

    public void enemyFire(Wave wave) {
        FireLogEntry<Double> e = new FireLogEntry<Double>(new LXXPoint(wave.getSourceStateAtFireTime()), wave.getTargetStateAtFireTime(),
                battleSnapshotManager.getLastSnapshots((Target) wave.getSourceStateAtFireTime().getRobot(), FIRE_DETECTION_LATENCY).get(0));
        entriesByWaves.put(wave, e);
    }

    // todo(zhidkov): add flat movement
    @SuppressWarnings({"UnusedDeclaration"})
    public void updateWaveState(Wave w) {
        updateWaveState(w, w.getSourcePosAtFireTime().angleTo(w.getTargetStateAtFireTime().getRobot()));
    }

    // todo(zhidkov): rename
    public void updateWaveState(Wave w, double bulletHeading) {
        final double lateralVelocity = LXXUtils.lateralVelocity(w.getSourceStateAtFireTime(), w.getTargetStateAtFireTime(),
                w.getTargetStateAtFireTime().getVelocityModule(), w.getTargetStateAtFireTime().getAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);

        final Double bearingOffset = Utils.normalRelativeAngle(bulletHeading - w.getSourcePosAtFireTime().angleTo(w.getTargetPosAtFireTime())) * lateralDirection;
        addEntry(w, bearingOffset);
    }

    private void addEntry(Wave w, Double guessFactor) {
        final FireLog<Double> log = getLog(w.getSourceStateAtFireTime().getRobot().getName());

        final FireLogEntry<Double> entry = entriesByWaves.get(w);
        entry.result = guessFactor;
        log.addEntry(entry);
    }

    public AimingPredictionData getPredictionData(Target t) {
        final FireLog<Double> log = getLog(t.getName());

        final BattleSnapshot predicate = battleSnapshotManager.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        final List<Double> bearingOffsets = getBearingOffsets(log, predicate, t.getFirePower());

        final int bearingOffsetsCount = bearingOffsets.size();
        final Map<Double, Double> bearingOffsetDangers = new TreeMap<Double, Double>();
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            if (bearingOffsetsCount > 0) {
                for (Double bulletBearingOffset : bearingOffsets) {
                    // this is empirical selected formula, which
                    // produce smooth peaks for bearing offsets with predicted bullets
                    final double difference = bulletBearingOffset - wavePointBearingOffset;
                    final double differenceSquare = difference * difference;
                    final double bearingOffsetsDifference = differenceSquare + A;
                    bearingOffsetDanger += 1D / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return new EnemyAimingPredictionData(bearingOffsetDangers);

    }

    private List<Double> getBearingOffsets(FireLog<Double> log, BattleSnapshot predicate, double firePower) {
        final List<EntryMatch<Double>> matches = log.getSimilarEntries(predicate, 1);
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getMyVelocityModule(), predicate.getMyAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);
        final List<Double> bearingOffsets = new LinkedList<Double>();
        if (matches.size() > 0) {
            for (EntryMatch<Double> match : matches) {
                if (bearingOffsets.size() > 0) {
                    break;
                }

                bearingOffsets.add(match.result * lateralDirection);
            }
        } else {
            final double maxEscapeAngle = LXXUtils.getMaxEscapeAngle(Rules.getBulletSpeed(firePower));
            bearingOffsets.add(maxEscapeAngle * lateralDirection);
        }

        return bearingOffsets;
    }

    private static FireLog<Double> getLog(String enemyName) {
        FireLog<Double> log = logs.get(enemyName);
        if (log == null) {
            log = createLog();
            logs.put(enemyName, log);
        }
        return log;
    }

    private static FireLog<Double> createLog() {
        final Attribute[] splitAttributes = {
                AttributesManager.myLateralVelocity, AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
        };
        return new FireLog<Double>(splitAttributes, 2, 0.02);
    }

}