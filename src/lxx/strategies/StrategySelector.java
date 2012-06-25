/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import lxx.strategies.bullet_shielding.BulletShieldingStrategy;
import lxx.strategies.challenges.MCChallengerStrategy;
import lxx.strategies.challenges.TCChallengerStrategy;
import lxx.strategies.duel.DuelFirePowerSelector;
import lxx.strategies.duel.DuelStrategy;
import lxx.strategies.duel.WaveSurfingMovement;
import lxx.strategies.find_enemies.FindEnemiesStrategy;
import lxx.strategies.win.WinStrategy;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_claws.TomcatClaws;
import lxx.targeting.tomcat_eyes.TomcatEyes;

import java.util.ArrayList;
import java.util.List;

public class StrategySelector {

    private final List<Strategy> strategies = new ArrayList<Strategy>();

    private Strategy prevStrategy;

    public StrategySelector(Tomcat robot, Office office) {
        final TargetManager targetManager = office.getTargetManager();
        final EnemyBulletManager enemyBulletManager = office.getEnemyBulletManager();
        final TomcatEyes tomcatEyes = office.getTomcatEyes();
        enemyBulletManager.addListener(tomcatEyes);

        final TomcatClaws tomcatClaws = new TomcatClaws(robot, office.getTurnSnapshotsLog(), office.getDataViewManager());
        final WaveSurfingMovement wsm = new WaveSurfingMovement(office);
        office.getPaintManager().addPainter(wsm);

        strategies.add(new FindEnemiesStrategy(robot, targetManager, robot.getInitialOthers()));
        if ("TCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode"))) {
            strategies.add(new TCChallengerStrategy(robot, tomcatClaws, targetManager, office));
        }
        if ("MCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode"))) {
            strategies.add(new MCChallengerStrategy(robot, wsm, targetManager, enemyBulletManager));
        }

        final BulletShieldingStrategy bsStrategy = new BulletShieldingStrategy(robot,
                wsm,
                tomcatClaws,
                new DuelFirePowerSelector(office.getStatisticsManager()), targetManager, enemyBulletManager, office);
        strategies.add(bsStrategy);

        final DuelStrategy duelStrategy = new DuelStrategy(robot,
                wsm,
                tomcatClaws,
                new DuelFirePowerSelector(office.getStatisticsManager()), targetManager, enemyBulletManager, office);
        strategies.add(duelStrategy);

        final WinStrategy winStrategy = new WinStrategy(robot, targetManager, enemyBulletManager);
        office.getPaintManager().addPainter(winStrategy);
        strategies.add(winStrategy);
    }

    public Strategy selectStrategy() {
        for (Strategy s : strategies) {
            if (s.match()) {
                if (prevStrategy != s) {
                    System.out.println("New Strategy: " + s.getClass().getSimpleName());
                }
                prevStrategy = s;
                return s;
            }
        }

        return null;
    }

}
