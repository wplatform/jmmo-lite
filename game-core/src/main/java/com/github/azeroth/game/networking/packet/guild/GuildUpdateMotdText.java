package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildUpdateMotdText extends ClientPacket {
    public String motdText;

    public GuildUpdateMotdText(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var textLen = this.<Integer>readBit(11);
        motdText = this.readString(textLen);
    }
}
