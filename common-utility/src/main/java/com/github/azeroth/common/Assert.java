package com.github.azeroth.common;

import org.slf4j.helpers.MessageFormatter;

public interface Assert {

    static void isTrue(boolean b) {
        if(!b) {
            throw new IllegalArgumentException("Assertion failed, require true but got false");
        }
    }

    static void isTrue(boolean b, String message, Object... args) {
        if(!b) {
            throw new IllegalArgumentException(MessageFormatter.basicArrayFormat(message, args));
        }
    }

    static void notOutOfBound(int index, int length, String message, Object... args) {
        if(index >= length) {
            throw new IndexOutOfBoundsException(MessageFormatter.basicArrayFormat(message, args));
        }
    }

    static void fail() {
        throw new IllegalStateException("Assertion failed");
    }

    static void fail(String message, Object... args) {
        throw new IllegalStateException(MessageFormatter.basicArrayFormat(message, args));
    }
}
