package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Getter;

@Getter
public final class BitVector extends UpdateMaskObject {

    private final Dynamic<Long> values = new Dynamic<>(0, 1, this);

    public BitVector() {
        super(2);
    }


    public void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(values.size());
        for (Long value : values) {
            data.writeInt64(value);
        }
    }

    public void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {

        if (ignoreChangesMask)
            changesMask.setAll();

        data.writeBits(changesMask.getBlock(0), 2);

        if (changesMask.get(0) && changesMask.get(1)) {
            if (!ignoreChangesMask)
                values.writeUpdateMask(data);
            else
                writeCompleteDynamicFieldUpdateMask(values.size(), data);
        }
        data.flushBits();
        if (changesMask.get(0) && changesMask.get(1)) {
            for (int i = 0; i < values.size(); ++i) {
                if (values.hasChanged(i) || ignoreChangesMask) {
                    data.writeInt64(values.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {

    }
}
