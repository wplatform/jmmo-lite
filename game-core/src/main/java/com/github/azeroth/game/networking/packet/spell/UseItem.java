package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class UseItem extends ClientPacket {
    public byte packSlot;
    public byte slot;
    public ObjectGuid castItem = ObjectGuid.EMPTY;
    public SpellcastRequest cast;

    public UseItem(WorldPacket packet) {
        super(packet);
        cast = new spellCastRequest();
    }

    @Override
    public void read() {
        packSlot = this.readUInt8();
        slot = this.readUInt8();
        castItem = this.readPackedGuid();
        cast.read(this);
    }
}
