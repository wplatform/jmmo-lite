package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlaySpellVisualKit extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public int kitRecID;
    public int kitType;
    public int duration;
    public boolean mountedVisual;

    public PlaySpellVisualKit() {
        super(ServerOpCode.SMSG_PLAY_SPELL_VISUAL_KIT);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeInt32(kitRecID);
        this.writeInt32(kitType);
        this.writeInt32(duration);
        this.writeBit(mountedVisual);
        this.flushBits();
    }
}
