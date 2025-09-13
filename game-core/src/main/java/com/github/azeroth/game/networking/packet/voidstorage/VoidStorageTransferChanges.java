package com.github.azeroth.game.networking.packet.voidstorage;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class VoidStorageTransferChanges extends ServerPacket {
    public ArrayList<ObjectGuid> removedItems = new ArrayList<>();
    public ArrayList<VoidItem> addedItems = new ArrayList<>();

    public VoidStorageTransferChanges() {
        super(ServerOpcode.VoidStorageTransferChanges);
    }

    @Override
    public void write() {
        this.writeBits(addedItems.size(), 4);
        this.writeBits(removedItems.size(), 4);
        this.flushBits();

        for (var addedItem : addedItems) {
            addedItem.write(this);
        }

        for (var removedItem : removedItems) {
            this.writeGuid(removedItem);
        }
    }
}
