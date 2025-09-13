package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class GameObjectInteraction extends ServerPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;
    public PlayerinteractionType interactionType = PlayerInteractionType.values()[0];

    public GameObjectInteraction() {
        super(ServerOpcode.GameObjectInteraction);
    }

    @Override
    public void write() {
        this.writeGuid(objectGUID);
        this.writeInt32(interactionType.getValue());
    }
}
