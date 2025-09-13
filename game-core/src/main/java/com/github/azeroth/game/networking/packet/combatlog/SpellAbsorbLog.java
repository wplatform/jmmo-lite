package com.github.azeroth.game.networking.packet.combatlog;


class SpellAbsorbLog extends CombatLogServerPacket {
    public ObjectGuid attacker = ObjectGuid.EMPTY;
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public int absorbedSpellID;
    public int absorbSpellID;
    public int absorbed;
    public int originalDamage;
    public boolean unk;

    public SpellAbsorbLog() {
        super(ServerOpcode.SpellAbsorbLog);
    }

    @Override
    public void write() {
        this.writeGuid(attacker);
        this.writeGuid(victim);
        this.writeInt32(absorbedSpellID);
        this.writeInt32(absorbSpellID);
        this.writeGuid(caster);
        this.writeInt32(absorbed);
        this.writeInt32(originalDamage);

        this.writeBit(unk);
        writeLogDataBit();
        flushBits();

        writeLogData();
    }
}
