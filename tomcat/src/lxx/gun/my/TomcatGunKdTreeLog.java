/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.my;

import lxx.data_analysis.kd_tree.GunKdTreeEntry;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.gun.HeapSortingIterator;
import lxx.gun.LxxLog;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.IntervalLong;

import java.util.Comparator;
import java.util.Iterator;

import static java.lang.Math.sqrt;

public class TomcatGunKdTreeLog extends LxxLog<GunKdTreeEntry> {

    private static final DistTimeComparator comparator = new DistTimeComparator();

    private final KdTreeAdapter<GunKdTreeEntry> kdTree;
    private final double[] weights;

    public TomcatGunKdTreeLog(Attribute[] attributes, double[] weights) {
        this.weights = weights;
        kdTree = new KdTreeAdapter<GunKdTreeEntry>(attributes, 50000);
    }

    public void addEntry(GunKdTreeEntry dataPoint) {
        super.addEntry(dataPoint);
        kdTree.addEntry(dataPoint);
    }

    public Iterator<GunKdTreeEntry> getRecordsIterator(TurnSnapshot query) {
        final GunKdTreeEntry[] similarEntries = kdTree.getNearestNeighbours(query);
        final IntervalLong timeInterval = new IntervalLong(Integer.MAX_VALUE, Integer.MIN_VALUE);
        final IntervalDouble distInterval = new IntervalDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (GunKdTreeEntry entry : similarEntries) {
            final int timeDiff = query.roundTime - entry.ts.roundTime;
            timeInterval.extend(timeDiff);
            distInterval.extend(entry.distance);
        }

        for (GunKdTreeEntry e : similarEntries) {
            final double timeDist = (e.ts.roundTime - timeInterval.a) / (timeInterval.getLength()) * weights[0];
            final double locDist = (e.distance - distInterval.a) / (distInterval.getLength()) * weights[1];
            e.normalWeightedDistance = sqrt(timeDist * timeDist + locDist * locDist);
        }

        return new HeapSortingIterator<GunKdTreeEntry>(similarEntries, comparator);
    }

    private static class DistTimeComparator implements Comparator<GunKdTreeEntry> {

        public int compare(GunKdTreeEntry o1, GunKdTreeEntry o2) {
            return Double.compare(o1.normalWeightedDistance, o2.normalWeightedDistance);
        }
    }

}
