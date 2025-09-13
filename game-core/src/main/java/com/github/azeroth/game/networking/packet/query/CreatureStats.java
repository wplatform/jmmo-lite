package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.domain.creature.CreatureTemplate;

public class CreatureStats {
    public String title;
    public String titleAlt;
    public String cursorName;
    public int creatureType;
    public int creatureFamily;
    public int classification;
    public CreatureDisplayStats display = new CreatureDisplayStats();
    public float hpMulti;
    public float energyMulti;
    public boolean leader;

    public int[] questItems;

    public int creatureMovementInfoID;
    public int healthScalingExpansion;

    public int requiredExpansion;

    public int vignetteID;
    public int unitClass;
    public int creatureDifficultyID;
    public int widgetSetID;
    public int widgetSetUnitConditionID;

    public int[] flags = new int[2];

    public int[] proxyCreatureID = new int[CreatureTemplate.MAX_KILL_CREDIT];
    public String[] name;
    public String[] nameAlt;
}
