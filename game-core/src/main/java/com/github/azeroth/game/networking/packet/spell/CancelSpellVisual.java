package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class CancelSpellVisual extends ServerPacket {
    public ObjectGuid source = ObjectGuid.EMPTY;
    public int spellVisualID;

    public CancelSpellVisual() {
        super(ServerOpCode.SMSG_CANCEL_SPELL_VISUAL);
    }

    @Override
    public void write() {
        this.writeGuid(source);
        this.writeInt32(spellVisualID);
    }
}
