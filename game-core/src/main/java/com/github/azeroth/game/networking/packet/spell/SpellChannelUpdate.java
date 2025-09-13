package com.github.azeroth.game.networking.packet.spell;


public class SpellChannelUpdate extends ServerPacket {
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public int timeRemaining;

    public SpellChannelUpdate() {
        super(ServerOpcode.SpellChannelUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(casterGUID);
        this.writeInt32(timeRemaining);
    }
}
