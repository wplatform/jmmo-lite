package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerUnbannedAppend implements IChannelAppender {
    private final ObjectGuid moderator;
    private final ObjectGuid unbanned;

    public PlayerUnbannedAppend() {
    }

    public PlayerUnbannedAppend(ObjectGuid moderator, ObjectGuid unbanned) {
        moderator = moderator;
        unbanned = unbanned;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerUnbannedNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = moderator;
        data.targetGuid = unbanned;
    }

    public PlayerUnbannedAppend clone() {
        PlayerUnbannedAppend varCopy = new PlayerUnbannedAppend();

        varCopy.moderator = this.moderator;
        varCopy.unbanned = this.unbanned;

        return varCopy;
    }
}
