package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class BattlePetJournal extends ServerPacket {
    public short trap;
    public boolean hasJournalLock = false;
    public ArrayList<BattlePetSlot> slots = new ArrayList<>();
    public ArrayList<BattlePetStruct> pets = new ArrayList<>();

    public BattlePetJournal() {
        super(ServerOpcode.BattlePetJournal);
    }

    @Override
    public void write() {
        this.writeInt16(trap);
        this.writeInt32(slots.size());
        this.writeInt32(pets.size());
        this.writeBit(hasJournalLock);
        this.flushBits();

        for (var slot : slots) {
            slot.write(this);
        }

        for (var pet : pets) {
            pet.write(this);
        }
    }
}

//Structs

