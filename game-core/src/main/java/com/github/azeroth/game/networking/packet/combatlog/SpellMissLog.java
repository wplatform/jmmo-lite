package com.github.azeroth.game.networking.packet.combatlog;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class SpellMissLog extends ServerPacket {
    public int spellID;
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ArrayList<SpellLogMissEntry> entries = new ArrayList<>();

    public SpellMissLog() {
        super(ServerOpCode.SMSG_SPELL_MISS_LOG);
    }

    public void write() {
        writeInt32(spellID);
        writeGuid(caster);
        writeInt32(entries.size());

        for (var missEntry : entries) {
            missEntry.write(this);
        }
    }
}
