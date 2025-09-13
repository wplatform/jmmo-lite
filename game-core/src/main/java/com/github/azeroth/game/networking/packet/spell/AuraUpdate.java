package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class AuraUpdate extends ServerPacket {
    public boolean updateAll;
    public ObjectGuid unitGUID;
    public ArrayList<AuraInfo> auras = new ArrayList<>();

    public AuraUpdate() {
        super(ServerOpCode.SMSG_AURA_UPDATE);
    }

    @Override
    public void write() {
        this.writeBit(updateAll);
        this.writeBits(auras.size(), 9);

        for (var aura : auras) {
            aura.write(this);
        }

        this.writeGuid(unitGUID);
    }
}
