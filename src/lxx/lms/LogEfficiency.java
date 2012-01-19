/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.bullets.LXXBullet;

public interface LogEfficiency<T> extends Comparable<T> {

    void update(LXXBullet bullet, LogPrediction prediction);

}
