package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.networking.ServerPacket;

public class ChatPkt extends ServerPacket {
    public ChatMsg slashCmd = ChatMsg.values()[0];
    public Language _Language = language.Universal;
    public ObjectGuid senderGUID = ObjectGuid.EMPTY;
    public ObjectGuid senderGuildGUID = ObjectGuid.EMPTY;
    public ObjectGuid senderAccountGUID = ObjectGuid.EMPTY;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public ObjectGuid partyGUID = ObjectGuid.EMPTY;

    public int senderVirtualAddress;

    public int targetVirtualAddress;
    public String senderName = "";
    public String targetName = "";
    public String prefix = "";
    public String channel = "";
    public String chatText = "";

    public int achievementID;
    public ChatFlags _ChatFlags = ChatFlags.values()[0];
    public float displayTime;

    public Integer unused_801 = null;
    public boolean hideChatLog;
    public boolean fakeSenderName;
    public ObjectGuid channelGUID = null;

    public ChatPkt() {
        super(ServerOpcode.chat);
    }


    public final void initialize(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId, String channelName, Locale locale) {
        initialize(chatType, language, sender, receiver, message, achievementId, channelName, locale, "");
    }

    public final void initialize(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId, String channelName) {
        initialize(chatType, language, sender, receiver, message, achievementId, channelName, locale.enUS, "");
    }

    public final void initialize(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId) {
        initialize(chatType, language, sender, receiver, message, achievementId, "", locale.enUS, "");
    }

    public final void initialize(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message) {
        initialize(chatType, language, sender, receiver, message, 0, "", locale.enUS, "");
    }

    public final void initialize(ChatMsg chatType, Language language, WorldObject sender, WorldObject receiver, String message, int achievementId, String channelName, Locale locale, String addonPrefix) {
        // Clear everything because same packet can be used multiple times
        clear();

        senderGUID.clear();
        senderAccountGUID.clear();
        senderGuildGUID.clear();
        partyGUID.clear();
        targetGUID.clear();
        senderName = "";
        targetName = "";
        _ChatFlags = ChatFlags.NONE;

        slashCmd = chatType;
        _Language = language;

        if (sender) {
            setSender(sender, locale);
        }

        if (receiver) {
            setReceiver(receiver, locale);
        }

        senderVirtualAddress = global.getWorldMgr().getVirtualRealmAddress();
        targetVirtualAddress = global.getWorldMgr().getVirtualRealmAddress();
        achievementID = achievementId;
        channel = channelName;
        prefix = addonPrefix;
        chatText = message;
    }

    public final void setReceiver(WorldObject receiver, Locale locale) {
        targetGUID = receiver.getGUID();

        var creatureReceiver = receiver.toCreature();

        if (creatureReceiver) {
            targetName = creatureReceiver.getName(locale);
        }
    }

    @Override
    public void write() {
        this.writeInt8((byte) slashCmd.getValue());
        this.writeInt32((int) _Language.getValue());
        this.writeGuid(senderGUID);
        this.writeGuid(senderGuildGUID);
        this.writeGuid(senderAccountGUID);
        this.writeGuid(targetGUID);
        this.writeInt32(targetVirtualAddress);
        this.writeInt32(senderVirtualAddress);
        this.writeGuid(partyGUID);
        this.writeInt32(achievementID);
        this.writeFloat(displayTime);
        this.writeBits(senderName.getBytes().length, 11);
        this.writeBits(targetName.getBytes().length, 11);
        this.writeBits(prefix.getBytes().length, 5);
        this.writeBits(channel.getBytes().length, 7);
        this.writeBits(chatText.getBytes().length, 12);
        this.writeBits((byte) _ChatFlags.getValue(), 14);
        this.writeBit(hideChatLog);
        this.writeBit(fakeSenderName);
        this.writeBit(unused_801 != null);
        this.writeBit(channelGUID != null);
        this.flushBits();

        this.writeString(senderName);
        this.writeString(targetName);
        this.writeString(prefix);
        this.writeString(channel);
        this.writeString(chatText);

        if (unused_801 != null) {
            this.writeInt32(unused_801.intValue());
        }

        if (channelGUID != null) {
            this.writeGuid(channelGUID.getValue());
        }
    }

    private void setSender(WorldObject sender, Locale locale) {
        senderGUID = sender.getGUID();

        var creatureSender = sender.toCreature();

        if (creatureSender) {
            senderName = creatureSender.getName(locale);
        }

        var playerSender = sender.toPlayer();

        if (playerSender) {
            senderAccountGUID = playerSender.getSession().getAccountGUID();
            _ChatFlags = playerSender.getChatFlags();

            senderGuildGUID = ObjectGuid.create(HighGuid.Guild, playerSender.getGuildId());

            var group = playerSender.getGroup();

            if (group) {
                partyGUID = group.getGUID();
            }
        }
    }
}
