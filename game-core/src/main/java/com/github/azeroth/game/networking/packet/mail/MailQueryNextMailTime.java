package com.github.azeroth.game.networking.packet.mail;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MailQueryNextMailTime extends ClientPacket {
    public MailQueryNextMailTime(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
