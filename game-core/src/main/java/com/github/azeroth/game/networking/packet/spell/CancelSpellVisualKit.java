package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class CancelSpellVisualKit extends ServerPacket {
    public ObjectGuid source = ObjectGuid.EMPTY;
    public int spellVisualKitID;
    public boolean mountedVisual;

    public CancelSpellVisualKit() {
        super(ServerOpCode.SMSG_CANCEL_SPELL_VISUAL_KIT);
    }

    @Override
    public void write() {
        this.writeGuid(source);
        this.writeInt32(spellVisualKitID);
        this.writeBit(mountedVisual);
        this.flushBits();
    }
}
