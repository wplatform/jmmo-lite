package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class BattlePetUpdates extends ServerPacket {
    public ArrayList<BattlePetStruct> pets = new ArrayList<>();
    public boolean petAdded;

    public BattlePetUpdates() {
        super(ServerOpcode.BattlePetUpdates);
    }

    @Override
    public void write() {
        this.writeInt32(pets.size());
        this.writeBit(petAdded);
        this.flushBits();

        for (var pet : pets) {
            pet.write(this);
        }
    }
}
