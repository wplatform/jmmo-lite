package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class InviteAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public InviteAppend() {
    }

    public InviteAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.InviteNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public InviteAppend clone() {
        InviteAppend varCopy = new InviteAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
