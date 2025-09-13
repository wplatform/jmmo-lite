package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LFGRoleCheckUpdate extends ServerPacket {
    public byte partyIndex;
    public byte roleCheckStatus;
    public ArrayList<Integer> joinSlots = new ArrayList<>();
    public ArrayList<Long> bgQueueIDs = new ArrayList<>();
    public int groupFinderActivityID = 0;
    public ArrayList<LFGRoleCheckUpdateMember> members = new ArrayList<>();
    public boolean isBeginning;
    public boolean isRequeue;

    public LFGRoleCheckUpdate() {
        super(ServerOpcode.LfgRoleCheckUpdate);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeInt8(roleCheckStatus);
        this.writeInt32(joinSlots.size());
        this.writeInt32(bgQueueIDs.size());
        this.writeInt32(groupFinderActivityID);
        this.writeInt32(members.size());

        for (var slot : joinSlots) {
            this.writeInt32(slot);
        }

        for (var bgQueueID : bgQueueIDs) {
            this.writeInt64(bgQueueID);
        }

        this.writeBit(isBeginning);
        this.writeBit(isRequeue);
        this.flushBits();

        for (var member : members) {
            member.write(this);
        }
    }
}
