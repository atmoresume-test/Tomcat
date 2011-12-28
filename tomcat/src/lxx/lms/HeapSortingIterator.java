/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.lms;

import lxx.data_analysis.DataPoint;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HeapSortingIterator<E extends DataPoint> implements Iterator<E> {

    private final E[] array;
    private final Comparator<E> cmp;

    private int i;

    public HeapSortingIterator(E[] array, Comparator<E> cmp) {
        this.array = array;
        this.cmp = cmp;

        for (int i = array.length / 2; i >= 0; i--) {
            downHeap(i, array.length);
        }
        i = array.length - 1;
    }

    public boolean hasNext() {
        return i >= -1;
    }

    public E next() {
        if (i > 0) {
            doSortStep();
        }
        if (i < 0) {
            throw new NoSuchElementException();
        }
        E res = array[i];
        i--;
        return res;
    }

    private void doSortStep() {
        final E temp = array[i];
        array[i] = array[0];
        array[0] = temp;

        downHeap(0, i);
    }

    private void downHeap(int k, int n) {
        E newElem = array[k];
        int child;

        while (k < n / 2) {
            child = (2 * k) + 1;

            if (child < n - 1 && cmp.compare(array[child], array[child + 1]) <= 0) {
                child++;
            }
            if (cmp.compare(newElem, array[child]) > 0) {
                break;
            }
            array[k] = array[child];
            k = child;
        }
        array[k] = newElem;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
