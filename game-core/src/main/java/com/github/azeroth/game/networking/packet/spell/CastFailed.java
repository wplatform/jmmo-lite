package com.github.azeroth.game.networking.packet.spell;


class CastFailed extends CastFailedBase {
    public SpellCastvisual visual = new spellCastVisual();

    public CastFailed() {
        super(ServerOpcode.CastFailed);
    }

    @Override
    public void write() {
        this.writeGuid(castID);
        this.writeInt32(spellID);

        visual.write(this);

        this.writeInt32(reason.getValue());
        this.writeInt32(failedArg1);
        this.writeInt32(failedArg2);
    }
}
