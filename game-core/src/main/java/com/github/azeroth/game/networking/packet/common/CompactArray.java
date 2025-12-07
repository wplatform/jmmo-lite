package com.github.azeroth.game.networking.packet.common;

import com.badlogic.gdx.utils.IntMap;
import lombok.Getter;

@Getter
public class CompactArray<T> {
    // 32-bit mask (uint32 in C++)
    private int mask;

    // contents stored in a compact vector-like list
    private final IntMap<T> contents = new IntMap<>(10);

    public T get(int index) {
        return contents.get(index, null);
    }

    public void insert(int index, T value) {
        // set bit in mask
        mask |= (1 << index);
        contents.put(index, value);
    }

    public void clear() {
        mask = 0;
        contents.clear();
    }
}
