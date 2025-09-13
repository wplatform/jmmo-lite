package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotModeratedAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotModeratedNotice;
    }

    public void append(ChannelNotify data) {
    }
}
