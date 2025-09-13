package com.github.azeroth.game.networking.packet.combatlog;


import com.github.azeroth.game.spell.SpellLogEffect;

import java.util.ArrayList;


class SpellExecuteLog extends CombatLogServerPacket {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public int spellID;
    public ArrayList<SpellLogEffect> effects = new ArrayList<>();

    public SpellExecuteLog() {
        super(ServerOpcode.SpellExecuteLog);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeInt32(spellID);
        this.writeInt32(effects.size());

        for (var effect : effects) {
            this.writeInt32(effect.effect);

            this.writeInt32(effect.powerDrainTargets.size());
            this.writeInt32(effect.extraAttacksTargets.size());
            this.writeInt32(effect.durabilityDamageTargets.size());
            this.writeInt32(effect.genericVictimTargets.size());
            this.writeInt32(effect.tradeSkillTargets.size());
            this.writeInt32(effect.feedPetTargets.size());

            for (var powerDrainTarget : effect.powerDrainTargets) {
                this.writeGuid(powerDrainTarget.victim);
                this.writeInt32(powerDrainTarget.points);
                this.writeInt32(powerDrainTarget.powerType);
                this.writeFloat(powerDrainTarget.amplitude);
            }

            for (var extraAttacksTarget : effect.extraAttacksTargets) {
                this.writeGuid(extraAttacksTarget.victim);
                this.writeInt32(extraAttacksTarget.numAttacks);
            }

            for (var durabilityDamageTarget : effect.durabilityDamageTargets) {
                this.writeGuid(durabilityDamageTarget.victim);
                this.writeInt32(durabilityDamageTarget.itemID);
                this.writeInt32(durabilityDamageTarget.amount);
            }

            for (var genericVictimTarget : effect.genericVictimTargets) {
                this.writeGuid(genericVictimTarget.victim);
            }

            for (var tradeSkillTarget : effect.tradeSkillTargets) {
                this.writeInt32(tradeSkillTarget.itemID);
            }


            for (var feedPetTarget : effect.feedPetTargets) {
                this.writeInt32(feedPetTarget.itemID);
            }
        }

        writeLogDataBit();
        flushBits();
        writeLogData();
    }
}
