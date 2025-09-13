package com.github.azeroth.game.networking.packet.character;

public class CharacterUndeleteInfo {
    // User specified variables
    public ObjectGuid characterGuid = ObjectGuid.EMPTY; // Guid of the character to restore
    public int clientToken = 0; // @todo: research

    // Server side data
    public String name;
}
