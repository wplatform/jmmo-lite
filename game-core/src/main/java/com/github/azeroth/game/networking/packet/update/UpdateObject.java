package com.github.azeroth.game.networking.packet.update;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class UpdateObject extends ServerPacket {
    public int numObjUpdates;
    public short mapID;
    public byte[] data;

    public UpdateObject() {
        super(ServerOpCode.SMSG_UPDATE_OBJECT);
    }

    @Override
    public void write() {
        this.writeInt32(numObjUpdates);
        this.writeInt16(mapID);
        this.writeBytes(data);
    }
}
