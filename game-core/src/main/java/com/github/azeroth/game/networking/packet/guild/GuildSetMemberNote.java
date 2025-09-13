package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildSetMemberNote extends ClientPacket {
    public ObjectGuid noteeGUID = ObjectGuid.EMPTY;
    public boolean isPublic; // 0 == officer, 1 == Public
    public String note;

    public GuildSetMemberNote(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        noteeGUID = this.readPackedGuid();

        var noteLen = this.<Integer>readBit(8);
        isPublic = this.readBit();

        note = this.readString(noteLen);
    }
}
