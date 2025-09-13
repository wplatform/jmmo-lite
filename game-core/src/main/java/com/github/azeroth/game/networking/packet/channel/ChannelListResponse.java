package com.github.azeroth.game.networking.packet.channel;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class ChannelListResponse extends ServerPacket {
    public ArrayList<ChannelPlayer> members;
    public String channel; // Channel Name
    public ChannelFlags channelFlags = Framework.Constants.channelFlags.values()[0];
    public boolean display;

    public ChannelListResponse() {
        super(ServerOpcode.ChannelList);
        members = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeBit(display);
        this.writeBits(channel.getBytes().length, 7);
        this.writeInt32((int) channelFlags.getValue());
        this.writeInt32(members.size());
        this.writeString(channel);

        for (var player : members) {
            this.writeGuid(player.guid);
            this.writeInt32(player.virtualRealmAddress);
            this.writeInt8((byte) player.flags.getValue());
        }
    }


    public final static class ChannelPlayer {
        public ObjectGuid UUID = ObjectGuid.EMPTY; // Player Guid

        public int virtualRealmAddress;
        public ChannelMemberflags flags = ChannelMemberFlags.values()[0];

        public ChannelPlayer() {
        }

        public ChannelPlayer(ObjectGuid guid, int realm, ChannelMemberFlags flags) {
            UUID = guid;
            virtualRealmAddress = realm;
            flags = flags;
        }

        public ChannelPlayer clone() {
            ChannelPlayer varCopy = new ChannelPlayer();

            varCopy.guid = this.guid;
            varCopy.virtualRealmAddress = this.virtualRealmAddress;
            varCopy.flags = this.flags;

            return varCopy;
        }
    }
}
