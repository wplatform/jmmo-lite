package com.github.azeroth.game.ai;

enum FollowState {
    NONE(0x00),
    Inprogress(0x01), //must always have this state for any follow
    paused(0x02), //disables following
    Complete(0x04), //follow is completed and may end
    PreEvent(0x08), //not implemented (allow pre event to run, before follow is initiated)
    PostEvent(0x10); //can be set at complete and allow post event to run

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, FollowState> mappings;
    private int intValue;

    private FollowState(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, FollowState> getMappings() {
        if (mappings == null) {
            synchronized (FollowState.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, FollowState>();
                }
            }
        }
        return mappings;
    }

    public static FollowState forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
