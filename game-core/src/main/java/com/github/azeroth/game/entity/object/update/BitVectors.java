package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class BitVectors extends UpdateMaskObject {

    private Array<BitVector> values;

    public BitVectors() {
        super(14);
    }


    void writeCreate(WorldPacket data, Player owner, Player receiver) {
        for (int i = 0; i < 13; ++i) {
            values.get(i).writeCreate(data, owner, receiver);
        }
    }

    void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        if (ignoreChangesMask)
            changesMask.setAll();

        data.writeBits(changesMask.getBlocksMask(0), 1);
        if (changesMask.getBlock(0) != 0)
            data.writeBits(changesMask.getBlock(0), 32);

        data.flushBits();
        if (changesMask.get(0)) {
            for (int i = 0; i < 13; ++i) {
                if (changesMask.get(1 + i)) {
                    values.get(i).writeUpdate(data, ignoreChangesMask, owner, receiver);
                }
            }
        }
    }


    @Override
    public void clearChangesMask() {

    }
}
