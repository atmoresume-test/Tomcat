/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.gun.enemy;

import lxx.gun.LogEfficienciesFactory;
import lxx.gun.LogEfficiency;

public class EnemyLogsEfficiencyFactory implements LogEfficienciesFactory {
    public LogEfficiency[] createEfficiencies() {
        return new LogEfficiency[]{new EnemyHitRate(), new LogHitMissRate(9), new LogHitMissRate(45), new LogHitMissRate(5000)};
    }
}
