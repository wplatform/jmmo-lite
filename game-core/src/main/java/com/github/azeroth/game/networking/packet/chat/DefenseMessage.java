package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.ServerPacket;

public class DefenseMessage extends ServerPacket {

    public int zoneID;
    public String messageText = "";

    public DefenseMessage() {
        super(ServerOpcode.DefenseMessage);
    }

    @Override
    public void write() {
        this.writeInt32(zoneID);
        this.writeBits(messageText.getBytes().length, 12);
        this.flushBits();
        this.writeString(messageText);
    }
}
