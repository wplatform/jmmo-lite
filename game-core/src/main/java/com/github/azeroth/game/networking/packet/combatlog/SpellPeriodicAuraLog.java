package com.github.azeroth.game.networking.packet.combatlog;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class SpellPeriodicAuraLog extends CombatLogServerPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public int spellID;
    public ArrayList<SpellLogEffect> effects = new ArrayList<>();

    public SpellPeriodicAuraLog() {
        super(ServerOpcode.SpellPeriodicAuraLog);
    }

    @Override
    public void write() {
        this.writeGuid(targetGUID);
        this.writeGuid(casterGUID);
        this.writeInt32(spellID);
        this.writeInt32(effects.size());
        writeLogDataBit();
        flushBits();

        effects.forEach(p -> p.write(this));

        writeLogData();
    }

    public final static class PeriodicalAuraLogEffectDebugInfo {
        public float critRollMade;
        public float critRollNeeded;

        public PeriodicalAuraLogEffectDebugInfo clone() {
            PeriodicalAuraLogEffectDebugInfo varCopy = new PeriodicalAuraLogEffectDebugInfo();

            varCopy.critRollMade = this.critRollMade;
            varCopy.critRollNeeded = this.critRollNeeded;

            return varCopy;
        }
    }

    public static class SpellLogEffect {
        public int effect;
        public int amount;
        public int originalDamage;
        public int overHealOrKill;
        public int schoolMaskOrPower;
        public int absorbedOrAmplitude;
        public int resisted;
        public boolean crit;
        public PeriodicalAuraLogEffectdebugInfo debugInfo = null;
        public contentTuningParams contentTuning;

        public final void write(WorldPacket data) {
            data.writeInt32(effect);
            data.writeInt32(amount);
            data.writeInt32(originalDamage);
            data.writeInt32(overHealOrKill);
            data.writeInt32(schoolMaskOrPower);
            data.writeInt32(absorbedOrAmplitude);
            data.writeInt32(resisted);

            data.writeBit(crit);
            data.writeBit(debugInfo != null);
            data.writeBit(contentTuning != null);
            data.flushBits();

            if (contentTuning != null) {
                contentTuning.write(data);
            }

            if (debugInfo != null) {
                data.writeFloat(debugInfo.getValue().critRollMade);
                data.writeFloat(debugInfo.getValue().critRollNeeded);
            }
        }
    }
}
