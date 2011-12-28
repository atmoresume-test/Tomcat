/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.data_analysis.LxxDataPoint;
import lxx.ts_log.attributes.Attribute;

public abstract class LxxLog<E extends LxxDataPoint> implements Log<E> {

    protected final Attribute[] attributes;

    private long lastUpdateRoundTime;
    private boolean enabled;

    protected LxxLog(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public void addEntry(E dataPoint) {
        lastUpdateRoundTime = dataPoint.ts.roundTime;
    }

    public long getLastUpdateRoundTime() {
        return lastUpdateRoundTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }
}
