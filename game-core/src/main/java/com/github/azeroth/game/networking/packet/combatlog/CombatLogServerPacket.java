package com.github.azeroth.game.networking.packet.combatlog;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.game.networking.packet.spell.SpellCastLogData;

public class CombatLogServerPacket extends ServerPacket {
    public SpellCastLogData logData;
    private boolean includeLogData;


    public CombatLogServerPacket(ServerOpCode opcode) {
        super(opcode);
    }


    @Override
    public void write() {
    }

    public final void setAdvancedCombatLogging(boolean value) {
        includeLogData = value;
    }

    public final void writeLogDataBit() {
        this.writeBit(includeLogData);
    }


    public final void writeLogData() {
        if (includeLogData) {
            logData.write(this);
        }
    }
}

//Structs

