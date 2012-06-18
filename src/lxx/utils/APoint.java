/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.io.Serializable;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public interface APoint extends Serializable {

    double getX();

    double getY();

    double aDistance(APoint p);

    double angleTo(APoint pnt);

    APoint project(double alpha, double distance);

    APoint project(DeltaVector dv);
}
