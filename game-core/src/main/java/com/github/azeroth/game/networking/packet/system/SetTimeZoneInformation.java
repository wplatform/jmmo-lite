package com.github.azeroth.game.networking.packet.system;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class SetTimeZoneInformation extends ServerPacket {
    public String serverTimeTZ;
    public String gameTimeTZ;
    public String serverRegionalTZ;

    public SetTimeZoneInformation() {
        super(ServerOpCode.SMSG_SET_TIME_ZONE_INFORMATION);
    }

    @Override
    public void write() {
        this.writeBits(serverTimeTZ.getBytes().length, 7);
        this.writeBits(gameTimeTZ.getBytes().length, 7);
        this.writeBits(serverRegionalTZ.getBytes().length, 7);
        this.flushBits();

        this.writeString(serverTimeTZ);
        this.writeString(gameTimeTZ);
        this.writeString(serverRegionalTZ);
    }
}
