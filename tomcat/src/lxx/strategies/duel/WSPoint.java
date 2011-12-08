/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;

import static java.lang.Math.signum;

class WSPoint extends LXXPoint implements Comparable<WSPoint> {

    public final PointDanger pointDanger;
    public OrbitDirection orbitDirection;
    public boolean isFirst;
    public boolean isLast;

    WSPoint(APoint point, PointDanger danger) {
        super(point);
        this.pointDanger = danger;
    }

    public int compareTo(WSPoint o) {
        return (int) signum(pointDanger.danger - o.pointDanger.danger);
    }
}
