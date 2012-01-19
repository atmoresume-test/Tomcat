/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.bullets.enemy.UndirectedGuessFactor;
import lxx.data_analysis.LxxDataPoint;
import lxx.data_analysis.r_tree.RTree;
import lxx.lms.HeapSortingIterator;
import lxx.lms.LxxLog;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.round;

public class EnemyGunRTreeLog extends LxxLog<LxxDataPoint<UndirectedGuessFactor>> {

    private static final LxxDataPoint<UndirectedGuessFactor> EXAMPLE = new LxxDataPoint<UndirectedGuessFactor>(null, null, null);
    private static final EnemyGunRTreeLog.RoundTimeDescComparator comparator = new RoundTimeDescComparator();

    private final RTree<LxxDataPoint<UndirectedGuessFactor>> rTree;
    private final Map<Attribute, Double> halfSideLength;
    private final LogType logType;

    public EnemyGunRTreeLog(Map<Attribute, Double> halfSideLength, LogType logType, Attribute... attrs) {
        super(attrs);
        this.halfSideLength = halfSideLength;
        this.logType = logType;
        rTree = new RTree<LxxDataPoint<UndirectedGuessFactor>>(attrs);
    }

    public void addEntry(LxxDataPoint<UndirectedGuessFactor> dataPoint) {
        super.addEntry(dataPoint);
        rTree.insert(dataPoint);
    }

    public Iterator<LxxDataPoint<UndirectedGuessFactor>> getRecordsIterator(TurnSnapshot query) {
        return new HeapSortingIterator<LxxDataPoint<UndirectedGuessFactor>>(rTree.rangeSearch(getRange(query), EXAMPLE),
                comparator);
    }

    private IntervalDouble[] getRange(TurnSnapshot center) {
        final IntervalDouble[] res = new IntervalDouble[attributes.length];
        int idx = 0;
        for (Attribute attr : attributes) {
            double delta = halfSideLength.get(attr);
            res[idx++] = new IntervalDouble((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                    (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta)));
        }

        return res;
    }

    public LogType getLogType() {
        return logType;
    }

    private static final class RoundTimeDescComparator implements Comparator<LxxDataPoint<UndirectedGuessFactor>> {

        public int compare(LxxDataPoint<UndirectedGuessFactor> o1, LxxDataPoint<UndirectedGuessFactor> o2) {
            return o1.ts.roundTime > o2.ts.roundTime ? -1 : 1;
        }
    }

    public enum LogType {
        HIT_LOG,
        VISIT_LOG
    }

}
