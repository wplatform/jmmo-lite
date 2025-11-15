package com.github.azeroth.game.networking.packet.authentication;


import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class ConnectToFailed extends ClientPacket {
    public ConnectToSerial serial;
    private byte con;

    protected ConnectToFailed(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        con = this.readByte();
        serial = ConnectToSerial.valueOf(this.readUInt32());
    }
}
