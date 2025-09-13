package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;

public class CharacterCreateInfo {
    // User specified variables
    public Race raceId = race.NONE;
    public PlayerClass classId = playerClass.NONE;
    public Gender sex = gender.NONE;
    public Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);
    public Integer templateSet = null;
    public boolean isTrialBoost;
    public boolean useNPE;
    public String name;

    // Server side data
    public byte charCount = 0;
}
