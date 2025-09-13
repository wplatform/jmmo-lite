package com.github.azeroth.game.map;


import com.github.azeroth.game.domain.map.enums.TransferAbortReason;

public class TransferAbortParams {
    public TransferAbortReason reason;
    public byte arg;
    public int mapDifficultyXConditionId;

    public TransferAbortParams(TransferAbortReason reason, byte arg) {
        this(reason, arg, 0);
    }

    public TransferAbortParams(TransferAbortReason reason) {
        this(reason, (byte)0, 0);
    }

    public TransferAbortParams() {
        this(TransferAbortReason.NONE, (byte) 0, 0);
    }

    public TransferAbortParams(TransferAbortReason reason, byte arg, int mapDifficultyXConditionId) {
        this.reason = reason;
        this.arg = arg;
        this.mapDifficultyXConditionId = mapDifficultyXConditionId;
    }

}
