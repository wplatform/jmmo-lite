package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class OwnerChangedAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public OwnerChangedAppend() {
    }

    public OwnerChangedAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.OwnerChangedNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public OwnerChangedAppend clone() {
        OwnerChangedAppend varCopy = new OwnerChangedAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
