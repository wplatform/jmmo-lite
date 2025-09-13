package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class DestructibleBuildingDamage extends ServerPacket {
    public ObjectGuid target = ObjectGuid.EMPTY;
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public int damage;
    public int spellID;

    public DestructibleBuildingDamage() {
        super(ServerOpcode.DestructibleBuildingDamage);
    }

    @Override
    public void write() {
        this.writeGuid(target);
        this.writeGuid(owner);
        this.writeGuid(caster);
        this.writeInt32(damage);
        this.writeInt32(spellID);
    }
}
