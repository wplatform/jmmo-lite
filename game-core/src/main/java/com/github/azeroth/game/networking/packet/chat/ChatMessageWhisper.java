package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.WorldPacket;

public class ChatMessageWhisper extends ClientPacket {
    public String text;    public language language = language.Universal;
    public String target;
    public ChatMessageWhisper(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        language = language.forValue(this.readInt32());
        var targetLen = this.<Integer>readBit(9);
        var textLen = this.<Integer>readBit(11);
        target = this.readString(targetLen);
        text = this.readString(textLen);
    }


}
