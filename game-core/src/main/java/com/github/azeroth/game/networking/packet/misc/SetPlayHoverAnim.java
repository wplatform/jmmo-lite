package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class SetPlayHoverAnim extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public boolean playHoverAnim;

    public setPlayHoverAnim() {
        super(ServerOpcode.SetPlayHoverAnim);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeBit(playHoverAnim);
        this.flushBits();
    }
}
