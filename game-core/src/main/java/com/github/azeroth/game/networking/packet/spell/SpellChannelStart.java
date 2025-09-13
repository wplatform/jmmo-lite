package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;

public class SpellChannelStart extends ServerPacket {
    public int spellID;
    public SpellCastvisual visual = new spellCastVisual();
    public SpellChannelStartinterruptImmunities interruptImmunities = null;
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public SpellTargetedhealPrediction healPrediction = null;

    public int channelDuration;

    public SpellChannelStart() {
        super(ServerOpcode.SpellChannelStart);
    }

    @Override
    public void write() {
        this.writeGuid(casterGUID);
        this.writeInt32(spellID);

        visual.write(this);

        this.writeInt32(channelDuration);
        this.writeBit(interruptImmunities != null);
        this.writeBit(healPrediction != null);
        this.flushBits();

        if (interruptImmunities != null) {
            interruptImmunities.getValue().write(this);
        }

        if (healPrediction != null) {
            healPrediction.getValue().write(this);
        }
    }
}
