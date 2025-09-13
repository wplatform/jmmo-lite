package com.github.azeroth.game.networking.packet.spell;


class PetCastFailed extends CastFailedBase {
    public PetCastFailed() {
        super(ServerOpcode.PetCastFailed);
    }

    @Override
    public void write() {
        this.writeGuid(castID);
        this.writeInt32(spellID);
        this.writeInt32(reason.getValue());
        this.writeInt32(failedArg1);
        this.writeInt32(failedArg2);
    }
}
