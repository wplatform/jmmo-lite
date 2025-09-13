package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerAlreadyMemberAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public PlayerAlreadyMemberAppend() {
    }

    public PlayerAlreadyMemberAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerAlreadyMemberNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public PlayerAlreadyMemberAppend clone() {
        PlayerAlreadyMemberAppend varCopy = new PlayerAlreadyMemberAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
