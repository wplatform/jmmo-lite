package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class CancelOrphanSpellVisual extends ServerPacket {
    public int spellVisualID;

    public CancelOrphanSpellVisual() {
        super(ServerOpCode.SMSG_CANCEL_ORPHAN_SPELL_VISUAL);
    }

    @Override
    public void write() {
        this.writeInt32(spellVisualID);
    }
}
