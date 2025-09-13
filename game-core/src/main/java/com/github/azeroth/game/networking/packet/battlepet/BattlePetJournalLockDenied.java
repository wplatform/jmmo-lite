package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ServerPacket;

public class BattlePetJournalLockDenied extends ServerPacket {
    public BattlePetJournalLockDenied() {
        super(ServerOpcode.BattlePetJournalLockDenied);
    }

    @Override
    public void write() {
    }
}
