package com.github.azeroth.game.scenario;

enum ScenarioType {
    Scenario(0),
    ChallengeMode(1),
    Solo(2),
    Dungeon(10);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ScenarioType> mappings;
    private int intValue;

    private ScenarioType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ScenarioType> getMappings() {
        if (mappings == null) {
            synchronized (ScenarioType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ScenarioType>();
                }
            }
        }
        return mappings;
    }

    public static ScenarioType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
