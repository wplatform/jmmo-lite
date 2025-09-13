package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum SAGateState {
    // alliance is defender
    AllianceGateOk(1),
    AllianceGateDamaged(2),
    AllianceGateDestroyed(3),

    // horde is defender
    HordeGateOk(4),
    HordeGateDamaged(5),
    HordeGateDestroyed(6);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, SAGateState> mappings;
    private int intValue;

    private SAGateState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, SAGateState> getMappings() {
        if (mappings == null) {
            synchronized (SAGateState.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, SAGateState>();
                }
            }
        }
        return mappings;
    }

    public static SAGateState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
