package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ServerPacket;

public class ChatServerMessage extends ServerPacket {
    public int messageID;
    public String stringParam = "";

    public ChatServerMessage() {
        super(ServerOpcode.ChatServerMessage);
    }

    @Override
    public void write() {
        this.writeInt32(messageID);

        this.writeBits(stringParam.getBytes().length, 11);
        this.writeString(stringParam);
    }
}
