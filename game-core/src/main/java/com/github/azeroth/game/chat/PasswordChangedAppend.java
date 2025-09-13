package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PasswordChangedAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public PasswordChangedAppend() {
    }

    public PasswordChangedAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PasswordChangedNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public PasswordChangedAppend clone() {
        PasswordChangedAppend varCopy = new PasswordChangedAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
