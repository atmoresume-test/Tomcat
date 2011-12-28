/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.data_analysis.DataPoint;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

import java.util.Iterator;

public interface Log<E extends DataPoint> {

    void addEntry(E dataPoint);

    Iterator<E> getRecordsIterator(TurnSnapshot query);

    long getLastUpdateRoundTime();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Attribute[] getAttributes();

}
