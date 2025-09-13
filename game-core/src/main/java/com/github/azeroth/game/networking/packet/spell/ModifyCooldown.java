package com.github.azeroth.game.networking.packet.spell;


public class ModifyCooldown extends ServerPacket {
    public boolean isPet;
    public boolean withoutCategoryCooldown;
    public int deltaTime;
    public int spellID;

    public modifyCooldown() {
        super(ServerOpcode.ModifyCooldown);
    }

    @Override
    public void write() {
        this.writeInt32(spellID);
        this.writeInt32(deltaTime);
        this.writeBit(isPet);
        this.writeBit(withoutCategoryCooldown);
        this.flushBits();
    }
}
