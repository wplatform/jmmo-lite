package com.github.azeroth.game.networking.packet.combatlog;


class SpellDamageShield extends CombatLogServerPacket {
    public ObjectGuid attacker = ObjectGuid.EMPTY;
    public ObjectGuid defender = ObjectGuid.EMPTY;
    public int spellID;
    public int totalDamage;
    public int originalDamage;
    public int overKill;
    public int schoolMask;
    public int logAbsorbed;

    public SpellDamageShield() {
        super(ServerOpcode.SpellDamageShield);
    }

    @Override
    public void write() {
        this.writeGuid(attacker);
        this.writeGuid(defender);
        this.writeInt32(spellID);
        this.writeInt32(totalDamage);
        this.writeInt32(originalDamage);
        this.writeInt32(overKill);
        this.writeInt32(schoolMask);
        this.writeInt32(logAbsorbed);

        writeLogDataBit();
        flushBits();
        writeLogData();
    }
}
