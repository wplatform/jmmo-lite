package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PetBattleSlotUpdates extends ServerPacket {
    public ArrayList<BattlePetSlot> slots = new ArrayList<>();
    public boolean autoSlotted;
    public boolean newSlot;

    public PetBattleSlotUpdates() {
        super(ServerOpcode.PetBattleSlotUpdates);
    }

    @Override
    public void write() {
        this.writeInt32(slots.size());
        this.writeBit(newSlot);
        this.writeBit(autoSlotted);
        this.flushBits();

        for (var slot : slots) {
            slot.write(this);
        }
    }
}
