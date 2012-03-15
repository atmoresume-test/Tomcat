/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 05.08.2010
 */
public class MyHeading implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(me.getState().getAbsoluteHeadingRadians());
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return toDegrees(me.getAbsoluteHeadingRadians());
    }
}
