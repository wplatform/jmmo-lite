package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class SetPlayHoverAnim extends ServerPacket {
    public ObjectGuid unitGUID;
    public boolean playHoverAnim;

    public SetPlayHoverAnim() {
        super(ServerOpCode.SMSG_SET_PLAY_HOVER_ANIM);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeBit(playHoverAnim);
        this.flushBits();
    }
}
