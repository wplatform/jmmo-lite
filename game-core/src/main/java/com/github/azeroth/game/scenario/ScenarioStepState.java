package com.github.azeroth.game.scenario;

public enum ScenarioStepState {
    Invalid(0),
    NotStarted(1),
    inProgress(2),
    Done(3);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ScenarioStepState> mappings;
    private int intValue;

    private ScenarioStepState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ScenarioStepState> getMappings() {
        if (mappings == null) {
            synchronized (ScenarioStepState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ScenarioStepState>();
                }
            }
        }
        return mappings;
    }

    public static ScenarioStepState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
