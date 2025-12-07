package com.github.azeroth.utils;

import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StringUtil {

    static String format(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String[] tokenize(String string, String delimiter, boolean keepEmpty) {
        Objects.requireNonNull(string);
        String[] split = string.split(delimiter);
        return Arrays.stream(split).map(String::trim).filter(e -> keepEmpty || !isEmpty(e)).toArray(String[]::new);
    }

    static int[] tokenizeInts(String string, String delimiter) {
        Objects.requireNonNull(string);
        String[] split = string.split(delimiter);
        return Arrays.stream(split).map(String::trim).filter(StringUtil::isEmpty).mapToInt(Integer::parseInt).toArray();
    }

    static int[] distinctTokenizeInts(String string, String delimiter) {
        Objects.requireNonNull(string);
        String[] split = string.split(delimiter);
        return Arrays.stream(split).map(String::trim).filter(StringUtil::isEmpty).mapToInt(Integer::parseInt).distinct().toArray();
    }

    static boolean equalsIgnoreCase(String a, String b) {
        if(a != null) {
            return a.equalsIgnoreCase(b);
        } else if(b != null) {
            return b.equalsIgnoreCase(a);
        } else {
            return true;
        }
    }

    static boolean containsIgnoreCase(String string, String substring) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(substring);
        var a = string.toLowerCase();
        var b = substring.toLowerCase();
        return a.contains(b);
    }





        


}
