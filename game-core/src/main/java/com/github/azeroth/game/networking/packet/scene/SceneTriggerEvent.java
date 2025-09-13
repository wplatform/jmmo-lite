package com.github.azeroth.game.networking.packet.scene;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SceneTriggerEvent extends ClientPacket {
    public int sceneInstanceID;
    public String _Event;

    public SceneTriggerEvent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var len = this.<Integer>readBit(6);
        sceneInstanceID = this.readUInt32();
        _Event = this.readString(len);
    }
}
