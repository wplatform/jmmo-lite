package com.github.azeroth.game.networking.packet.toy;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.HashMap;

public class AccountToyUpdate extends ServerPacket {
    public boolean isFullUpdate = false;
    public HashMap<Integer, toyFlags> toys = new HashMap<Integer, toyFlags>();

    public AccountToyUpdate() {
        super(ServerOpcode.AccountToyUpdate);
    }

    @Override
    public void write() {
        this.writeBit(isFullUpdate);
        this.flushBits();

        // all lists have to have the same size
        this.writeInt32(toys.size());
        this.writeInt32(toys.size());
        this.writeInt32(toys.size());

        for (var pair : toys.entrySet()) {
            this.writeInt32(pair.getKey());
        }

        for (var pair : toys.entrySet()) {
            this.writeBit(pair.getValue().hasFlag(toyFlags.favorite));
        }

        for (var pair : toys.entrySet()) {
            this.writeBit(pair.getValue().hasFlag(toyFlags.HasFanfare));
        }

        this.flushBits();
    }
}
