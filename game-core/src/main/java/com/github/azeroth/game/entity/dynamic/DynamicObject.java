package com.github.azeroth.game.entity.dynamic;


import com.github.azeroth.game.entity.SpellCastVisualField;
import com.github.azeroth.game.entity.object.GirdObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.CellMoveState;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.listener.interfaces.idynamicobject.IDynamicObjectOnUpdate;
import com.github.azeroth.game.spell.Aura;
import com.github.azeroth.game.spell.AuraRemoveMode;
import com.github.azeroth.game.spell.SpellInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
public class DynamicObject extends WorldObject implements GirdObject {

    private final DynamicObjectData dynamicObjectData;
    private Aura aura;
    private Aura removedAura;
    private Unit caster;
    private int duration; // for non-aura dynobjects
    private boolean isViewpoint;
    private Cell currentCell;
    private CellMoveState moveState;
    private Position newPosition = new Position();

    public DynamicObject(boolean isWorldObject) {
        super(isWorldObject);
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.DynamicObject.getValue()));
        setObjectTypeId(TypeId.DynamicObject);

        updateFlag.stationary = true;

        dynamicObjectData = new dynamicObjectData();
    }


    @Override
    public int getFaction() {
        return caster.getFaction();
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getCasterGUID();
    }

    @Override
    public void close() throws IOException {
        // make sure all references were properly removed
        removedAura = null;

        super.close();
    }

    @Override
    public void addToWorld() {
        // Register the dynamicObject for guid lookup and for caster
        if (!isInWorld()) {

            getMap().getObjectsStore().TryAdd(getGUID(), this);
            super.addToWorld();
            bindToCaster();
        }
    }

    @Override
    public void removeFromWorld() {
        // Remove the dynamicObject from the accessor and from all lists of objects in world
        if (isInWorld()) {
            if (isViewpoint) {
                removeCasterViewpoint();
            }

            if (aura != null) {
                removeAura();
            }

            // dynobj could get removed in aura.RemoveAura
            if (!isInWorld()) {
                return;
            }

            unbindFromCaster();
            super.removeFromWorld();
            tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

            getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
            _ = tempOut__.outArgValue;
        }
    }


    public final boolean createDynamicObject(long guidlow, Unit caster, SpellInfo spell, Position pos, float radius, DynamicObjectType type, SpellCastVisualField spellVisual) {
        setMap(caster.getMap());
        getLocation().relocate(pos);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.Server, "DynamicObject (spell {0}) not created. Suggested coordinates isn't valid (X: {1} Y: {2})", spell.getId(), getLocation().getX(), getLocation().getY());

            return false;
        }

        create(ObjectGuid.create(HighGuid.DynamicObject, getLocation().getMapId(), spell.getId(), guidlow));
        PhasingHandler.inheritPhaseShift(this, caster);

        updatePositionData();
        setZoneScript();

        setEntry(spell.getId());
        setObjectScale(1f);

        setUpdateFieldValue(getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.caster), caster.getGUID());
        setUpdateFieldValue(getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.type), (byte) type.getValue());

        SpellCastVisualField spellCastVisual = getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.spellVisual);
        tangible.RefObject<Integer> tempRef_SpellXSpellVisualID = new tangible.RefObject<Integer>(spellCastVisual.spellXSpellVisualID);
        setUpdateFieldValue(tempRef_SpellXSpellVisualID, spellVisual.spellXSpellVisualID);
        spellCastVisual.spellXSpellVisualID = tempRef_SpellXSpellVisualID.refArgValue;
        tangible.RefObject<Integer> tempRef_ScriptVisualID = new tangible.RefObject<Integer>(spellCastVisual.scriptVisualID);
        setUpdateFieldValue(tempRef_ScriptVisualID, spellVisual.scriptVisualID);
        spellCastVisual.scriptVisualID = tempRef_ScriptVisualID.refArgValue;

        setUpdateFieldValue(getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.spellID), spell.getId());
        setUpdateFieldValue(getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.radius), radius);
        setUpdateFieldValue(getValues().modifyValue(dynamicObjectData).modifyValue(dynamicObjectData.castTime), gameTime.GetGameTimeMS());

        if (isWorldObject()) {
            setActive(true); //must before add to map to be put in world container
        }

        var transport = caster.getTransport();

        if (transport != null) {
            var newPos = pos.Copy();
            transport.calculatePassengerOffset(newPos);
            getMovementInfo().transport.pos.relocate(newPos);

            // This object must be added to transport before adding to map for the client to properly display it
            transport.addPassenger(this);
        }

        if (!getMap().addToMap(this)) {
            // Returning false will cause the object to be deleted - remove from transport
            if (transport != null) {
                transport.removePassenger(this);
            }

            return false;
        }

        return true;
    }


    @Override
    public void update(int diff) {
        // caster has to be always available and in the same map
        var expired = false;

        if (aura != null) {
            if (!aura.isRemoved()) {
                aura.updateOwner(diff, this);
            }

            // _aura may be set to null in aura.UpdateOwner call
            if (aura != null && (aura.isRemoved() || aura.isExpired())) {
                expired = true;
            }
        } else {
            if (getDuration() > diff) {
                _duration -= diff;
            } else {
                expired = true;
            }
        }

        if (expired) {
            remove();
        } else {
            global.getScriptMgr().<IDynamicObjectOnUpdate>ForEach(p -> p.onUpdate(this, diff));
        }
    }

    public final void remove() {
        if (isInWorld()) {
            addObjectToRemoveList();
        }
    }

    public final void delay(int delaytime) {
        setDuration(getDuration() - delaytime);
    }

    public final void setAura(Aura aura) {
        aura = aura;
    }

    public final void setCasterViewpoint() {
        var caster = caster.toPlayer();

        if (caster != null) {
            caster.setViewpoint(this, true);
            isViewpoint = true;
        }
    }

    public final SpellInfo getSpellInfo() {
        return global.getSpellMgr().getSpellInfo(getSpellId(), getMap().getDifficultyID());
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt8((byte) flags.getValue());
        getObjectData().writeCreate(buffer, flags, this, target);
        dynamicObjectData.writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt32(getValues().getChangedObjectTypeMask());

        if (getValues().hasChanged(TypeId.object)) {
            getObjectData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.DynamicObject)) {
            dynamicObjectData.writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(dynamicObjectData);
        super.clearUpdateMask(remove);
    }

    public final Unit getCaster() {
        return caster;
    }

    public final int getSpellId() {
        return dynamicObjectData.spellID;
    }

    public final ObjectGuid getCasterGUID() {
        return dynamicObjectData.caster;
    }

    public final float getRadius() {
        return dynamicObjectData.radius;
    }

    private int getDuration() {
        if (aura == null) {
            return duration;
        } else {
            return aura.getDuration();
        }
    }

    public final void setDuration(int newDuration) {
        if (aura == null) {
            duration = newDuration;
        } else {
            aura.setDuration(newDuration);
        }
    }

    private void removeAura() {
        removedAura = aura;
        aura = null;

        if (!removedAura.isRemoved()) {
            removedAura._Remove(AuraRemoveMode.Default);
        }
    }

    private void removeCasterViewpoint() {
        var caster = caster.toPlayer();

        if (caster != null) {
            caster.setViewpoint(this, false);
            isViewpoint = false;
        }
    }

    private void bindToCaster() {
        caster = global.getObjAccessor().GetUnit(this, getCasterGUID());
        caster._RegisterDynObject(this);
    }

    private void unbindFromCaster() {
        caster._UnregisterDynObject(this);
        caster = null;
    }

    private void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedDynamicObjectMask, Player target) {
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        if (requestedDynamicObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().DynamicObject.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().DynamicObject.getValue())) {
            dynamicObjectData.writeUpdate(buffer, requestedDynamicObjectMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    @Override
    public final void setNewCellPosition(float x, float y, float z, float o) {
        moveState = CellMoveState.ACTIVE;
        newPosition.relocate(x, y, z, o);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final DynamicObject owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final dynamicObjectData dynamicObjectData = new dynamicObjectData();

        public ValuesUpdateForPlayerWithMaskSender(DynamicObject owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), dynamicObjectData.getUpdateMask(), player);

            com.github.azeroth.game.networking.packet.UpdateObject packet;
            tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }
}
