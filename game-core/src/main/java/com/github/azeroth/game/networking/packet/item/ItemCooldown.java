package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ServerPacket;

public class ItemCooldown extends ServerPacket {
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;
    public int spellID;
    public int cooldown;

    public ItemCooldown() {
        super(ServerOpcode.ItemCooldown);
    }

    @Override
    public void write() {
        this.writeGuid(itemGuid);
        this.writeInt32(spellID);
        this.writeInt32(cooldown);
    }
}
