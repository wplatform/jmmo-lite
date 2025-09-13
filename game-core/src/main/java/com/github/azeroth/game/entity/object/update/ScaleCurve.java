package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class ScaleCurve extends UpdateMaskObject {
    public FieldType<Boolean> overrideActive = new FieldType<>(0, 1);
    public FieldType<Integer> startTimeOffset = new FieldType<>(0, 2);
    public FieldType<Integer> parameterCurve = new FieldType<>(0, 3);
    public Array<Vector2> points = new Array<Vector2>(2, 4, 5);

    public ScaleCurve() {
        super(7);
    }

    public final void writeCreate(WorldPacket data, AreaTrigger owner, Player receiver) {
        data.writeInt32(startTimeOffset);

        for (var i = 0; i < 2; ++i) {
            data.writeVector2(points.get(i));
        }

        data.writeInt32(parameterCurve);
        data.writeBit((boolean) overrideActive);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, AreaTrigger owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 7);

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBit(overrideActive);
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                data.writeInt32(startTimeOffset);
            }

            if (changesMask.get(3)) {
                data.writeInt32(parameterCurve);
            }
        }

        if (changesMask.get(4)) {
            for (var i = 0; i < 2; ++i) {
                if (changesMask.get(5 + i)) {
                    data.writeVector2(points.get(i));
                }
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(overrideActive);
        clearChangesMask(startTimeOffset);
        clearChangesMask(parameterCurve);
        clearChangesMask(points);
        getChangesMask().resetAll();
    }
}
