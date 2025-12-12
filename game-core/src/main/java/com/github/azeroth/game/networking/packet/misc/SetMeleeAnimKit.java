package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class SetMeleeAnimKit extends ServerPacket {
    public ObjectGuid unit;
    public short animKitID;

    public SetMeleeAnimKit() {
        super(ServerOpCode.SMSG_SET_MELEE_ANIM_KIT);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeInt16(animKitID);
    }
}
