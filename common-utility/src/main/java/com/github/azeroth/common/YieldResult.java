package com.github.azeroth.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "ofNull")
public class YieldResult<T> {

    private T value;

    public boolean wasNull() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
