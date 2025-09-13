package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class VoiceOnAppend implements IChannelAppender {
    private final ObjectGuid guid;

    public VoiceOnAppend() {
    }

    public VoiceOnAppend(ObjectGuid guid) {
        guid = guid;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.VoiceOnNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = guid;
    }

    public VoiceOnAppend clone() {
        VoiceOnAppend varCopy = new VoiceOnAppend();

        varCopy.guid = this.guid;

        return varCopy;
    }
}
