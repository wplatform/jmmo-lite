package com.github.azeroth.game.battlefield;


import java.util.HashMap;


public enum BattleFieldTypes {
    WinterGrasp(1),
    TolBarad(2),
    max(3);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, BattleFieldTypes> mappings;
    private int intValue;

    private BattleFieldTypes(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, BattleFieldTypes> getMappings() {
        if (mappings == null) {
            synchronized (BattleFieldTypes.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, BattleFieldTypes>();
                }
            }
        }
        return mappings;
    }

    public static BattleFieldTypes forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
