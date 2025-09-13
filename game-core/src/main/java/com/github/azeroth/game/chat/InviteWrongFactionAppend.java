package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class InviteWrongFactionAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.InviteWrongFactionNotice;
    }

    public void append(ChannelNotify data) {
    }
}
