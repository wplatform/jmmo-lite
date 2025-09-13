package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

interface IChannelAppender {
    void append(ChannelNotify data);

    ChatNotify getNotificationType();
}
