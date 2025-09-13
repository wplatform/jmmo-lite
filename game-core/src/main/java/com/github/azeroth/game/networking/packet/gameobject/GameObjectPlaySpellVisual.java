package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class GameObjectPlaySpellVisual extends ServerPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;
    public ObjectGuid activatorGUID = ObjectGuid.EMPTY;
    public int spellVisualID;

    public GameObjectPlaySpellVisual() {
        super(ServerOpcode.GameObjectPlaySpellVisual);
    }

    @Override
    public void write() {
        this.writeGuid(objectGUID);
        this.writeGuid(activatorGUID);
        this.writeInt32(spellVisualID);
    }
}
