package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotOwnerAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotOwnerNotice;
    }

    public void append(ChannelNotify data) {
    }
}
