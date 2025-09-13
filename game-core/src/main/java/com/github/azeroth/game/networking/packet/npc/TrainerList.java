package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.domain.creature.TrainerType;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class TrainerList extends ServerPacket {
    public ObjectGuid trainerGUID = ObjectGuid.EMPTY;
    public TrainerType trainerType;
    public int trainerID = 1;
    public ArrayList<TrainerListSpell> spells = new ArrayList<>();
    public String greeting;

    public TrainerList() {
        super(ServerOpCode.SMSG_TRAINER_LIST);
    }

    @Override
    public void write() {
        this.writeGuid(trainerGUID);
        this.writeInt32(trainerType.ordinal());
        this.writeInt32(trainerID);

        this.writeInt32(spells.size());

        for (var spell : spells) {
            this.writeInt32(spell.spellID);
            this.writeInt32(spell.moneyCost);
            this.writeInt32(spell.reqSkillLine);
            this.writeInt32(spell.reqSkillRank);

            for (int reqAbility : spell.reqAbility) {
                this.writeInt32(reqAbility);
            }

            this.writeInt8((byte) spell.usable.ordinal());
            this.writeInt8(spell.reqLevel);
        }

        this.writeBits(greeting.getBytes().length, 11);
        this.flushBits();
        this.writeString(greeting);
    }
}
