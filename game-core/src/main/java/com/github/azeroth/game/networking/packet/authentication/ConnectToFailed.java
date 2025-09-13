package com.github.azeroth.game.networking.packet.authentication;


import com.github.azeroth.game.networking.WorldPacket;

class ConnectToFailed extends ClientPacket {
    public ConnectToserial serial = ConnectToSerial.values()[0];
    private byte con;

    public ConnectToFailed(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        serial = ConnectToSerial.forValue(this.readUInt());
        con = this.readUInt8();
    }
}
