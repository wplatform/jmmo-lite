package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotInAreaAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotInAreaNotice;
    }

    public void append(ChannelNotify data) {
    }
}
