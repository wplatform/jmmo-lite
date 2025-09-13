package com.github.azeroth.game.networking.packet.addon;

import com.github.azeroth.game.networking.WorldPacket;

public final class AddOnInfo {
    public String name;
    public String version;
    public boolean loaded;
    public boolean disabled;

    public void read(WorldPacket data) {
        data.resetBitPos();

        var nameLength = data.<Integer>readBit(10);
        var versionLength = data.<Integer>readBit(10);
        loaded = data.readBit();
        disabled = data.readBit();

        if (nameLength > 1) {
            name = data.readString(nameLength - 1);
            data.readUInt8(); // null terminator
        }

        if (versionLength > 1) {
            version = data.readString(versionLength - 1);
            data.readUInt8(); // null terminator
        }
    }

    public AddOnInfo clone() {
        AddOnInfo varCopy = new AddOnInfo();

        varCopy.name = this.name;
        varCopy.version = this.version;
        varCopy.loaded = this.loaded;
        varCopy.disabled = this.disabled;

        return varCopy;
    }
}
