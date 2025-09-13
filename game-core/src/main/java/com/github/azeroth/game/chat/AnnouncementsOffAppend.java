package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class AnnouncementsOffAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public AnnouncementsOffAppend() {
    }

    public AnnouncementsOffAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.AnnouncementsOffNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public AnnouncementsOffAppend clone() {
        AnnouncementsOffAppend varCopy = new AnnouncementsOffAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
