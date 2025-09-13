package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class CastFailedBase extends ServerPacket {
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastResult reason = SpellCastResult.values()[0];
    public int failedArg1 = -1;
    public int failedArg2 = -1;

    public CastFailedBase(ServerOpCode opcode, ConnectionType connectionType) {
        super(opcode, connectionType);
    }

    @Override
    public void write() {
        throw new UnsupportedOperationException();
    }
}
