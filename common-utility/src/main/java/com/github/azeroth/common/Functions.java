package com.github.azeroth.common;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface Functions {


    static <K, V> BiFunction<K, List<V>, List<V>> addToList(V valueToAdd) {
        return addToList(valueToAdd, ArrayList::new);
    }

    static <K, V> BiFunction<K, List<V>, List<V>> addToList(V valueToAdd, Supplier<List<V>> listSupplier) {
        return (k, v) -> {
            List<V> values = v == null ? listSupplier.get() : v;
            values.add(valueToAdd);
            return values;
        };
    }

    static <K, V> BiFunction<K, List<V>, List<V>> addEmptyList() {
        return addEmptyList(ArrayList::new);
    }

    static <K, V> BiFunction<K, List<V>, List<V>> addEmptyList(Supplier<List<V>> listSupplier) {
        return (k, v) -> v == null ? listSupplier.get() : v;
    }

    static <K, V> BiFunction<K, Set<V>, Set<V>> addToSet(V valueToAdd) {
        return addToSet(valueToAdd, HashSet::new);
    }

    static <K, V> BiFunction<K, Set<V>, Set<V>> addToSet(V valueToAdd, Supplier<Set<V>> supplier) {
        return (k, v) -> {
            Set<V> values = v == null ? supplier.get() : v;
            values.add(valueToAdd);
            return values;
        };
    }

    static <K, V extends Comparable<V>> BiFunction<K, TreeSet<V>, TreeSet<V>> addToTreeSet(V valueToAdd) {
        return (k, v) -> {
            TreeSet<V> values = v == null ? new TreeSet<>() : v;
            values.add(valueToAdd);
            return values;
        };
    }

    static <K1, K2, V2> BiFunction<K1, Map<K2, V2>, Map<K2, V2>> addToMap(K2 keyToAdd, V2 valueToAdd) {
        return addToMap(keyToAdd, valueToAdd, HashMap::new);
    }

    static <K1, K2, V2> BiFunction<K1, Map<K2, V2>, Map<K2, V2>> addToMap(K2 keyToAdd, V2 valueToAdd, Supplier<Map<K2, V2>> supplier) {
        return (k, v) -> {
            Map<K2,V2> values = v == null ? supplier.get() : v;
            values.put(keyToAdd, valueToAdd);
            return values;
        };
    }

    static <K, V> BiFunction<K, Set<V>, Set<V>> addToSetIfAbsentThen(V valueToAdd, Runnable runnable) {
        return (k, v) -> {
            Set<V> values = v == null ? new HashSet<>() : v;
            if(!values.contains(valueToAdd)) {
                runnable.run();
            }
            values.add(valueToAdd);
            return values;
        };
    }


    static <K, V> BiFunction<K, V, V> ifAbsent(Supplier<V> supplier) {
        return (k, v) -> v == null ? supplier.get() : v;
    }

}
