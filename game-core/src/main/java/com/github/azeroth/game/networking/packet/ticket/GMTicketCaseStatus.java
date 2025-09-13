package com.github.azeroth.game.networking.packet.ticket;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class GMTicketCaseStatus extends ServerPacket {
    public ArrayList<GMTicketCase> cases = new ArrayList<>();

    public GMTicketCaseStatus() {
        super(ServerOpCode.SMSG_GM_TICKET_CASE_STATUS);
    }

    @Override
    public void write() {
        this.writeInt32(cases.size());

        for (var c : cases) {
            this.writeInt32(c.caseID);
            this.writeInt64(c.caseOpened);
            this.writeInt32(c.caseStatus);
            this.writeInt16(c.cfgRealmID);
            this.writeInt64(c.characterID);
            this.writeInt32(c.waitTimeOverrideMinutes);

            this.writeBits(c.url.getBytes().length, 11);
            this.writeBits(c.waitTimeOverrideMessage.getBytes().length, 10);

            this.writeString(c.url);
            this.writeString(c.waitTimeOverrideMessage);
        }
    }

    public final static class GMTicketCase {
        public int caseID;
        public long caseOpened;
        public int caseStatus;
        public short cfgRealmID;
        public long characterID;
        public int waitTimeOverrideMinutes;
        public String url;
        public String waitTimeOverrideMessage;

        public GMTicketCase clone() {
            GMTicketCase varCopy = new GMTicketCase();

            varCopy.caseID = this.caseID;
            varCopy.caseOpened = this.caseOpened;
            varCopy.caseStatus = this.caseStatus;
            varCopy.cfgRealmID = this.cfgRealmID;
            varCopy.characterID = this.characterID;
            varCopy.waitTimeOverrideMinutes = this.waitTimeOverrideMinutes;
            varCopy.url = this.url;
            varCopy.waitTimeOverrideMessage = this.waitTimeOverrideMessage;

            return varCopy;
        }
    }
}
