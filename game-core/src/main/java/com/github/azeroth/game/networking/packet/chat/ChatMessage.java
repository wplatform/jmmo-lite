package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ChatMessage extends ClientPacket {
    public String text;
    public ChatMessage(WorldPacket packet) {
        super(packet);
    }    public language language = language.Universal;

    @Override
    public void read() {
        language = language.forValue(this.readInt32());
        var len = this.<Integer>readBit(11);
        text = this.readString(len);
    }


}
