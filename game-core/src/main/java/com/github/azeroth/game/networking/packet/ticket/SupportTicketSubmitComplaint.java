package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class SupportTicketSubmitComplaint extends ClientPacket {
    public SupportTicketheader header = new supportTicketHeader();
    public SupportTicketchatLog chatLog;
    public ObjectGuid targetCharacterGUID = ObjectGuid.EMPTY;
    public int reportType;
    public int majorCategory;
    public int minorCategoryFlags;
    public String note;
    public SupportTickethorusChatLog horusChatLog;
    public SupportTicketmailInfo mailInfo = null;
    public SupportTicketCalendarEventInfo calenderInfo = null;
    public SupportTicketpetInfo petInfo = null;
    public SupportTicketguildInfo guildInfo = null;
    public SupportTicketLFGListSearchResult LFGListSearchResult = null;
    public SupportTicketLFGListApplicant LFGListApplicant = null;
    public SupportTicketcommunityMessage communityMessage = null;
    public SupportTicketclubFinderResult clubFinderResult = null;
    public SupportTicketunused910 unused910 = null;

    public SupportTicketSubmitComplaint(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        header.read(this);
        targetCharacterGUID = this.readPackedGuid();
        reportType = this.readInt32();
        majorCategory = this.readInt32();
        minorCategoryFlags = this.readInt32();
        chatLog.read(this);

        var noteLength = this.<Integer>readBit(10);
        var hasMailInfo = this.readBit();
        var hasCalendarInfo = this.readBit();
        var hasPetInfo = this.readBit();
        var hasGuildInfo = this.readBit();
        var hasLFGListSearchResult = this.readBit();
        var hasLFGListApplicant = this.readBit();
        var hasClubMessage = this.readBit();
        var hasClubFinderResult = this.readBit();
        var hasUnk910 = this.readBit();

        this.resetBitPos();

        if (hasClubMessage) {
            SupportTicketCommunityMessage communityMessage = new SupportTicketCommunityMessage();
            communityMessage.isPlayerUsingVoice = this.readBit();
            communityMessage = communityMessage;
            this.resetBitPos();
        }

        horusChatLog.read(this);

        note = this.readString(noteLength);

        if (hasMailInfo) {
            mailInfo = new SupportTicketMailInfo();
            mailInfo.getValue().read(this);
        }

        if (hasCalendarInfo) {
            calenderInfo = new SupportTicketCalendarEventInfo();
            calenderInfo.getValue().read(this);
        }

        if (hasPetInfo) {
            petInfo = new SupportTicketPetInfo();
            petInfo.getValue().read(this);
        }

        if (hasGuildInfo) {
            guildInfo = new SupportTicketGuildInfo();
            guildInfo.getValue().read(this);
        }

        if (hasLFGListSearchResult) {
            LFGListSearchResult = new SupportTicketLFGListSearchResult();
            LFGListSearchResult.getValue().read(this);
        }

        if (hasLFGListApplicant) {
            LFGListApplicant = new SupportTicketLFGListApplicant();
            LFGListApplicant.getValue().read(this);
        }

        if (hasClubFinderResult) {
            clubFinderResult = new SupportTicketClubFinderResult();
            clubFinderResult.getValue().read(this);
        }

        if (hasUnk910) {
            unused910 = new SupportTicketUnused910();
            unused910.getValue().read(this);
        }
    }

    public final static class SupportTicketChatLine {
        public long timestamp;
        public String text;

        public SupportTicketChatLine() {
        }

        public SupportTicketChatLine(WorldPacket data) {
            timestamp = data.readInt64();
            text = data.readString(data.<Integer>readBit(12));
        }

        public SupportTicketChatLine(long timestamp, String text) {
            timestamp = timestamp;
            text = text;
        }

        public void read(WorldPacket data) {
            timestamp = data.readUInt32();
            text = data.readString(data.<Integer>readBit(12));
        }

        public SupportTicketChatLine clone() {
            SupportTicketChatLine varCopy = new SupportTicketChatLine();

            varCopy.timestamp = this.timestamp;
            varCopy.text = this.text;

            return varCopy;
        }
    }

    public static class SupportTicketChatLog {
        public ArrayList<SupportTicketChatLine> lines = new ArrayList<>();
        public Integer reportLineIndex = null;

        public final void read(WorldPacket data) {
            var linesCount = data.readUInt32();
            var hasReportLineIndex = data.readBit();

            data.resetBitPos();

            for (int i = 0; i < linesCount; i++) {
                lines.add(new SupportTicketChatLine(data));
            }

            if (hasReportLineIndex) {
                reportLineIndex = data.readUInt32();
            }
        }
    }

    public final static class SupportTicketHorusChatLine {
        public long timestamp;
        public ObjectGuid authorGUID = ObjectGuid.EMPTY;
        public Long clubID = null;
        public ObjectGuid channelGUID = null;
        public SenderRealm realmAddress = null;
        public Integer slashCmd = null;
        public String text;

        public void read(WorldPacket data) {
            timestamp = data.readInt64();
            authorGUID = data.readPackedGuid();

            var hasClubID = data.readBit();
            var hasChannelGUID = data.readBit();
            var hasRealmAddress = data.readBit();
            var hasSlashCmd = data.readBit();
            var textLength = data.<Integer>readBit(12);

            if (hasClubID) {
                clubID = data.readUInt64();
            }

            if (hasChannelGUID) {
                channelGUID = data.readPackedGuid();
            }

            if (hasRealmAddress) {
                SenderRealm senderRealm = new SenderRealm();
                senderRealm.virtualRealmAddress = data.readUInt32();
                senderRealm.field_4 = data.readUInt16();
                senderRealm.field_6 = data.readUInt8();
                realmAddress = senderRealm;
            }

            if (hasSlashCmd) {
                slashCmd = data.readInt32();
            }

            text = data.readString(textLength);
        }

        public SupportTicketHorusChatLine clone() {
            SupportTicketHorusChatLine varCopy = new SupportTicketHorusChatLine();

            varCopy.timestamp = this.timestamp;
            varCopy.authorGUID = this.authorGUID;
            varCopy.clubID = this.clubID;
            varCopy.channelGUID = this.channelGUID;
            varCopy.realmAddress = this.realmAddress;
            varCopy.slashCmd = this.slashCmd;
            varCopy.text = this.text;

            return varCopy;
        }

        public final static class SenderRealm {
            public int virtualRealmAddress;
            public short field_4;
            public byte field_6;

            public SenderRealm clone() {
                SenderRealm varCopy = new SenderRealm();

                varCopy.virtualRealmAddress = this.virtualRealmAddress;
                varCopy.field_4 = this.field_4;
                varCopy.field_6 = this.field_6;

                return varCopy;
            }
        }
    }

    public static class SupportTicketHorusChatLog {
        public ArrayList<SupportTicketHorusChatLine> lines = new ArrayList<>();

        public final void read(WorldPacket data) {
            var linesCount = data.readUInt32();
            data.resetBitPos();

            for (int i = 0; i < linesCount; i++) {
                var chatLine = new SupportTicketHorusChatLine();
                chatLine.read(data);
                lines.add(chatLine);
            }
        }
    }

    public final static class SupportTicketMailInfo {
        public long mailID;
        public String mailSubject;
        public String mailBody;

        public void read(WorldPacket data) {
            mailID = data.readUInt64();
            var bodyLength = data.<Integer>readBit(13);
            var subjectLength = data.<Integer>readBit(9);

            mailBody = data.readString(bodyLength);
            mailSubject = data.readString(subjectLength);
        }

        public SupportTicketMailInfo clone() {
            SupportTicketMailInfo varCopy = new SupportTicketMailInfo();

            varCopy.mailID = this.mailID;
            varCopy.mailSubject = this.mailSubject;
            varCopy.mailBody = this.mailBody;

            return varCopy;
        }
    }

    public final static class SupportTicketCalendarEventInfo {
        public long eventID;
        public long inviteID;
        public String eventTitle;

        public void read(WorldPacket data) {
            eventID = data.readUInt64();
            inviteID = data.readUInt64();

            eventTitle = data.readString(data.<Byte>readBit(8));
        }

        public SupportTicketCalendarEventInfo clone() {
            SupportTicketCalendarEventInfo varCopy = new SupportTicketCalendarEventInfo();

            varCopy.eventID = this.eventID;
            varCopy.inviteID = this.inviteID;
            varCopy.eventTitle = this.eventTitle;

            return varCopy;
        }
    }

    public final static class SupportTicketPetInfo {
        public ObjectGuid petID = ObjectGuid.EMPTY;
        public String petName;

        public void read(WorldPacket data) {
            petID = data.readPackedGuid();

            petName = data.readString(data.<Byte>readBit(8));
        }

        public SupportTicketPetInfo clone() {
            SupportTicketPetInfo varCopy = new SupportTicketPetInfo();

            varCopy.petID = this.petID;
            varCopy.petName = this.petName;

            return varCopy;
        }
    }

    public final static class SupportTicketGuildInfo {
        public ObjectGuid guildID = ObjectGuid.EMPTY;
        public String guildName;

        public void read(WorldPacket data) {
            var nameLength = data.<Byte>readBit(8);
            guildID = data.readPackedGuid();

            guildName = data.readString(nameLength);
        }

        public SupportTicketGuildInfo clone() {
            SupportTicketGuildInfo varCopy = new SupportTicketGuildInfo();

            varCopy.guildID = this.guildID;
            varCopy.guildName = this.guildName;

            return varCopy;
        }
    }

    public final static class SupportTicketLFGListSearchResult {
        public rideTicket rideTicket;
        public int groupFinderActivityID;
        public ObjectGuid lastTitleAuthorGuid = ObjectGuid.EMPTY;
        public ObjectGuid lastDescriptionAuthorGuid = ObjectGuid.EMPTY;
        public ObjectGuid lastVoiceChatAuthorGuid = ObjectGuid.EMPTY;
        public ObjectGuid listingCreatorGuid = ObjectGuid.EMPTY;
        public ObjectGuid unknown735 = ObjectGuid.EMPTY;
        public String title;
        public String description;
        public String voiceChat;

        public void read(WorldPacket data) {
            rideTicket = new rideTicket();
            rideTicket.read(data);

            groupFinderActivityID = data.readUInt32();
            lastTitleAuthorGuid = data.readPackedGuid();
            lastDescriptionAuthorGuid = data.readPackedGuid();
            lastVoiceChatAuthorGuid = data.readPackedGuid();
            listingCreatorGuid = data.readPackedGuid();
            unknown735 = data.readPackedGuid();

            var titleLength = data.<Byte>readBit(10);
            var descriptionLength = data.<Byte>readBit(11);
            var voiceChatLength = data.<Byte>readBit(8);

            title = data.readString(titleLength);
            description = data.readString(descriptionLength);
            voiceChat = data.readString(voiceChatLength);
        }

        public SupportTicketLFGListSearchResult clone() {
            SupportTicketLFGListSearchResult varCopy = new SupportTicketLFGListSearchResult();

            varCopy.rideTicket = this.rideTicket;
            varCopy.groupFinderActivityID = this.groupFinderActivityID;
            varCopy.lastTitleAuthorGuid = this.lastTitleAuthorGuid;
            varCopy.lastDescriptionAuthorGuid = this.lastDescriptionAuthorGuid;
            varCopy.lastVoiceChatAuthorGuid = this.lastVoiceChatAuthorGuid;
            varCopy.listingCreatorGuid = this.listingCreatorGuid;
            varCopy.unknown735 = this.unknown735;
            varCopy.title = this.title;
            varCopy.description = this.description;
            varCopy.voiceChat = this.voiceChat;

            return varCopy;
        }
    }

    public final static class SupportTicketLFGListApplicant {
        public RideTicket rideTicket;
        public String comment;

        public void read(WorldPacket data) {
            rideTicket = new rideTicket();
            rideTicket.read(data);

            comment = data.readString(data.<Integer>readBit(9));
        }

        public SupportTicketLFGListApplicant clone() {
            SupportTicketLFGListApplicant varCopy = new SupportTicketLFGListApplicant();

            varCopy.rideTicket = this.rideTicket;
            varCopy.comment = this.comment;

            return varCopy;
        }
    }

    public final static class SupportTicketCommunityMessage {
        public boolean isPlayerUsingVoice;

        public SupportTicketCommunityMessage clone() {
            SupportTicketCommunityMessage varCopy = new SupportTicketCommunityMessage();

            varCopy.isPlayerUsingVoice = this.isPlayerUsingVoice;

            return varCopy;
        }
    }

    public final static class SupportTicketClubFinderResult {
        public long clubFinderPostingID;
        public long clubID;
        public ObjectGuid clubFinderGUID = ObjectGuid.EMPTY;
        public String clubName;

        public void read(WorldPacket data) {
            clubFinderPostingID = data.readUInt64();
            clubID = data.readUInt64();
            clubFinderGUID = data.readPackedGuid();
            clubName = data.readString(data.<Integer>readBit(12));
        }

        public SupportTicketClubFinderResult clone() {
            SupportTicketClubFinderResult varCopy = new SupportTicketClubFinderResult();

            varCopy.clubFinderPostingID = this.clubFinderPostingID;
            varCopy.clubID = this.clubID;
            varCopy.clubFinderGUID = this.clubFinderGUID;
            varCopy.clubName = this.clubName;

            return varCopy;
        }
    }

    public final static class SupportTicketUnused910 {
        public String field_0;
        public ObjectGuid field_104 = ObjectGuid.EMPTY;

        public void read(WorldPacket data) {
            var field_0Length = data.<Integer>readBit(7);
            field_104 = data.readPackedGuid();
            field_0 = data.readString(field_0Length);
        }

        public SupportTicketUnused910 clone() {
            SupportTicketUnused910 varCopy = new SupportTicketUnused910();

            varCopy.field_0 = this.field_0;
            varCopy.field_104 = this.field_104;

            return varCopy;
        }
    }
}
