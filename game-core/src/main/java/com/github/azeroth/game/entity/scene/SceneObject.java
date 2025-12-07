package com.github.azeroth.game.entity.scene;


import com.github.azeroth.game.entity.UpdateMask;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.object.enums.TypeMask;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.networking.WorldPacket;

public class SceneObject extends WorldObject {
    private final SceneObjectData sceneObjectData;
    private final Position stationaryposition = new Position();
    private ObjectGuid createdBySpellCast = ObjectGuid.EMPTY;

    public SceneObject() {
        super(false);
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.sceneObject.getValue()));
        setObjectTypeId(TypeId.SCENE_OBJECT);

        updateFlag.stationary = true;
        updateFlag.sceneObject = true;

        sceneObjectData = new sceneObjectData();
        stationaryPosition = new Position();
    }

    public static SceneObject createSceneObject(int sceneId, Unit creator, Position pos, ObjectGuid privateObjectOwner) {
        var sceneTemplate = global.getObjectMgr().getSceneTemplate(sceneId);

        if (sceneTemplate == null) {
            return null;
        }

        var lowGuid = creator.getMap().generateLowGuid(HighGuid.SceneObject);

        SceneObject sceneObject = new sceneObject();

        if (!sceneObject.create(lowGuid, sceneType.NORMAL, sceneId, sceneTemplate != null ? sceneTemplate.ScenePackageId : 0, creator.getMap(), creator, pos, privateObjectOwner)) {
            sceneObject.close();

            return null;
        }

        return sceneObject;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return sceneObjectData.createdBy;
    }

    @Override
    public int getFaction() {
        return 0;
    }

    @Override
    public float getStationaryX() {
        return stationaryPosition.getX();
    }

    @Override
    public float getStationaryY() {
        return stationaryPosition.getY();
    }

    @Override
    public float getStationaryZ() {
        return stationaryPosition.getZ();
    }

    @Override
    public float getStationaryO() {
        return stationaryPosition.getO();
    }

    @Override
    public void addToWorld() {
        if (!isInWorld()) {

            getMap().getObjectsStore().TryAdd(getGUID(), this);
            super.addToWorld();
        }
    }

    @Override
    public void removeFromWorld() {
        if (isInWorld()) {
            super.removeFromWorld();
            tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

            getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
            _ = tempOut__.outArgValue;
        }
    }

    @Override
    public void update(int diff) {
        super.update(diff);

        if (shouldBeRemoved()) {
            remove();
        }
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        getObjectData().writeCreate(buffer, flags, this, target);
        sceneObjectData.writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize());
        data.writeInt8((byte) flags.getValue());
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

        if (getValues().hasChanged(TypeId.sceneObject)) {
            sceneObjectData.writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(sceneObjectData);
        super.clearUpdateMask(remove);
    }

    public final void setCreatedBySpellCast(ObjectGuid castId) {
        createdBySpellCast = castId;
    }

    private void remove() {
        if (isInWorld()) {
            addObjectToRemoveList();
        }
    }

    private boolean shouldBeRemoved() {
        var creator = global.getObjAccessor().GetUnit(this, getOwnerGUID());

        if (creator == null) {
            return true;
        }

        if (!createdBySpellCast.isEmpty()) {
            // search for a dummy aura on creator

            var linkedAura = creator.getAuraQuery().hasSpellId(createdBySpellCast.getEntry()).hasCastId(createdBySpellCast).getResults().FirstOrDefault();

            if (linkedAura == null) {
                return true;
            }
        }

        return false;
    }

    private boolean create(long lowGuid, SceneType type, int sceneId, int scriptPackageId, Map map, Unit creator, Position pos, ObjectGuid privateObjectOwner) {
        setMap(map);
        getLocation().relocate(pos);
        relocateStationaryPosition(pos);

        setPrivateObjectOwner(privateObjectOwner);

        create(ObjectGuid.create(HighGuid.SceneObject, getLocation().getMapId(), sceneId, lowGuid));
        PhasingHandler.inheritPhaseShift(this, creator);

        setEntry(scriptPackageId);
        setObjectScale(1.0f);

        setUpdateFieldValue(getValues().modifyValue(sceneObjectData).modifyValue(sceneObjectData.scriptPackageID), (int) scriptPackageId);
        setUpdateFieldValue(getValues().modifyValue(sceneObjectData).modifyValue(sceneObjectData.rndSeedVal), gameTime.GetGameTimeMS());
        setUpdateFieldValue(getValues().modifyValue(sceneObjectData).modifyValue(sceneObjectData.createdBy), creator.getGUID());
        setUpdateFieldValue(getValues().modifyValue(sceneObjectData).modifyValue(sceneObjectData.sceneType), (int) type.getValue());

        if (!getMap().addToMap(this)) {
            return false;
        }

        return true;
    }

    private void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedSceneObjectMask, Player target) {
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        if (requestedSceneObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().sceneObject.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().sceneObject.getValue())) {
            sceneObjectData.writeUpdate(buffer, requestedSceneObjectMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    private void relocateStationaryPosition(Position pos) {
        stationaryPosition.relocate(pos);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final SceneObject owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final sceneObjectData sceneObjectData = new sceneObjectData();

        public ValuesUpdateForPlayerWithMaskSender(SceneObject owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), sceneObjectData.getUpdateMask(), player);

            com.github.azeroth.game.networking.packet.UpdateObject packet;
            tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }
}
