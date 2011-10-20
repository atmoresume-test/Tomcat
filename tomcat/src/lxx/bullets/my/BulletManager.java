/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.my;

import lxx.RobotListener;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.events.FireEvent;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.paint.LXXGraphics;
import lxx.utils.LXXPoint;
import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class BulletManager implements RobotListener {

    private static boolean paintEnabled = false;

    private final List<LXXBullet> oldBullets = new ArrayList<LXXBullet>();

    private final List<LXXBullet> bullets = new ArrayList<LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();

    private void addBullet(LXXBullet bullet) {
        bullets.add(bullet);
        for (BulletManagerListener listener : listeners) {
            listener.bulletFired(bullet);
        }
    }

    private void onBulletHitBullet(BulletHitBulletEvent e) {
        final LXXBullet b = getLXXBullet(e.getBullet());
        removeBullet(b);
        b.setState(LXXBulletState.INTERCEPTED);
        for (BulletManagerListener lst : listeners) {
            lst.bulletIntercepted(b);
        }
    }

    private void removeBullet(LXXBullet b) {
        bullets.remove(b);
        oldBullets.add(b);
    }

    private void onBulletHit(BulletHitEvent event) {
        final LXXBullet b = getLXXBullet(event.getBullet());
        if (b.getTarget().getName().equals(event.getName())) {
            for (BulletManagerListener listener : listeners) {
                listener.bulletHit(b);
            }
        } else {
            if (b.getTravelledDistance() >= b.getDistanceToTarget()) {
                for (BulletManagerListener listener : listeners) {
                    listener.bulletMiss(b);
                }
            }
        }
        b.setState(LXXBulletState.HITTED);
        removeBullet(b);
    }

    private void onBulletMissed(BulletMissedEvent event) {
        final LXXBullet b = getLXXBullet(event.getBullet());
        if (b.getTarget() != null && b.getTarget().isAlive()) {
            for (BulletManagerListener lst : listeners) {
                lst.bulletMiss(b);
            }
            b.setState(LXXBulletState.MISSED);
        }
        removeBullet(b);
    }

    public LXXBullet getLXXBullet(Bullet b) {
        for (LXXBullet bullet : bullets) {
            if (Utils.isNear(b.getHeadingRadians(), bullet.getBullet().getHeadingRadians()) &&
                    Utils.isNear(b.getPower(), bullet.getBullet().getPower()) &&
                    new LXXPoint(bullet.getBullet().getX(), bullet.getBullet().getY()).aDistance(new LXXPoint(b.getX(), b.getY())) < 40) {
                return bullet;
            }
        }

        return null;
    }

    public LXXBullet getFirstBullet() {
        if (bullets.size() == 0) {
            return null;
        }
        for (LXXBullet b : bullets) {
            if (b.getFirePosition().aDistance(b.getTarget()) > b.getTravelledDistance()) {
                return b;
            }
        }
        return null;
    }

    public void addListener(BulletManagerListener listener) {
        listeners.add(listener);
    }

    public void onEvent(Event event) {
        if (event instanceof BulletMissedEvent) {
            onBulletMissed((BulletMissedEvent) event);
        } else if (event instanceof BulletHitEvent) {
            onBulletHit((BulletHitEvent) event);
        } else if (event instanceof BulletHitBulletEvent) {
            onBulletHitBullet((BulletHitBulletEvent) event);
        } else if (event instanceof FireEvent) {
            addBullet(((FireEvent) event).getBullet());
        } else if (event instanceof LXXPaintEvent && bullets.size() > 0 && paintEnabled) {
            final LXXBullet firstBullet = getFirstBullet();
            if (firstBullet == null) {
                return;
            }
            LXXGraphics g = ((LXXPaintEvent) event).getGraphics();
            firstBullet.getAimPredictionData().paint(g, firstBullet);
        } else if (event instanceof LXXKeyEvent) {
            if (Character.toUpperCase(((LXXKeyEvent) event).getKeyChar()) == 'G') {
                paintEnabled = !paintEnabled;
            }
        }
    }

    public List<LXXBullet> getBullets() {
        return new LinkedList<LXXBullet>(bullets);
    }

}
