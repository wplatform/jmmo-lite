package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankTextQuery extends ClientPacket {
    public int tab;

    public GuildBankTextQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        tab = this.readInt32();
    }
}
