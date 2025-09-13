package com.github.azeroth.game.networking.packet.spell;


public class SpellFailedOther extends ServerPacket {
    public ObjectGuid casterUnit = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastvisual visual = new spellCastVisual();
    public short reason;
    public ObjectGuid castID = ObjectGuid.EMPTY;

    public SpellFailedOther() {
        super(ServerOpcode.SpellFailedOther);
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
