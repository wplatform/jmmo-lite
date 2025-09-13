package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.defines.ChatMsg;
import com.github.azeroth.game.networking.WorldPacket;

public class ChatAddonMessageParams {
    public String prefix;
    public String text;
    public ChatMsg type = ChatMsg.PARTY;
    public boolean isLogged;

    public final void read(WorldPacket data) {
        var prefixLen = data.readBit(5);
        var textLen = data.readBit(8);
        isLogged = data.readBit();
        type = ChatMsg.valueOf(data.readInt32());
        prefix = data.readString(prefixLen);
        text = data.readString(textLen);
    }
}
