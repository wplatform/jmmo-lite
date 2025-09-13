package com.github.azeroth.game.networking.packet.item;


public class SetProficiency extends ServerPacket {
    public int proficiencyMask;
    public byte proficiencyClass;

    public SetProficiency() {
        super(ServerOpcode.SetProficiency);
    }

    @Override
    public void write() {
        this.writeInt32(proficiencyMask);
        this.writeInt8(proficiencyClass);
    }
}
