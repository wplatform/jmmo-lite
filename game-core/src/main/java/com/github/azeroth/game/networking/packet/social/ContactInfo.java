package com.github.azeroth.game.networking.packet.social;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.entity.player.FriendInfo;
import com.github.azeroth.game.entity.player.SocialFlag;
import com.github.azeroth.game.networking.WorldPacket;

public class ContactInfo {

    private final int virtualRealmAddr;

    private final int nativeRealmAddr;
    private final SocialFlag typeFlags;
    private final String notes;
    private final Friendstatus status;

    private final int areaID;

    private final int level;
    private final PlayerClass classID;
    private final boolean mobile;

    private final ObjectGuid UUID;
    private final ObjectGuid wowAccountGuid;

    public ContactInfo(ObjectGuid guid, FriendInfo friendInfo) {
        UUID = guid;
        wowAccountGuid = friendInfo.wowAccountGuid;
        virtualRealmAddr = global.getWorldMgr().getVirtualRealmAddress();
        nativeRealmAddr = global.getWorldMgr().getVirtualRealmAddress();
        typeFlags = friendInfo.flags;
        notes = friendInfo.note;
        status = friendInfo.status;
        areaID = friendInfo.area;
        level = friendInfo.level;
        classID = friendInfo.class;
    }

    public final void write(WorldPacket data) {
        data.writeGuid(UUID);
        data.writeGuid(wowAccountGuid);
        data.writeInt32(virtualRealmAddr);
        data.writeInt32(nativeRealmAddr);
        data.writeInt32(typeFlags.getValue());
        data.writeInt8((byte) status.getValue());
        data.writeInt32(areaID);
        data.writeInt32(level);
        data.writeInt32((int) classID.getValue());
        data.writeBits(notes.getBytes().length, 10);
        data.writeBit(mobile);
        data.flushBits();
        data.writeString(notes);
    }
}
