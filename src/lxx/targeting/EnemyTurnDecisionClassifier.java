package lxx.targeting;

import lxx.EnemySnapshot;
import lxx.data_analysis.kd_tree.GunKdTreeEntry;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.*;

/**
 * User: Aleksey Zhidkov
 * Date: 13.06.12
 */
public class EnemyTurnDecisionClassifier {

    private final BufferEntry[][] buffer = new BufferEntry[3][3];

    private final KdTreeAdapter<NormalEnemyTurnDecision> tree;
    private final Attribute[] attrs;

    public EnemyTurnDecisionClassifier(Attribute[] attrs, int sizeLimit) {
        this.attrs = attrs;
        tree = new KdTreeAdapter<NormalEnemyTurnDecision>(this.attrs, sizeLimit);

        for (int i = 0; i < buffer.length; i++) {
            for (int j = 0; j < buffer[i].length; j++) {
                buffer[i][j] = new BufferEntry();
            }
        }
    }

    public double[] classify(TurnSnapshot ts) {
        final GunKdTreeEntry[] neighbours = tree.getNearestNeighbours(ts);

        final int[] accVotes = new int[4];
        final int[] trVotes = new int[21];

        for (GunKdTreeEntry e : neighbours) {
            final NormalEnemyTurnDecision payload = (NormalEnemyTurnDecision) e.payload;
            accVotes[((int) round(payload.acceleration + 1))]++;
            trVotes[((int) round(payload.turnRateRadians + 1))]++;
        }

        int acc = 0;
        for (int i = 1; i < accVotes.length; i++) {
            if (accVotes[i] > accVotes[acc]) {
                acc = i;
            }
        }

        int tr = 0;
        for (int i = 1; i < trVotes.length; i++) {
            if (trVotes[i] > trVotes[tr]) {
                tr = i;
            }
        }

        return new double[]{(acc - 1) * ts.enemySnapshot.getLastDirection(), (tr - 10) * signum(ts.getAttrValue(AttributesManager.enemyBearingToMe))};
    }

    public void learn(TurnSnapshot prev, TurnSnapshot next) {
        final NormalEnemyTurnDecision etd = getEnemyTurnDecision(prev, next);
        buffer[getAccelerationClass(prev)][getSpeedClass(prev)].buffer.add(etd);

        flushBuffer();
    }

    private void flushBuffer() {
        int minEntries = Integer.MAX_VALUE;

        for (BufferEntry[] buffersArray : buffer) {
            for (BufferEntry buffer : buffersArray) {
                if (buffer.size() > 0) {
                    minEntries = min(minEntries, buffer.size());
                }
            }
        }

        final int bufferLimit = minEntries / 2;

        for (BufferEntry[] buffersArray : buffer) {
            for (BufferEntry buffer : buffersArray) {
                while (buffer.usedEntries < bufferLimit && buffer.hasNext()) {
                    tree.addEntry(buffer.nextEntry());
                }
            }
        }
    }

    private NormalEnemyTurnDecision getEnemyTurnDecision(TurnSnapshot prev, TurnSnapshot next) {
        final EnemySnapshot nextEs = next.enemySnapshot;
        final EnemySnapshot prevEs = prev.enemySnapshot;

        final double accel = (nextEs.getVelocity() - prevEs.getVelocity()) * prevEs.getLastDirection();
        final double turnRateRadians = nextEs.getTurnRateRadians() * signum(prev.getAttrValue(AttributesManager.enemyBearingToMe));

        return new NormalEnemyTurnDecision(prev, attrs, accel, turnRateRadians);
    }

    private static int getAccelerationClass(TurnSnapshot ts) {
        return (int) signum(ts.enemySnapshot.getAcceleration());
    }

    private static int getSpeedClass(TurnSnapshot ts) {
        switch ((int) ts.enemySnapshot.getSpeed()) {
            case 0:
                return 0;
            case 8:
                return 2;
            default:
                return 1;
        }
    }

    private static class NormalEnemyTurnDecision extends GunKdTreeEntry {

        private final double acceleration;
        private final double turnRateRadians;

        private NormalEnemyTurnDecision(TurnSnapshot ts, Attribute[] attrs, double acceleration, double turnRateRadians) {
            super(ts, attrs);
            this.acceleration = acceleration;
            this.turnRateRadians = turnRateRadians;
        }
    }

    private static class BufferEntry {

        public final Stack<NormalEnemyTurnDecision> buffer = new Stack<NormalEnemyTurnDecision>();
        public int usedEntries;

        public int size() {
            return usedEntries + buffer.size();
        }

        public NormalEnemyTurnDecision nextEntry() {
            usedEntries++;
            return buffer.pop();
        }

        public boolean hasNext() {
            return buffer.size() > 0;
        }
    }

}
