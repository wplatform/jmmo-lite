package com.github.azeroth.game.networking.packet.social;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.entity.player.FriendInfo;
import com.github.azeroth.game.entity.player.FriendStatus;
import com.github.azeroth.game.entity.player.FriendsResult;
import com.github.azeroth.game.networking.ServerPacket;

public class FriendStatusPkt extends ServerPacket {

    public int virtualRealmAddress;
    public String notes;
    public PlayerClass classID = playerClass.NONE;
    public Friendstatus status = FriendStatus.values()[0];
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public ObjectGuid wowAccountGuid = ObjectGuid.EMPTY;

    public int level;

    public int areaID;
    public FriendsResult friendResult = FriendsResult.values()[0];
    public boolean mobile;

    public FriendStatusPkt() {
        super(ServerOpcode.FriendStatus);
    }

    public final void initialize(ObjectGuid guid, FriendsResult result, FriendInfo friendInfo) {
        virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        notes = friendInfo.note;
        classID = friendInfo.class;
        status = friendInfo.status;
        UUID = guid;
        wowAccountGuid = friendInfo.wowAccountGuid;
        level = friendInfo.level;
        areaID = friendInfo.area;
        friendResult = result;
    }

    @Override
    public void write() {
        this.writeInt8((byte) friendResult.getValue());
        this.writeGuid(UUID);
        this.writeGuid(wowAccountGuid);
        this.writeInt32(virtualRealmAddress);
        this.writeInt8((byte) status.getValue());
        this.writeInt32(areaID);
        this.writeInt32(level);
        this.writeInt32((int) classID.getValue());
        this.writeBits(notes.getBytes().length, 10);
        this.writeBit(mobile);
        this.flushBits();
        this.writeString(notes);
    }
}
