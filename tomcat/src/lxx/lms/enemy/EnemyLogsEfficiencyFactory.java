/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms.enemy;

import lxx.lms.LogEfficienciesFactory;
import lxx.lms.LogEfficiency;

public class EnemyLogsEfficiencyFactory implements LogEfficienciesFactory {

    public int getEfficienciesCount() {
        return 3;
    }

    public LogEfficiency[] createEfficiencies() {
        return new LogEfficiency[]{new LogHitMissRate(9), new LogHitMissRate(45), new LogHitMissRate(5000)};
    }

}
