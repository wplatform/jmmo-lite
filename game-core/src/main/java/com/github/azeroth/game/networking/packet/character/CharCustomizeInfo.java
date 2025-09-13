package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;

public class CharCustomizeInfo {
    public ObjectGuid charGUID = ObjectGuid.EMPTY;
    public Gender sexID = gender.NONE;
    public String charName;
    public Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);
}
