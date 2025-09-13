package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class GameObjectCloseInteraction extends ServerPacket {
    public PlayerinteractionType interactionType = PlayerInteractionType.values()[0];

    public GameObjectCloseInteraction() {
        super(ServerOpcode.GameObjectCloseInteraction);
    }

    @Override
    public void write() {
        this.writeInt32(interactionType.getValue());
    }
}
