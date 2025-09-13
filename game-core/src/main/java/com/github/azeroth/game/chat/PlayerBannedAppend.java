package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerBannedAppend implements IChannelAppender {
    private final ObjectGuid moderator;
    private final ObjectGuid banned;

    public PlayerBannedAppend() {
    }

    public PlayerBannedAppend(ObjectGuid moderator, ObjectGuid banned) {
        moderator = moderator;
        banned = banned;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerBannedNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = moderator;
        data.targetGuid = banned;
    }

    public PlayerBannedAppend clone() {
        PlayerBannedAppend varCopy = new PlayerBannedAppend();

        varCopy.moderator = this.moderator;
        varCopy.banned = this.banned;

        return varCopy;
    }
}
