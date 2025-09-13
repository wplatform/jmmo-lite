package com.github.azeroth.game.networking.packet.scene;

import com.github.azeroth.game.networking.ServerPacket;

public class CancelScene extends ServerPacket {
    public int sceneInstanceID;

    public cancelScene() {
        super(ServerOpcode.CancelScene);
    }

    @Override
    public void write() {
        this.writeInt32(sceneInstanceID);
    }
}
