package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum SAEventIds {
    BG_SA_EVENT_BLUE_GATE_DAMAGED(19040),
    BG_SA_EVENT_BLUE_GATE_DESTROYED(19045),

    BG_SA_EVENT_GREEN_GATE_DAMAGED(19041),
    BG_SA_EVENT_GREEN_GATE_DESTROYED(19046),

    BG_SA_EVENT_RED_GATE_DAMAGED(19042),
    BG_SA_EVENT_RED_GATE_DESTROYED(19047),

    BG_SA_EVENT_PURPLE_GATE_DAMAGED(19043),
    BG_SA_EVENT_PURPLE_GATE_DESTROYED(19048),

    BG_SA_EVENT_YELLOW_GATE_DAMAGED(19044),
    BG_SA_EVENT_YELLOW_GATE_DESTROYED(19049),

    BG_SA_EVENT_ANCIENT_GATE_DAMAGED(19836),
    BG_SA_EVENT_ANCIENT_GATE_DESTROYED(19837),

    BG_SA_EVENT_TITAN_RELIC_ACTIVATED(22097);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, SAEventIds> mappings;
    private int intValue;

    private SAEventIds(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, SAEventIds> getMappings() {
        if (mappings == null) {
            synchronized (SAEventIds.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, SAEventIds>();
                }
            }
        }
        return mappings;
    }

    public static SAEventIds forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
