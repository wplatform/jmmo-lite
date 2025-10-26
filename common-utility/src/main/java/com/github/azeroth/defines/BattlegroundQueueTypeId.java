package com.github.azeroth.defines;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BattlegroundQueueTypeId {
    public static final BattlegroundQueueTypeId BATTLEGROUND_QUEUE_NONE = new BattlegroundQueueTypeId((short) 0, (byte) 0, false, (byte) 0);

    final short battleMasterListId;
    final byte type;
    final boolean rated;
    final byte teamSize;

    static BattlegroundQueueTypeId FromPacked(long packedQueueId) {
        return new BattlegroundQueueTypeId((short) (packedQueueId & 0xFFFF), (byte) ((packedQueueId >> 16) & 0xF), ((packedQueueId >> 20) & 1) != 0, (byte) ((packedQueueId >> 24) & 0x3F));
    }

    long getPacked() {
        return (long) battleMasterListId
                | ((long) (type & 0xF) << 16)
                | ((long) (rated ? 1 : 0) << 20)
                | ((long) (teamSize & 0x3F) << 24)
                | 0x1F10000000000000L;
    }
}
