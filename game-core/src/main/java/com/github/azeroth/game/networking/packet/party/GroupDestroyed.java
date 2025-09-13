package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class GroupDestroyed extends ServerPacket {
    public GroupDestroyed() {
        super(ServerOpcode.GroupDestroyed);
    }

    @Override
    public void write() {
    }
}
