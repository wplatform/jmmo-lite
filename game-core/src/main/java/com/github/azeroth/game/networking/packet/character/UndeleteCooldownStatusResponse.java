package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.networking.ServerPacket;

public class UndeleteCooldownStatusResponse extends ServerPacket {
    public boolean onCooldown;

    public int maxCooldown; // max. cooldown until next free character restoration. Displayed in undelete confirm message. (in sec)

    public int currentCooldown; // Current cooldown until next free character restoration. (in sec)

    public UndeleteCooldownStatusResponse() {
        super(ServerOpcode.UndeleteCooldownStatusResponse);
    }

    @Override
    public void write() {
        this.writeBit(onCooldown);
        this.writeInt32(maxCooldown);
        this.writeInt32(currentCooldown);
    }
}
