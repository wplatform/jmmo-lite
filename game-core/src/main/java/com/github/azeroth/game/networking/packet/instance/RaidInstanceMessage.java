package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class RaidInstanceMessage extends ServerPacket {
    public InstanceResetWarningtype type = InstanceResetWarningType.values()[0];
    public int mapID;
    public Difficulty difficultyID = Difficulty.values()[0];
    public boolean locked;
    public boolean extended;

    public RaidInstanceMessage() {
        super(ServerOpcode.RaidInstanceMessage);
    }

    @Override
    public void write() {
        this.writeInt8((byte) type.getValue());
        this.writeInt32(mapID);
        this.writeInt32((int) difficultyID.getValue());
        this.writeBit(locked);
        this.writeBit(extended);
        this.flushBits();
    }
}
