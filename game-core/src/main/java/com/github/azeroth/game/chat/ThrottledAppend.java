package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class ThrottledAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.ThrottledNotice;
    }

    public void append(ChannelNotify data) {
    }
}
