/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.data_analysis.DataPoint;

public interface FireAngleReconstructor<E extends DataPoint> {

    BearingOffsetDanger[] getBearingOffsets(E logRecord);

}
