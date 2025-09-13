package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotMemberAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotMemberNotice;
    }

    public void append(ChannelNotify data) {
    }
}
