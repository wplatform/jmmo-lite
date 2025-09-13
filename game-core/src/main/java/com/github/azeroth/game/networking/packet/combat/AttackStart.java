package com.github.azeroth.game.networking.packet.combat;


public class AttackStart extends ServerPacket {
    public ObjectGuid attacker = ObjectGuid.EMPTY;
    public ObjectGuid victim = ObjectGuid.EMPTY;

    public attackStart() {
        super(ServerOpcode.AttackStart);
    }

    @Override
    public void write() {
        this.writeGuid(attacker);
        this.writeGuid(victim);
    }
}
