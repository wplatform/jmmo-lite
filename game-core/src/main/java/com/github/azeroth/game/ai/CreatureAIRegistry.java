package com.github.azeroth.game.ai;/*
 * Java translation of CreatureAIRegistry.cpp
 * Registry for Creature AI types. Comments preserved.
 */

import lombok.Getter;
import lombok.Setter;
import java.util.Map;
import java.util.HashMap;

@Getter @Setter
public class CreatureAIRegistry {
    private static final Map<Integer, String> registry = new HashMap<>();

    public static void register(int id, String aiName) { registry.put(id, aiName); }
    public static String get(int id) { return registry.get(id); }
}
