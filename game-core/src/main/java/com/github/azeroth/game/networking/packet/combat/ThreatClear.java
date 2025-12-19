package com.github.azeroth.game.networking.packet.combat;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ThreatClear extends ServerPacket {
    public ObjectGuid unitGUID;

    public ThreatClear() {
        super(ServerOpCode.SMSG_THREAT_CLEAR);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
    }
}
