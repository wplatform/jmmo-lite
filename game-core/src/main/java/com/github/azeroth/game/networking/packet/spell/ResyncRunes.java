package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ResyncRunes extends ServerPacket {
    public RuneData runes = new RuneData();

    public ResyncRunes() {
        super(ServerOpCode.SMSG_RESYNC_RUNES);
    }

    @Override
    public void write() {
        runes.write(this);
    }
}
