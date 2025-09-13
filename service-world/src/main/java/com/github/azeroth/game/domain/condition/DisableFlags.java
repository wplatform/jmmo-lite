package com.github.azeroth.game.domain.condition;


import java.util.HashMap;

public class DisableFlags {
    public static final disableFlags SPELLPLAYER = new disableFlags(0x01);
    public static final DisableFlags SPELLCREATURE = new disableFlags(0x02);
    public static final DisableFlags SPELLPET = new disableFlags(0x04);
    public static final DisableFlags SPELLDEPRECATEDSPELL = new disableFlags(0x08);
    public static final DisableFlags SPELLMAP = new disableFlags(0x10);
    public static final DisableFlags SPELLAREA = new disableFlags(0x20);
    public static final DisableFlags SPELLLOS = new disableFlags(0x40);
    public static final DisableFlags SPELLGAMEOBJECT = new disableFlags(0x80);
    public static final DisableFlags SPELLARENAS = new disableFlags(0x100);
    public static final DisableFlags SPELLBATTLEGROUNDS = new disableFlags(0x200);
    public static final DisableFlags MAXSPELL = new disableFlags(0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40 | 0x80 | 0x100 | 0x200);

    public static final DisableFlags VMAPAREAFLAG = new disableFlags(0x01);
    public static final DisableFlags VMAPHEIGHT = new disableFlags(0x02);
    public static final DisableFlags VMAPLOS = new disableFlags(0x04);
    public static final DisableFlags VMAPLIQUIDSTATUS = new disableFlags(0x08);

    public static final DisableFlags MMapPathFinding = new disableFlags(0x00);

    public static final DisableFlags DUNGEONSTATUSNORMAL = new disableFlags(0x01);
    public static final DisableFlags DUNGEONSTATUSHEROIC = new disableFlags(0x02);

    public static final DisableFlags DUNGEONSTATUSNORMAL10MAN = new disableFlags(0x01);
    public static final DisableFlags DUNGEONSTATUSNORMAL25MAN = new disableFlags(0x02);
    public static final DisableFlags DUNGEONSTATUSHEROIC10MAN = new disableFlags(0x04);
    public static final DisableFlags DUNGEONSTATUSHEROIC25MAN = new disableFlags(0x08);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, DisableFlags> mappings;
    private int intValue;

    private disableFlags(int value) {
        intValue = value;
        synchronized (DisableFlags.class) {
            getMappings().put(value, this);
        }
    }

    private static HashMap<Integer, DisableFlags> getMappings() {
        if (mappings == null) {
            synchronized (DisableFlags.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, DisableFlags>();
                }
            }
        }
        return mappings;
    }

    public static DisableFlags forValue(int value) {
        synchronized (DisableFlags.class) {
            DisableFlags enumObj = getMappings().get(value);
            if (enumObj == null) {
                return new disableFlags(value);
            } else {
                return enumObj;
            }
        }
    }

    public int getValue() {
        return intValue;
    }
}
