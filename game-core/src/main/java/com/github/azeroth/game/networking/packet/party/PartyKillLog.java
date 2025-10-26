package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PartyKillLog extends ServerPacket {
    public ObjectGuid player;
    public ObjectGuid victim;

    public PartyKillLog() {
        super(ServerOpCode.SMSG_PARTY_KILL_LOG);
    }

    @Override
    public void write() {
        this.writeGuid(player);
        this.writeGuid(victim);
    }
}
