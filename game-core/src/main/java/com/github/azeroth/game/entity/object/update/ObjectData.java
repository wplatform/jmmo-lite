package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Getter;


@Getter
public final class ObjectData extends UpdateMaskObject {

    private int entry;
    private int dynamicFlags;
    private float scale;

    public ObjectData() {
        super(4);
    }


    public void setEntry(int entry) {
        if (entry != this.entry) {
            this.entry = entry;
            fireMarkChanged(0, 1);
        }

    }

    public void setDynamicFlags(int dynamicFlags) {
        if (dynamicFlags != this.dynamicFlags) {
            fireMarkChanged(0, 2);
            this.dynamicFlags = dynamicFlags;
        }
    }

    public void setScale(float scale) {
        if (scale != this.scale) {
            fireMarkChanged(0, 3);
            this.scale = scale;
        }
    }

    public void writeCreate(WorldPacket data, EnumFlag<UpdateFieldFlag> fieldVisibilityFlags, Object owner, Player receiver) {
        data.writeInt32(entry);// << int32(ViewerDependentValue<EntryIDTag>::GetValue(this, owner, receiver));
        data.writeInt32(dynamicFlags);//data << uint32(ViewerDependentValue<DynamicFlagsTag>::GetValue(this, owner, receiver));
        data.writeFloat(scale);
    }

    public void writeUpdate(WorldPacket data, EnumFlag<UpdateFieldFlag> fieldVisibilityFlags, Object owner, Player receiver) {
        writeUpdate(data, changesMask, false, owner, receiver);
    }

    public void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Object owner, Player receiver) {

        data.writeBits(changesMask.getBlock(0), 4);

        data.flushBits();
        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeInt32(entry);// << int32(ViewerDependentValue<EntryIDTag>::GetValue(this, owner, receiver));
            }
            if (changesMask.get(2)) {
                data.writeInt32(dynamicFlags);// << uint32(ViewerDependentValue<DynamicFlagsTag>::GetValue(this, owner, receiver));
            }
            if (changesMask.get(3)) {
                data.writeFloat(scale);// << float(Scale);
            }
        }
    }

    public void clearChangesMask() {
        changesMask.resetAll();
    }

}
