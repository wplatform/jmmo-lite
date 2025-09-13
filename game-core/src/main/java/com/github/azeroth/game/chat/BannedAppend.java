package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class BannedAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.BannedNotice;
    }

    public void append(ChannelNotify data) {
    }
}
