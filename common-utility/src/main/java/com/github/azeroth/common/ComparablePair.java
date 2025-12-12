package com.github.azeroth.common;

public class ComparablePair<K extends Comparable<K>, V extends Comparable<V>> extends Pair<K, V> implements Comparable<ComparablePair<K, V>>{
    @Override
    public int compareTo(ComparablePair<K, V> o) {
        int result = this.first.compareTo(o.first);
        if (result == 0) {
            result = this.second.compareTo(o.second);
        }
        return result;
    }
}
