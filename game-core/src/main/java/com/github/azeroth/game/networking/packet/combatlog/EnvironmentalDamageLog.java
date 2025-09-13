package com.github.azeroth.game.networking.packet.combatlog;


class EnvironmentalDamageLog extends CombatLogServerPacket {
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public EnviromentalDamage type = EnviromentalDamage.values()[0];
    public int amount;
    public int resisted;
    public int absorbed;

    public EnvironmentalDamageLog() {
        super(ServerOpcode.EnvironmentalDamageLog);
    }

    @Override
    public void write() {
        this.writeGuid(victim);
        this.writeInt8((byte) type.getValue());
        this.writeInt32(amount);
        this.writeInt32(resisted);
        this.writeInt32(absorbed);

        writeLogDataBit();
        flushBits();
        writeLogData();
    }
}
