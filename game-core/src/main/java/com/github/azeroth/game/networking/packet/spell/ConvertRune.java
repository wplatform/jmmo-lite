package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ConvertRune extends ServerPacket {

    public RuneData Runes;
    public int Index = 0;
    public int Rune = 0;

    public ConvertRune() {
        super(SMSG_CONVERT_RUNE);
    }

    @Override
    public void write() {

    }
}
