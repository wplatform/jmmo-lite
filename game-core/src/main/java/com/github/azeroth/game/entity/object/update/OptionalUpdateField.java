package com.github.azeroth.game.entity.object.update;


public class OptionalUpdateField<T> {
    private boolean hasValue;
    private T value;
    private int blockBit;
    private int bit;

    public OptionalUpdateField(int blockBit, int bit) {
        setBlockBit(blockBit);
        setBit(bit);
    }

    public final T get() {
        return value;
    }

    public final int getBlockBit() {
        return blockBit;
    }

    public final void setBlockBit(int value) {
        blockBit = value;
    }

    public final int getBit() {
        return bit;
    }

    public final void setBit(int value) {
        bit = value;
    }

    public final T getValue() {
        return getValue();
    }

    public final void set(T value) {
        this.value = value;
    }

    public final void setValue(T value) {
        hasValue = true;
        setValue(value);
    }


    public final boolean hasValue() {
        return hasValue;
    }
}
