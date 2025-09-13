package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.WorldPacket;


class BattlegroundCapturePointInfo {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public Vector2 pos;
    public BattlegroundCapturePointstate state = BattlegroundCapturePointState.Neutral;
    public long captureTime;
    public duration captureTotalduration = new duration();

    public final void write(WorldPacket data) {
        data.writeGuid(UUID);
        data.writeVector2(pos);
        data.writeInt8((byte) state.getValue());

        if (state == BattlegroundCapturePointState.ContestedHorde || state == BattlegroundCapturePointState.ContestedAlliance) {
            data.writeInt64(captureTime);
            data.writeInt32((int) captureTotalDuration.TotalMilliseconds);
        }
    }
}
