package com.github.azeroth.game.networking.packet.scene;

import com.github.azeroth.game.networking.WorldPacket;

class ScenePlaybackComplete extends ClientPacket {
    public int sceneInstanceID;

    public ScenePlaybackComplete(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        sceneInstanceID = this.readUInt();
    }
}
