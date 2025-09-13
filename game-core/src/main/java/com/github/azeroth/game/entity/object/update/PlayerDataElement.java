package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Data;

@Data
public class PlayerDataElement {
    private int type;
    private float floatValue;
    private long int64Value;


    public void WriteCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeBits(type, 1);
        if (type == 1) {
            data.writeFloat(floatValue);
        }
        if (type == 0) {
            data.writeInt64(int64Value);
        }
        data.flushBits();
    }

    public void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        WriteCreate(data, owner, receiver);
    }
}
