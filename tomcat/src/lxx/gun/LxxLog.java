/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun;

import lxx.data_analysis.LxxDataPoint;

public abstract class LxxLog<E extends LxxDataPoint> implements Log<E> {

    private long lastUpdateRoundTime;
    private boolean enabled;

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
}
