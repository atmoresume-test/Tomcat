/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.utils.APoint;

/**
 * User: jdev
 * Date: 12.02.2011
 */
public interface Movement {

    MovementDecision getMovementDecision();

    APoint getDestination();

}
