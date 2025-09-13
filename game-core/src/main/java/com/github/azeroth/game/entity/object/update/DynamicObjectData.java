package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class DynamicObjectData extends UpdateMaskObject {
    public UpdateField<ObjectGuid> caster = new UpdateField<>(0, 1);

    public UpdateField<Byte> type = new UpdateField<>(0, 2);
    public UpdateField<SpellCastVisualField> spellVisual = new UpdateField<>(0, 3);

    public UpdateField<Integer> spellID = new UpdateField<>(0, 4);
    public UpdateField<Float> radius = new UpdateField<>(0, 5);

    public UpdateField<Integer> castTime = new UpdateField<>(0, 6);

    public DynamicObjectData() {
        super(7);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, DynamicObject owner, Player receiver) {
        data.writeGuid(caster);
        data.writeInt8(type);
        ((SpellCastVisualField) spellVisual).writeCreate(data, owner, receiver);
        data.writeInt32(spellID);
        data.writeFloat(radius);
        data.writeInt32(castTime);
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, DynamicObject owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, DynamicObject owner, Player receiver) {
        data.writeBits(getChangesMask().getBlock(0), 7);

        data.flushBits();

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(1)) {
                data.writeGuid(caster);
            }

            if (getChangesMask().get(2)) {
                data.writeInt8(type);
            }

            if (getChangesMask().get(3)) {
                ((SpellCastVisualField) spellVisual).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }

            if (getChangesMask().get(4)) {
                data.writeInt32(spellID);
            }

            if (getChangesMask().get(5)) {
                data.writeFloat(radius);
            }

            if (getChangesMask().get(6)) {
                data.writeInt32(castTime);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(caster);
        clearChangesMask(type);
        clearChangesMask(spellVisual);
        clearChangesMask(spellID);
        clearChangesMask(radius);
        clearChangesMask(castTime);
        getChangesMask().resetAll();
    }
}
