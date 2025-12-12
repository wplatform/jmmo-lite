package com.github.azeroth.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Pair<K, V> {
    private K first;
    private V second;


    public void first(K first) {
        this.first = first;
    }

    public void second(V second) {
        this.second = second;
    }

    public K first() {
        return first;
    }

    public V second() {
        return second;
    }
}
