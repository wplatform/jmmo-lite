package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.ServerPacket;

public class BattlePetJournalLockAcquired extends ServerPacket {
    public BattlePetJournalLockAcquired() {
        super(ServerOpcode.BattlePetJournalLockAcquired);
    }

    @Override
    public void write() {
    }
}
