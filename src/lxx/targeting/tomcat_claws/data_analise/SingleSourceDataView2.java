/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.data_analysis.LocationFactory;
import lxx.data_analysis.kd_tree.GunKdTreeEntry;
import lxx.data_analysis.kd_tree.KdTreeAdapter;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.IntervalDouble;
import lxx.utils.IntervalLong;

import java.util.*;

import static java.lang.Math.sqrt;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView2 implements DataView {

    private final KdTreeAdapter<GunKdTreeEntry> dataSource;
    private final double[] weights;
    private final String name;

    public SingleSourceDataView2(Attribute[] attributes, double[] weights, String name) {
        this.weights = weights;
        this.name = name;
        dataSource = new KdTreeAdapter<GunKdTreeEntry>(attributes, 50000);
    }

    public Collection<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final GunKdTreeEntry[] similarEntries = dataSource.getNearestNeighbours(ts);

        final LinkedList<IntervalLong> coveredTimeIntervals = new LinkedList<IntervalLong>();
        final List<TurnSnapshot> dataSet = new LinkedList<TurnSnapshot>();
        for (GunKdTreeEntry e : similarEntries) {
            boolean contained = false;
            final int eRoundTime = e.ts.roundTime;
            for (IntervalLong ival : coveredTimeIntervals) {
                if (ival.contains(eRoundTime)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                dataSet.add(e.ts);
                coveredTimeIntervals.add(new IntervalLong(eRoundTime - 10, eRoundTime + 10));
            }
        }

        return dataSet;
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new GunKdTreeEntry(LocationFactory.getWeightedLocation(ts, dataSource.getAttributes(), weights), ts, dataSource.getAttributes()));
    }

    public String getName() {
        return name;
    }

    private static class DistTimeComparator implements Comparator<GunKdTreeEntry> {

        public int compare(GunKdTreeEntry o1, GunKdTreeEntry o2) {
            return Double.compare(o1.normalWeightedDistance, o2.normalWeightedDistance);
        }
    }

}
