/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public class ArrTest {

    public static <T extends Number> void main(String[] args) {
        T[] arr1 = (T[]) new Number[]{};
        Integer[] arr2 = (Integer[]) arr1;
    }

}
