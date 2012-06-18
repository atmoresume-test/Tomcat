/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.data_analysis;

import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;

public class LocationFactory {

     private LocationFactory() {
     }

     public static double[] getPlainLocation(TurnSnapshot ts, Attribute[] attrs) {
        return getLocationImpl(ts, attrs, false, null);
    }

    public static double[] getNormalLocation(TurnSnapshot ts, Attribute[] attrs) {
        return getLocationImpl(ts, attrs, true, null);
    }

    private static double[] getLocationImpl(TurnSnapshot ts, Attribute[] attrs, boolean normalise, double[] weights) {
        final double[] location = new double[attrs.length];

        for (int i = 0; i < attrs.length; i++) {
            location[i] = ts.getAttrValue(attrs[i]) / (normalise ? attrs[i].maxRange.getLength() : 1) *
                    (weights != null ? weights[i] : 1);
        }

        return location;
    }

    public static double[] getWeightedLocation(TurnSnapshot ts, Attribute[] attributes, double[] weights) {
        return getLocationImpl(ts, attributes, true, weights);
    }
}
