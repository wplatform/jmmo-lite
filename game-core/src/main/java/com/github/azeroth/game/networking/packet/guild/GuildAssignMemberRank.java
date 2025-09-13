package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildAssignMemberRank extends ClientPacket {
    public ObjectGuid member = ObjectGuid.EMPTY;
    public int rankOrder;

    public GuildAssignMemberRank(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        member = this.readPackedGuid();
        rankOrder = this.readInt32();
    }
}
