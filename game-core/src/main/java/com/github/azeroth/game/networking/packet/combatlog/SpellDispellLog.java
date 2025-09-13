package com.github.azeroth.game.networking.packet.combatlog;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class SpellDispellLog extends ServerPacket {
    public ArrayList<SpelldispellData> dispellData = new ArrayList<>();
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public int dispelledBySpellID;
    public boolean isBreak;
    public boolean isSteal;

    public SpellDispellLog() {
        super(ServerOpcode.SpellDispellLog);
    }

    @Override
    public void write() {
        this.writeBit(isSteal);
        this.writeBit(isBreak);
        this.writeGuid(targetGUID);
        this.writeGuid(casterGUID);
        this.writeInt32(dispelledBySpellID);

        this.writeInt32(dispellData.size());

        for (var data : dispellData) {
            this.writeInt32(data.spellID);
            this.writeBit(data.harmful);
            this.writeBit(data.rolled != null);
            this.writeBit(data.needed != null);

            if (data.rolled != null) {
                this.writeInt32(data.rolled.intValue());
            }

            if (data.needed != null) {
                this.writeInt32(data.needed.intValue());
            }

            this.flushBits();
        }
    }
}
