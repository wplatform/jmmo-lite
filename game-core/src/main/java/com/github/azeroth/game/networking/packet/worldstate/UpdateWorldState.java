package com.github.azeroth.game.networking.packet.worldstate;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class UpdateWorldState extends ServerPacket {
    public int value;
    public boolean hidden; // @todo: research
    public int variableID;

    public UpdateWorldState() {
        super(ServerOpCode.SMSG_UPDATE_WORLD_STATE);
    }

    @Override
    public void write() {
        this.writeInt32(variableID);
        this.writeInt32(value);
        this.writeBit(hidden);
        this.flushBits();
    }
}
