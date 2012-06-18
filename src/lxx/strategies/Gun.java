/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;

/**
 * User: jdev
 * Date: 12.02.2011
 */
public interface Gun {

    GunDecision getGunDecision(TurnSnapshot ts, double firePower);

}
