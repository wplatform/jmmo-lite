package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class GroupNewLeader extends ServerPacket {
    public byte partyIndex;
    public String name;

    public GroupNewLeader() {
        super(ServerOpcode.GroupNewLeader);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeBits(name.getBytes().length, 9);
        this.writeString(name);
    }
}
