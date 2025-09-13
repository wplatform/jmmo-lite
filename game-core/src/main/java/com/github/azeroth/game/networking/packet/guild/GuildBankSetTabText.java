package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankSetTabText extends ClientPacket {
    public int tab;
    public String tabText;

    public GuildBankSetTabText(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        tab = this.readInt32();
        tabText = this.readString(this.<Integer>readBit(14));
    }
}
