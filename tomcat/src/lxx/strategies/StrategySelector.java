/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.strategies.challenges.TCChallengerStrategy;
import lxx.targeting.TargetManager;
import lxx.strategies.duel.AdvancedTargetersMovement;
import lxx.strategies.duel.DuelFirePowerSelector;
import lxx.strategies.duel.DuelStrategy;
import lxx.strategies.duel.SimpleTargetersMovement;
import lxx.strategies.find_enemies.FindEnemiesStrategy;
import lxx.strategies.win.WinStrategy;
import lxx.targeting.tomcat_claws.TomcatClaws;
import lxx.targeting.tomcat_eyes.TomcatEyes;

import java.util.ArrayList;
import java.util.List;

public class StrategySelector {

    private final List<Strategy> strategies = new ArrayList<Strategy>();

    public StrategySelector(Tomcat robot, Office office) {
        final TargetManager targetManager = office.getTargetManager();
        final EnemyBulletManager enemyBulletManager = office.getEnemyBulletManager();
        final TomcatEyes tomcatEyes = new TomcatEyes(robot);
        targetManager.addListener(tomcatEyes);
        enemyBulletManager.addListener(tomcatEyes);

        final TomcatClaws tomcatClaws = new TomcatClaws(robot, office.getTurnSnapshotsLog(), office.getDataViewManager().getDuelDataView());

        strategies.add(new FindEnemiesStrategy(robot, targetManager, robot.getInitialOthers()));
        strategies.add(new TCChallengerStrategy(robot, tomcatClaws, targetManager));

        final DuelStrategy waveSurfingDuelStrategy = new DuelStrategy(robot,
                new SimpleTargetersMovement(robot, targetManager, enemyBulletManager, tomcatEyes),
                new AdvancedTargetersMovement(robot, targetManager, enemyBulletManager, tomcatEyes),
                tomcatClaws,
                new DuelFirePowerSelector(tomcatEyes), targetManager, enemyBulletManager, tomcatEyes);
        strategies.add(waveSurfingDuelStrategy);

        strategies.add(new WinStrategy(robot, targetManager, enemyBulletManager));
    }

    public Strategy selectStrategy() {
        for (Strategy s : strategies) {
            if (s.match()) {
                return s;
            }
        }

        return null;
    }

}
