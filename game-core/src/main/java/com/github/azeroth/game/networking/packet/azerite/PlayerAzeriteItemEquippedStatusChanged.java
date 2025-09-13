package com.github.azeroth.game.networking.packet.azerite;

import com.github.azeroth.game.networking.ServerPacket;

public class PlayerAzeriteItemEquippedStatusChanged extends ServerPacket {
    public boolean isHeartEquipped;

    public PlayerAzeriteItemEquippedStatusChanged() {
        super(ServerOpcode.PlayerAzeriteItemEquippedStatusChanged);
    }

    @Override
    public void write() {
        this.writeBit(isHeartEquipped);
        this.flushBits();
    }
}
