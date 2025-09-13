package com.github.azeroth.game.entity.corpse;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.loot.Loot;

import com.github.azeroth.game.networking.WorldPacket;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;

@Getter
public class Corpse extends WorldObject {


    private final CorpseType type;
    private long time;
    private CellCoord cellCoord; // gride for corpse position for fast search

    private com.github.azeroth.game.entity.corpseData corpseData;
    private loot loot;
    private Player lootRecipient;

    public Corpse() {
        this(CorpseType.Bones);
    }

    public Corpse(CorpseType type) {
        super(type != CorpseType.Bones);
        type = type;
        setObjectTypeId(TypeId.Corpse);
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.Corpse.getValue()));

        updateFlag.stationary = true;

        setCorpseData(new corpseData());

        time = gameTime.GetGameTime();
    }

    public static void deleteFromDB(ObjectGuid ownerGuid, SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CORPSE);
        stmt.AddValue(0, ownerGuid.getCounter());
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CORPSE_PHASES);
        stmt.AddValue(0, ownerGuid.getCounter());
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CORPSE_CUSTOMIZATIONS);
        stmt.AddValue(0, ownerGuid.getCounter());
        DB.characters.ExecuteOrAppend(trans, stmt);
    }

    public final CorpseData getCorpseData() {
        return corpseData;
    }

    public final void setCorpseData(CorpseData value) {
        corpseData = value;
    }

    public final Loot getLoot() {
        return loot;
    }

    public final void setLoot(Loot value) {
        loot = value;
    }

    public final Player getLootRecipient() {
        return lootRecipient;
    }

    public final void setLootRecipient(Player value) {
        lootRecipient = value;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getCorpseData().owner;
    }

    public final void setOwnerGUID(ObjectGuid owner) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().owner), owner);
    }

    @Override
    public int getFaction() {
        return (int) (int) getCorpseData().factionTemplate;
    }

    @Override
    public void setFaction(int value) {
        setFactionTemplate(value);
    }

    @Override
    public void addToWorld() {
        // Register the corpse for guid lookup
        if (!isInWorld()) {

            getMap().getObjectsStore().TryAdd(getGUID(), this);
        }

        super.addToWorld();
    }

    @Override
    public void removeFromWorld() {
        // Remove the corpse from the accessor
        if (isInWorld()) {
            tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

            getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
            _ = tempOut__.outArgValue;
        }

        super.removeFromWorld();
    }

    public final boolean create(long guidlow, Map map) {
        create(ObjectGuid.create(HighGuid.Corpse, map.getId(), 0, guidlow));

        return true;
    }

    public final boolean create(long guidlow, Player owner) {
        getLocation().relocate(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), owner.getLocation().getO());

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.player, "Corpse (guidlow {0}, owner {1}) not created. Suggested coordinates isn't valid (X: {2} Y: {3})", guidlow, owner.getName(), owner.getLocation().getX(), owner.getLocation().getY());

            return false;
        }

        create(ObjectGuid.create(HighGuid.Corpse, owner.getLocation().getMapId(), 0, guidlow));

        setObjectScale(1);
        setOwnerGUID(owner.getGUID());

        cellCoord = MapDefine.computeCellCoord(getLocation().getX(), getLocation().getY());

        PhasingHandler.inheritPhaseShift(this, owner);

        return true;
    }

    @Override
    public void update(int diff) {
        super.update(diff);

        if (getLoot() != null) {
            getLoot().update();
        }
    }

    public final void saveToDB() {
        // prevent DB data inconsistence problems and duplicates
        SQLTransaction trans = new SQLTransaction();
        deleteFromDB(trans);

        StringBuilder items = new StringBuilder();

        for (var i = 0; i < getCorpseData().items.getSize(); ++i) {
            items.append(String.format("%1$s ", getCorpseData().items.get(i)));
        }

        byte index = 0;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CORPSE);
        stmt.AddValue(index++, getOwnerGUID().getCounter()); // guid
        stmt.AddValue(index++, getLocation().getX()); // posX
        stmt.AddValue(index++, getLocation().getY()); // posY
        stmt.AddValue(index++, getLocation().getZ()); // posZ
        stmt.AddValue(index++, getLocation().getO()); // orientation
        stmt.AddValue(index++, getLocation().getMapId()); // mapId
        stmt.AddValue(index++, (int) getCorpseData().displayID); // displayId
        stmt.AddValue(index++, items.toString()); // itemCache
        stmt.AddValue(index++, (byte) getCorpseData().raceID); // race
        stmt.AddValue(index++, (byte) getCorpseData(). class); // class
        stmt.AddValue(index++, (byte) getCorpseData().sex); // gender
        stmt.AddValue(index++, (int) getCorpseData().flags); // flags
        stmt.AddValue(index++, (int) getCorpseData().dynamicFlags); // dynFlags
        stmt.AddValue(index++, (int) time); // time
        stmt.AddValue(index++, (int) getCorpseType().getValue()); // corpseType
        stmt.AddValue(index++, getInstanceId()); // instanceId
        trans.append(stmt);

        for (var phaseId : getPhaseShift().phases.keySet()) {
            index = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CORPSE_PHASES);
            stmt.AddValue(index++, getOwnerGUID().getCounter()); // OwnerGuid
            stmt.AddValue(index++, phaseId); // PhaseId
            trans.append(stmt);
        }

        for (var customization : getCorpseData().customizations) {
            index = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CORPSE_CUSTOMIZATIONS);
            stmt.AddValue(index++, getOwnerGUID().getCounter()); // OwnerGuid
            stmt.AddValue(index++, customization.chrCustomizationOptionID);
            stmt.AddValue(index++, customization.chrCustomizationChoiceID);
            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);
    }

    public final void deleteFromDB(SQLTransaction trans) {
        deleteFromDB(getOwnerGUID(), trans);
    }

    public final boolean loadCorpseFromDB(long guid, SQLFields field) {
        //        0     1     2     3            4      5          6          7     8      9       10     11        12    13          14          15
        // SELECT posX, posY, posZ, orientation, mapId, displayId, itemCache, race, class, gender, flags, dynFlags, time, corpseType, instanceId, guid FROM corpse WHERE mapId = ? AND instanceId = ?

        var posX = field.<Float>Read(0);
        var posY = field.<Float>Read(1);
        var posZ = field.<Float>Read(2);
        var o = field.<Float>Read(3);
        var mapId = field.<SHORT>Read(4);

        create(ObjectGuid.create(HighGuid.Corpse, mapId, 0, guid));

        setObjectScale(1.0f);
        setDisplayId(field.<Integer>Read(5));
        LocalizedString items = new LocalizedString();

        if (items.length == getCorpseData().items.getSize()) {
            for (int index = 0; index < getCorpseData().items.getSize(); ++index) {
                setItem(index, Integer.parseInt(items.get((int) index)));
            }
        }

        setRace(field.<Byte>Read(7));
        setClass(field.<Byte>Read(8));
        setSex(field.<Byte>Read(9));
        replaceAllFlags(CorpseFlags.forValue(field.<Byte>Read(10)));
        replaceAllCorpseDynamicFlags(CorpseDynFlags.forValue(field.<Byte>Read(11)));
        setOwnerGUID(ObjectGuid.create(HighGuid.Player, field.<Long>Read(15)));
        setFactionTemplate(CliDB.ChrRacesStorage.get(getCorpseData().raceID).factionID);

        time = field.<Integer>Read(12);

        var instanceId = field.<Integer>Read(14);

        // place
        setLocationInstanceId(instanceId);
        getLocation().setMapId(mapId);
        getLocation().relocate(posX, posY, posZ, o);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.player, "Corpse ({0}, owner: {1}) is not created, given coordinates are not valid (X: {2}, Y: {3}, Z: {4})", getGUID().toString(), getOwnerGUID().toString(), posX, posY, posZ);

            return false;
        }

        cellCoord = MapDefine.computeCellCoord(getLocation().getX(), getLocation().getY());

        return true;
    }

    public final boolean isExpired(long t) {
        // Deleted character
        if (!global.getCharacterCacheStorage().hasCharacterCacheEntry(getOwnerGUID())) {
            return true;
        }

        if (type == CorpseType.Bones) {
            return time < t - 60 * time.Minute;
        } else {
            return time < t - 3 * time.Day;
        }
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        getObjectData().writeCreate(buffer, flags, this, target);
        getCorpseData().writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize() + 1);
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

        if (getValues().hasChanged(TypeId.Corpse)) {
            getCorpseData().writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(getCorpseData());
        super.clearUpdateMask(remove);
    }

    public final CorpseDynFlags getCorpseDynamicFlags() {
        return CorpseDynFlags.forValue((int) getCorpseData().dynamicFlags);
    }

    public final boolean hasCorpseDynamicFlag(CorpseDynFlags flag) {
        return (getCorpseData().dynamicFlags & (int) flag.getValue()) != 0;
    }

    public final void setCorpseDynamicFlag(CorpseDynFlags dynamicFlags) {
        setUpdateFieldFlagValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().dynamicFlags), (int) dynamicFlags.getValue());
    }

    public final void removeCorpseDynamicFlag(CorpseDynFlags dynamicFlags) {
        removeUpdateFieldFlagValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().dynamicFlags), (int) dynamicFlags.getValue());
    }

    public final void replaceAllCorpseDynamicFlags(CorpseDynFlags dynamicFlags) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().dynamicFlags), (int) dynamicFlags.getValue());
    }

    public final void setPartyGUID(ObjectGuid partyGuid) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().partyGUID), partyGuid);
    }

    public final void setGuildGUID(ObjectGuid guildGuid) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().guildGUID), guildGuid);
    }


    public final void setDisplayId(int displayId) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().displayID), displayId);
    }


    public final void setRace(byte race) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().raceID), race);
    }


    public final void setClass(byte classId) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData(). class),classId);
    }


    public final void setSex(byte sex) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().sex), sex);
    }

    public final void replaceAllFlags(CorpseFlags flags) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().flags), (int) flags.getValue());
    }

    public final void setFactionTemplate(int factionTemplate) {
        setUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().factionTemplate), factionTemplate);
    }


    public final void setItem(int slot, int item) {

        setUpdateFieldValue(ref getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().items, (int) slot), item);
    }

    public final void setCustomizations(ArrayList<ChrCustomizationChoice> customizations) {
        clearDynamicUpdateFieldValues(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().customizations));

        for (var customization : customizations) {
            var newChoice = new ChrCustomizationChoice();
            newChoice.chrCustomizationOptionID = customization.chrCustomizationOptionID;
            newChoice.chrCustomizationChoiceID = customization.chrCustomizationChoiceID;
            addDynamicUpdateFieldValue(getValues().modifyValue(getCorpseData()).modifyValue(getCorpseData().customizations), newChoice);
        }
    }

    public final long getGhostTime() {
        return time;
    }

    public final void resetGhostTime() {
        time = gameTime.GetGameTime();
    }

    public final CorpseType getCorpseType() {
        return type;
    }

    public final CellCoord getCellCoord() {
        return cellCoord;
    }

    public final void setCellCoord(CellCoord cellCoord) {
        cellCoord = cellCoord;
    }

    @Override
    public Loot getLootForPlayer(Player player) {
        return getLoot();
    }

    private void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedCorpseMask, Player target) {
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        if (requestedCorpseMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().Corpse.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().Corpse.getValue())) {
            getCorpseData().writeUpdate(buffer, requestedCorpseMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final Corpse owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final CorpseData corpseData = new corpseData();

        public ValuesUpdateForPlayerWithMaskSender(Corpse owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), corpseData.getUpdateMask(), player);

            com.github.azeroth.game.networking.packet.UpdateObject packet;
            tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }
}
