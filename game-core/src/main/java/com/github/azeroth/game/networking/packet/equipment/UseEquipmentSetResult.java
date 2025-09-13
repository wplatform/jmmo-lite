package com.github.azeroth.game.networking.packet.equipment;


import com.github.azeroth.game.networking.ServerPacket;

class UseEquipmentSetResult extends ServerPacket {

    public long GUID; //Set Identifier

    public byte reason;

    public UseEquipmentSetResult() {
        super(ServerOpcode.UseEquipmentSetResult);
    }

    @Override
    public void write() {
        this.writeInt64(GUID);
        this.writeInt8(reason);
    }
}
