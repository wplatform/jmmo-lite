package com.github.azeroth.defines;

public enum LootType {
    NONE(0),
    CORPSE(1),
    PICKPOCKETING(2),
    FISHING(3),
    DISENCHANTING(4),
    ITEM(5),
    SKINNING(6),
    GATHERING_NODE(8),
    CHEST(9),
    CORPSE_PERSONAL(14),

    FISHING_HOLE(20),                       // unsupported by client, sending LOOT_FISHING instead
    INSIGNIA(21),                       // unsupported by client, sending LOOT_CORPSE instead
    FISHING_JUNK(22),                       // unsupported by client, sending LOOT_FISHING instead
    PROSPECTING(23),
    MILLING(24);

    private final byte value;

    LootType(int value) {
        this.value = (byte) value;
    }

    public LootType getLootTypeForClient() {
        return switch (this) {
            case PROSPECTING, MILLING -> DISENCHANTING;
            case INSIGNIA -> SKINNING;
            case FISHING_HOLE, FISHING_JUNK -> FISHING;
            default -> this;
        };
    }
}
