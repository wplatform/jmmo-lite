package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ChatMessageChannel extends ClientPacket {
    public ObjectGuid channelGUID = ObjectGuid.EMPTY;    public language language = language.Universal;
    public String text;
    public String target;
    public ChatMessageChannel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        language = language.forValue(this.readInt32());
        channelGUID = this.readPackedGuid();
        var targetLen = this.<Integer>readBit(9);
        var textLen = this.<Integer>readBit(11);
        target = this.readString(targetLen);
        text = this.readString(textLen);
    }


}
