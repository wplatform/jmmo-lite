package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotModeratorAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotModeratorNotice;
    }

    public void append(ChannelNotify data) {
    }
}
