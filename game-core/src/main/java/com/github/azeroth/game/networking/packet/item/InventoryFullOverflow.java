package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class InventoryFullOverflow extends ServerPacket {
    public InventoryFullOverflow() {
        super(ServerOpcode.InventoryFullOverflow);
    }

    @Override
    public void write() {
    }
}
