package com.github.azeroth.game.networking.packet.combat;


public class HealthUpdate extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public long health;

    public HealthUpdate() {
        super(ServerOpcode.HealthUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeInt64(health);
    }
}
