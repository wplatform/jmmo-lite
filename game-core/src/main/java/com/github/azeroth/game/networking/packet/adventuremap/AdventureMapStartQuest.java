package com.github.azeroth.game.networking.packet.adventuremap;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class AdventureMapStartQuest extends ClientPacket {
    public int questID;

    public AdventureMapStartQuest(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        questID = this.readUInt32();
    }
}
