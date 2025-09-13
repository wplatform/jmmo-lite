package com.github.azeroth.game.networking.packet.spell;


public class SpellFailure extends ServerPacket {
    public ObjectGuid casterUnit = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastvisual visual = new spellCastVisual();
    public short reason;
    public ObjectGuid castID = ObjectGuid.EMPTY;

    public SpellFailure() {
        super(ServerOpcode.SpellFailure);
    }

    @Override
    public void write() {
        this.writeGuid(casterUnit);
        this.writeGuid(castID);
        this.writeInt32(spellID);

        visual.write(this);

        this.writeInt16(reason);
    }
}
