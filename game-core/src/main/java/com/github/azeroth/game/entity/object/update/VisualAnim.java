package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class VisualAnim extends UpdateMaskObject  {
    public FieldType<Boolean> field_C = new FieldType<>(0, 1);
    public FieldType<Integer> animationDataID = new FieldType<>(0, 2);
    public FieldType<Integer> animKitID = new FieldType<>(0, 3);
    public FieldType<Integer> animProgress = new FieldType<>(0, 4);

    public VisualAnim() {
        super(5);
    }

    public final void writeCreate(WorldPacket data, AreaTrigger owner, Player receiver) {
        data.writeInt32(animationDataID);
        data.writeInt32(animKitID);
        data.writeInt32(animProgress);
        data.writeBit(field_C);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, AreaTrigger owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 5);

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBit(field_C);
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                data.writeInt32(animationDataID);
            }

            if (changesMask.get(3)) {
                data.writeInt32(animKitID);
            }

            if (changesMask.get(4)) {
                data.writeInt32(animProgress);
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(field_C);
        clearChangesMask(animationDataID);
        clearChangesMask(animKitID);
        clearChangesMask(animProgress);
        getChangesMask().resetAll();
    }
}
