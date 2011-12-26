/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun;

import lxx.data_analysis.DataPoint;
import lxx.ts_log.TurnSnapshot;

import java.util.Iterator;

public interface Log<E extends DataPoint> {

    void addEntry(E dataPoint);

    Iterator<E> getRecordsIterator(TurnSnapshot query);

}
