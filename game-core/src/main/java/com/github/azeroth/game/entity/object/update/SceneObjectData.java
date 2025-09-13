package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class SceneObjectData extends UpdateMaskObject {
    public UpdateField<Integer> scriptPackageID = new UpdateField<>(0, 1);
    public UpdateField<Integer> rndSeedVal = new UpdateField<>(0, 2);
    public UpdateField<ObjectGuid> createdBy = new UpdateField<>(0, 3);
    public UpdateField<Integer> sceneType = new UpdateField<>(0, 4);

    protected SceneObjectData() {
        super(5);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, WorldObject owner, Player receiver) {
        data.writeInt32(scriptPackageID);
        data.writeInt32(rndSeedVal);
        data.writeGuid(createdBy);
        data.writeInt32(sceneType);
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, WorldObject owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, WorldObject owner, Player receiver) {
        data.writeBits(getChangesMask().getBlock(0), 5);

        data.flushBits();

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(1)) {
                data.writeInt32(scriptPackageID);
            }

            if (getChangesMask().get(2)) {
                data.writeInt32(rndSeedVal);
            }

            if (getChangesMask().get(3)) {
                data.writeGuid(createdBy);
            }

            if (getChangesMask().get(4)) {
                data.writeInt32(sceneType);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(scriptPackageID);
        clearChangesMask(rndSeedVal);
        clearChangesMask(createdBy);
        clearChangesMask(sceneType);
        getChangesMask().resetAll();
    }
}
