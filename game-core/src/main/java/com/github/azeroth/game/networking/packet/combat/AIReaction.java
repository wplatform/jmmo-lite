package com.github.azeroth.game.networking.packet.combat;


public class AIReaction extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public Aireaction reaction = AiReaction.values()[0];

    public AIReaction() {
        super(ServerOpcode.AiReaction);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeInt32((int) reaction.getValue());
    }
}
