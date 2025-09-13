package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.networking.ServerPacket;

public class ItemTimeUpdate extends ServerPacket {
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;

    public int durationLeft;

    public ItemTimeUpdate() {
        super(ServerOpcode.ItemTimeUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(itemGuid);
        this.writeInt32(durationLeft);
    }
}
