package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class MutedAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.MutedNotice;
    }

    public void append(ChannelNotify data) {
    }
}
