package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.ServerPacket;

public class BattlePetErrorPacket extends ServerPacket {
    public BattlePetError result = BattlePetError.values()[0];
    public int creatureID;

    public BattlePetErrorPacket() {
        super(ServerOpcode.BattlePetError);
    }

    @Override
    public void write() {
        this.writeBits(result, 4);
        this.writeInt32(creatureID);
    }
}
