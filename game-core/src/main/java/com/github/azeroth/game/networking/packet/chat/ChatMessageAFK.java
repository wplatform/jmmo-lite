package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ChatMessageAFK extends ClientPacket {
    public String text;

    public ChatMessageAFK(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var len = this.<Integer>readBit(11);
        text = this.readString(len);
    }
}
