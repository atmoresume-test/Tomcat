/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.enemy;

import lxx.bullets.enemy.UndirectedGuessFactor;
import lxx.data_analysis.LxxDataPoint;
import lxx.data_analysis.r_tree.RTree;
import lxx.gun.HeapSortingIterator;
import lxx.gun.Log;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.round;

public class EnemyGunRTreeLog implements Log<LxxDataPoint<UndirectedGuessFactor>> {

    private static final LxxDataPoint<UndirectedGuessFactor> EXAMPLE = new LxxDataPoint<UndirectedGuessFactor>(null, null, null);
    private static final EnemyGunRTreeLog.RoundTimeDescComparator comparator = new RoundTimeDescComparator();

    private final Attribute[] attrs;
    private final RTree<LxxDataPoint<UndirectedGuessFactor>> rTree;
    private final Map<Attribute, Double> halfSideLength;

    private long lastUpdateRoundTime;

    public EnemyGunRTreeLog(Map<Attribute, Double> halfSideLength) {
        this.halfSideLength = halfSideLength;
        attrs = new Attribute[halfSideLength.size()];
        halfSideLength.keySet().toArray(attrs);
        rTree = new RTree<LxxDataPoint<UndirectedGuessFactor>>(attrs);
    }

    public void addEntry(LxxDataPoint<UndirectedGuessFactor> dataPoint) {
        rTree.insert(dataPoint);
        lastUpdateRoundTime = dataPoint.ts.roundTime;
    }

    public Iterator<LxxDataPoint<UndirectedGuessFactor>> getRecordsIterator(TurnSnapshot query) {
        return new HeapSortingIterator<LxxDataPoint<UndirectedGuessFactor>>(rTree.rangeSearch(getRange(query), EXAMPLE),
                comparator);
    }

    private IntervalDouble[] getRange(TurnSnapshot center) {
        final IntervalDouble[] res = new IntervalDouble[attrs.length];
        int idx = 0;
        for (Attribute attr : attrs) {
            double delta = halfSideLength.get(attr);
            res[idx++] = new IntervalDouble((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                    (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta)));
        }

        return res;
    }

    public long getLastUpdateRoundTime() {
        return lastUpdateRoundTime;
    }

    private static final class RoundTimeDescComparator implements Comparator<LxxDataPoint<UndirectedGuessFactor>> {

        public int compare(LxxDataPoint<UndirectedGuessFactor> o1, LxxDataPoint<UndirectedGuessFactor> o2) {
            return o1.ts.roundTime > o2.ts.roundTime ? -1 : 1;
        }
    }

}
