package com.github.azeroth.game.networking.packet.combatlog;


class SpellNonMeleeDamageLog extends CombatLogServerPacket {
    public ObjectGuid me = ObjectGuid.EMPTY;
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastvisual visual = new spellCastVisual();
    public int damage;
    public int originalDamage;
    public int overkill = -1;
    public byte schoolMask;
    public int shieldBlock;
    public int resisted;
    public boolean periodic;
    public int absorbed;

    public int flags;

    // Optional<SpellNonMeleeDamageLogDebugInfo> debugInfo;
    public contentTuningParams contentTuning;

    public SpellNonMeleeDamageLog() {
        super(ServerOpcode.SpellNonMeleeDamageLog);
    }

    @Override
    public void write() {
        this.writeGuid(me);
        this.writeGuid(casterGUID);
        this.writeGuid(castID);
        this.writeInt32(spellID);
        visual.write(this);
        this.writeInt32(damage);
        this.writeInt32(originalDamage);
        this.writeInt32(overkill);
        this.writeInt8(schoolMask);
        this.writeInt32(absorbed);
        this.writeInt32(resisted);
        this.writeInt32(shieldBlock);

        this.writeBit(periodic);
        this.writeBits(flags, 7);
        this.writeBit(false); // Debug info
        writeLogDataBit();
        this.writeBit(contentTuning != null);
        flushBits();
        writeLogData();

        if (contentTuning != null) {
            contentTuning.write(this);
        }
    }
}
