package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class NewWorld extends ServerPacket {
    public int mapID;
    public int reason;
    public TeleportLocation loc;
    public Position movementOffset; // Adjusts all pending movement events by this offset

    public NewWorld() {
        super(ServerOpCode.SMSG_NEW_WORLD);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        loc.write(this);
        this.writeInt32(reason);
        this.writeXYZ(movementOffset);
    }
}
