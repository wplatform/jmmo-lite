package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class GroupUninvite extends ServerPacket {
    public GroupUninvite() {
        super(ServerOpcode.GroupUninvite);
    }

    @Override
    public void write() {
    }
}
