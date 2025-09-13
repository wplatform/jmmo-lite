package com.github.azeroth.game.networking.packet.combatlog;


class SpellEnergizeLog extends CombatLogServerPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public int spellID;
    public Powertype type = powerType.values()[0];
    public int amount;
    public int overEnergize;

    public SpellEnergizeLog() {
        super(ServerOpcode.SpellEnergizeLog);
    }

    @Override
    public void write() {
        this.writeGuid(targetGUID);
        this.writeGuid(casterGUID);

        this.writeInt32(spellID);
        this.writeInt32((int) type.getValue());
        this.writeInt32(amount);
        this.writeInt32(overEnergize);

        writeLogDataBit();
        flushBits();
        writeLogData();
    }
}
