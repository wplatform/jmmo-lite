package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class ArenaCooldown extends UpdateMaskObject {
    public FieldType<Integer> spellID = new FieldType<>(0, 1);
    public FieldType<Integer> charges = new FieldType<>(0, 2);
    public FieldType<Integer> flags = new FieldType<>(0, 3);
    public FieldType<Integer> startTime = new FieldType<>(0, 4);
    public FieldType<Integer> endTime = new FieldType<>(0, 5);
    public FieldType<Integer> nextChargeTime = new FieldType<>(0, 6);
    public FieldType<Byte> maxCharges = new FieldType<>(0, 7);

    public ArenaCooldown() {
        super(8);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(spellID);
        data.writeInt32(charges);
        data.writeInt32(flags);
        data.writeInt32(startTime);
        data.writeInt32(endTime);
        data.writeInt32(nextChargeTime);
        data.writeInt8(maxCharges);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 8);

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeInt32(spellID);
            }

            if (changesMask.get(2)) {
                data.writeInt32(charges);
            }

            if (changesMask.get(3)) {
                data.writeInt32(flags);
            }

            if (changesMask.get(4)) {
                data.writeInt32(startTime);
            }

            if (changesMask.get(5)) {
                data.writeInt32(endTime);
            }

            if (changesMask.get(6)) {
                data.writeInt32(nextChargeTime);
            }

            if (changesMask.get(7)) {
                data.writeInt8(maxCharges);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(spellID);
        clearChangesMask(charges);
        clearChangesMask(flags);
        clearChangesMask(startTime);
        clearChangesMask(endTime);
        clearChangesMask(nextChargeTime);
        clearChangesMask(maxCharges);
        getChangesMask().resetAll();
    }
}
