package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class ChannelOwnerAppend implements IChannelAppender {
    private final Channel channel;
    private final String ownerName;
    private ObjectGuid ownerGuid = ObjectGuid.EMPTY;

    public ChannelOwnerAppend() {
    }

    public ChannelOwnerAppend(Channel channel, ObjectGuid ownerGuid) {
        channel = channel;
        ownerGuid = ownerGuid;
        ownerName = "";

        var characterCacheEntry = global.getCharacterCacheStorage().getCharacterCacheByGuid(ownerGuid);

        if (characterCacheEntry != null) {
            ownerName = characterCacheEntry.name;
        }
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.ChannelOwnerNotice;
    }

    public void append(ChannelNotify data) {
        data.sender = ((channel.isConstant() || ownerGuid.isEmpty()) ? "Nobody" : ownerName);
    }

    public ChannelOwnerAppend clone() {
        ChannelOwnerAppend varCopy = new ChannelOwnerAppend();

        varCopy.channel = this.channel;
        varCopy.ownerGuid = this.ownerGuid;
        varCopy.ownerName = this.ownerName;

        return varCopy;
    }
}
