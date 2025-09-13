package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankActivate extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;
    public boolean fullUpdate;

    public GuildBankActivate(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        fullUpdate = this.readBit();
    }
}
