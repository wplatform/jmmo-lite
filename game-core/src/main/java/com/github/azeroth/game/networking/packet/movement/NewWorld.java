package com.github.azeroth.game.networking.packet.movement;


public class NewWorld extends ServerPacket {
    public int mapID;
    public int reason;
    public Teleportlocation loc = new teleportLocation();
    public Position movementOffset; // Adjusts all pending movement events by this offset

    public NewWorld() {
        super(ServerOpcode.NewWorld);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        loc.write(this);
        this.writeInt32(reason);
        this.writeXYZ(movementOffset);
    }
}
