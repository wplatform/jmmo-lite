package com.github.azeroth.common;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumFlag<T extends Enum<T> & EnumFlag.FlagValue> {

    public interface FlagValue {
        int getValue();
    }

    private final Class<T> elementType;
    @Getter
    private int flag;



    @SafeVarargs
    public final boolean hasAnyFlag(T... elements) {
        for (T element : elements) {
            if(hasFlag(element)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFlag(T element) {
        return (this.flag & element.getValue()) != 0;
    }

    @SafeVarargs
    public final boolean hasFlag(T... elements) {
        for (T element : elements) {
            if(!hasFlag(element)) {
                return false;
            }
        }
        return true;
    }


    public boolean hasNotFlag(T element) {
        return (this.flag & ~element.getValue()) != 0;
    }

    @SafeVarargs
    public final boolean hasNotFlag(T... elements) {
        int newFlag = 0;
        for (T element : elements) {
            newFlag |= element.getValue();
        }
        return (this.flag & ~newFlag) != 0;
    }

    public boolean hasFlag(EnumFlag<T> other) {
        return (this.flag & other.flag) != 0;
    }

    public boolean hasAllFlags(T element) {
        return (this.flag & element.getValue()) == element.getValue();
    }

    public boolean hasAllFlags(EnumFlag<T> other) {
        return (this.flag & other.flag) == other.flag;
    }


    @SafeVarargs
    public final EnumFlag<T> removeNotFlag(T... elements) {
        for (T element : elements) {
            removeNotFlag(element);
        }
        return this;
    }

    public EnumFlag<T> removeNotFlag(T element) {
        this.flag &= element.getValue();
        return this;
    }


    @SafeVarargs
    public final EnumFlag<T> removeFlag(T... elements) {
        for (T element : elements) {
            removeFlag(element);
        }
        return this;
    }

    public EnumFlag<T> removeFlag(T element) {
        this.flag &= ~element.getValue();
        return this;
    }


    public EnumFlag<T> removeFlag(EnumFlag<T> other) {
        this.flag &= ~other.flag;
        return this;
    }

    @SafeVarargs
    public final EnumFlag<T> addFlag(T... elements) {
        for (T element : elements) {
            addFlag(element);
        }
        return this;
    }

    public EnumFlag<T> addFlag(T element) {
        this.flag |= element.getValue();
        return this;
    }

    public EnumFlag<T> addFlag(EnumFlag<T> other) {
        this.flag |= other.flag;
        return this;
    }


    public EnumFlag<T> xor(T element) {
        this.flag ^= element.getValue();
        return this;
    }

    public EnumFlag<T> xor(EnumFlag<T> other) {
        this.flag ^= other.flag;
        return this;
    }

    public EnumFlag<T> not() {
        this.flag = ~flag;
        return this;
    }

    public boolean isEmpty() {
        return this.flag == 0;
    }

    @SafeVarargs
    public final EnumFlag<T> set(T... elements) {
        int newFlag = 0;
        for (T element : elements) {
            newFlag |= element.getValue();
        }
        this.flag = newFlag;
        return this;
    }

    public EnumFlag<T> setFlags(int newFlag) {
        this.flag = newFlag;
        return this;
    }

    public boolean equals(T flag) {
       return this.flag == flag.getValue();
    }


    public Optional<T> toElement() {
        EnumSet<T> elements = EnumSet.allOf(elementType);
        return elements.stream()
                .filter(e -> e.getValue() == this.flag)
                .findFirst();
    }

    public static <T extends Enum<T> & EnumFlag.FlagValue> EnumFlag<T> of(Class<T> elementType) {
        return of(elementType, 0);
    }

    public static <T extends Enum<T> & EnumFlag.FlagValue> EnumFlag<T> of(EnumFlag<T> enumFlag) {
        return of(enumFlag.elementType, enumFlag.flag);
    }

    public static <T extends Enum<T> & EnumFlag.FlagValue> EnumFlag<T> of(T element) {
        @SuppressWarnings("unchecked")
        Class<T> elementType = (Class<T>) element.getClass();
        return of(elementType, element.getValue());
    }

    @SafeVarargs
    public static <T extends Enum<T> & EnumFlag.FlagValue> EnumFlag<T> of(T... elements) {
        @SuppressWarnings("unchecked")
        Class<T> elementType = (Class<T>)elements[0].getClass();
        int flag = 0;
        for (T element : elements) {
            flag |= element.getValue();
        }
        return of(elementType, flag);
    }

    public static <T extends Enum<T> & EnumFlag.FlagValue> EnumFlag<T> of(Class<T> elementType, int value) {
        return new EnumFlag<>(elementType, value);
    }

    @Override
    public String toString() {
        return String.valueOf(flag);
    }
}
