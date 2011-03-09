/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.bullets;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public interface BulletManagerListener {

    void bulletHit(LXXBullet bullet);

    void bulletMiss(LXXBullet bullet);

    void bulletIntercepted(LXXBullet bullet);

}
