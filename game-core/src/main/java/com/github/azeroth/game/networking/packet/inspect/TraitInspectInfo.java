package com.github.azeroth.game.networking.packet.inspect;

import com.github.azeroth.game.networking.WorldPacket;

public final class TraitInspectInfo {
    public int level;
    public int chrSpecializationID;
    public TraitconfigPacket config;

    public void write(WorldPacket data) {
        data.writeInt32(level);
        data.writeInt32(chrSpecializationID);

        if (config != null) {
            config.write(data);
        }
    }

    public TraitInspectInfo clone() {
        TraitInspectInfo varCopy = new traitInspectInfo();

        varCopy.level = this.level;
        varCopy.chrSpecializationID = this.chrSpecializationID;
        varCopy.config = this.config;

        return varCopy;
    }
}
