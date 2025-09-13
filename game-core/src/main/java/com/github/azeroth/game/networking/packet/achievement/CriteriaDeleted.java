package com.github.azeroth.game.networking.packet.achievement;


public class CriteriaDeleted extends ServerPacket {
    public int criteriaID;

    public CriteriaDeleted() {
        super(ServerOpcode.CriteriaDeleted);
    }

    @Override
    public void write() {
        this.writeInt32(criteriaID);
    }
}
