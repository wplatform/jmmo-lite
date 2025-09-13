package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class WrongPasswordAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.WrongPasswordNotice;
    }

    public void append(ChannelNotify data) {
    }
}
