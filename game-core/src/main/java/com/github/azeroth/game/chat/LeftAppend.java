package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class LeftAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public LeftAppend() {
    }

    public LeftAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.LeftNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public LeftAppend clone() {
        LeftAppend varCopy = new LeftAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
