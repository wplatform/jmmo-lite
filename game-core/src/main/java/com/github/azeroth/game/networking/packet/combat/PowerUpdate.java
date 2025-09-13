package com.github.azeroth.game.networking.packet.combat;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PowerUpdate extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public ArrayList<PowerUpdatePower> powers;

    public PowerUpdate() {
        super(ServerOpcode.PowerUpdate);
        powers = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeInt32(powers.size());

        for (var power : powers) {
            this.writeInt32(power.power);
            this.writeInt8(power.powerType);
        }
    }
}
