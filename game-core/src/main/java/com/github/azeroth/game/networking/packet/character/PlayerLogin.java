package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class PlayerLogin extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY; // Guid of the player that is logging in
    public float farClip; // Visibility distance (for terrain)

    public PlayerLogin(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        farClip = this.readFloat();
    }
}
