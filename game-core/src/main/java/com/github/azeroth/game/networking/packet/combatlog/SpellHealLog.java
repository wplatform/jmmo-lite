package com.github.azeroth.game.networking.packet.combatlog;


class SpellHealLog extends CombatLogServerPacket {
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public int spellID;

    public int health;
    public int originalHeal;

    public int overHeal;

    public int absorbed;
    public boolean crit;
    public Float critRollMade = null;
    public Float critRollNeeded = null;
    public contentTuningParams contentTuning;

    public SpellHealLog() {
        super(ServerOpcode.SpellHealLog);
    }

    @Override
    public void write() {
        this.writeGuid(targetGUID);
        this.writeGuid(casterGUID);

        this.writeInt32(spellID);
        this.writeInt32(health);
        this.writeInt32(originalHeal);
        this.writeInt32(overHeal);
        this.writeInt32(absorbed);

        this.writeBit(crit);

        this.writeBit(critRollMade != null);
        this.writeBit(critRollNeeded != null);
        writeLogDataBit();
        this.writeBit(contentTuning != null);
        flushBits();

        writeLogData();

        if (critRollMade != null) {
            this.writeFloat(critRollMade.floatValue());
        }

        if (critRollNeeded != null) {
            this.writeFloat(critRollNeeded.floatValue());
        }

        if (contentTuning != null) {
            contentTuning.write(this);
        }
    }
}
