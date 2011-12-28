/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.bullets.enemy.BearingOffsetDanger;

import java.util.List;

public class LogPrediction {

    private final List<BearingOffsetDanger> bearingOffsets;

    public boolean used = false;

    public LogPrediction(List<BearingOffsetDanger> bearingOffsets) {
        this.bearingOffsets = bearingOffsets;
    }

    public List<BearingOffsetDanger> getBearingOffsets() {
        return bearingOffsets;
    }
}
