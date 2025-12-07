package com.github.azeroth.game.entity.object;


import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.defines.Power;
import com.github.azeroth.defines.UnitDynFlag;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerCreatePropertiesFlag;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerOrbitInfo;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.object.enums.TypeMask;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.UnitMoveType;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.conversation.Conversation;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.gobject.Transport;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.object.update.*;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.PlayerDefine;
import com.github.azeroth.game.entity.player.enums.ActionButtonUpdateState;
import com.github.azeroth.game.entity.scene.SceneObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.movement.model.TransportInfo;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.movement.MovementIOUtil;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.world.WorldContext;
import com.github.azeroth.time.GameTime;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

import static com.github.azeroth.game.entity.object.update.UpdateFieldFlags.*;
import static com.github.azeroth.game.entity.object.update.UpdateFields.*;


@Getter
@Setter
public abstract class GenericObject {


    protected static final byte OFFSET_0 = 0;
    protected static final byte OFFSET_1 = 1;
    protected static final byte OFFSET_2 = 2;
    protected static final byte OFFSET_3 = 3;


    protected boolean objectUpdated;
    protected final TypeId objectTypeId;
    protected MovementInfo movementInfo;


    private boolean inWorld;
    private boolean newObject;
    private boolean destroyedObject;



    protected final EnumFlag<TypeMask> objectType;
    protected final EnumFlag<ObjectUpdateFlag> updateFlag;


    private final int[] int32Values;
    private final IntArray[] dynamicValues;

    private final BitSet changesMask;
    private final DynFieldChangeType[] dynamicChangesMask;
    private final BitSet[] dynamicChangesArrayMask;


    private final short valuesCount;
    private final short dynamicValuesCount;

    private int fieldNotifyFlags;

    protected final WorldContext worldContext;

    public GenericObject(WorldContext worldContext, ObjectGuid guid, TypeId objectTypeId,
                         EnumFlag<TypeMask> objectType, EnumFlag<ObjectUpdateFlag> updateFlag,
                         short valuesCount, short dynamicValuesCount) {
        this.worldContext = worldContext;
        this.objectTypeId = objectTypeId;
        this.objectType = objectType;
        this.updateFlag = updateFlag;
        this.valuesCount = valuesCount;
        this.dynamicValuesCount = dynamicValuesCount;
        int32Values = new int[valuesCount];
        Arrays.fill(int32Values, 0);
        dynamicValues = new IntArray[dynamicValuesCount];
        for (int i = 0; i < dynamicValuesCount; i++) {
            dynamicValues[i] = new IntArray();
        }
        changesMask = new BitSet(valuesCount);
        dynamicChangesMask = new DynFieldChangeType[dynamicValuesCount];
        Arrays.fill(dynamicChangesMask, DynFieldChangeType.UNCHANGED);
        dynamicChangesArrayMask = new BitSet[dynamicValuesCount];
        for (int i = 0; i < dynamicValuesCount; i++) {
            dynamicChangesArrayMask[i] = new BitSet();
        }

        objectUpdated = false;
        setGuidValue(OBJECT_FIELD_GUID, guid);
        setInt16Value(OBJECT_FIELD_TYPE, OFFSET_0, (short) updateFlag.getFlag());
    }


    public final ObjectGuid getGUID() {
        return getGuidValue(OBJECT_FIELD_GUID);
    }


    public final int getEntry() {
        return getInt32Value(OBJECT_FIELD_ENTRY);
    }

    public final void setEntry(int entry) {
        setInt32Value(OBJECT_FIELD_ENTRY, entry);
    }

    public float getObjectScale() {
        return getFloatValue(OBJECT_FIELD_SCALE_X);
    }

    public void setObjectScale(float scale) {
        setFloatValue(OBJECT_FIELD_SCALE_X, scale);
    }

    protected final int getDynamicFlags() {
        return getInt32Value(OBJECT_DYNAMIC_FLAGS);
    }

    protected final boolean hasDynamicFlag(int flag) {
        return hasFlag(OBJECT_DYNAMIC_FLAGS, flag);
    }

    protected final void setDynamicFlag(int flag) {
        setFlag(OBJECT_DYNAMIC_FLAGS, flag);
    }

    protected final void removeDynamicFlag(int flag) {
        removeFlag(OBJECT_DYNAMIC_FLAGS, flag);
    }

    protected final void replaceAllDynamicFlags(int flag) {
        setInt32Value(OBJECT_DYNAMIC_FLAGS, flag);
    }


    protected final boolean isType(TypeMask mask) {
        return objectType.hasFlag(mask);
    }


    protected void addToWorld() {
        if (inWorld)
            return;
        Objects.requireNonNull(int32Values);
        inWorld = true;

        // synchronize values mirror with values array (changes will send in updatecreate opcode any way
        Assert.isTrue(!objectUpdated);
        clearUpdateMask(false);
    }

    protected void removeFromWorld() {
        if (!inWorld)
            return;

        inWorld = false;

        // if we remove from world then sending changes not required
        clearUpdateMask(true);

    }

    protected void buildCreateUpdateBlockForPlayer(UpdateData data, Player target) {
        if (target == null) return;

        ObjectUpdateType updateType = newObject ? ObjectUpdateType.CREATE_OBJECT2 : ObjectUpdateType.CREATE_OBJECT;
        EnumFlag<ObjectUpdateFlag> flags = EnumFlag.of(updateFlag);
        // building packet for yourself
        if (target == this) {
            flags.addFlag(ObjectUpdateFlag.SELF);
        }

        switch (getGUID().highGuid()) {
            case Player:
            case Pet:
            case Corpse:
            case DynamicObject:
            case AreaTrigger:
            case Conversation:
            case SceneObject:
                updateType = ObjectUpdateType.CREATE_OBJECT2;
                break;
            case Creature:
            case Vehicle: {
                if (this instanceof TempSummon summon) {
                    if (summon.getSummonerGUID().isPlayer()) updateType = ObjectUpdateType.CREATE_OBJECT2;
                }


                break;
            }
            case GameObject: {
                if (toGameObject().getOwnerGUID().isPlayer()) updateType = ObjectUpdateType.CREATE_OBJECT2;
                break;
            }
            default:
                break;
        }

        if (this instanceof WorldObject worldObject) {
            if (!flags.hasFlag(ObjectUpdateFlag.LIVING)) {
                TransportInfo transport = worldObject.movementInfo.getTransport();
                if (transport != null && !transport.getGuid().isEmpty())
                    flags.addFlag(ObjectUpdateFlag.TRANSPORT_POSITION);
            }

            if (worldObject.getAIAnimKitId() != 0f || worldObject.getMovementAnimKitId() != 0f || worldObject.getMeleeAnimKitId() != 0f)
                flags.addFlag(ObjectUpdateFlag.ANIM_KITS);
        }

        if (flags.hasFlag(ObjectUpdateFlag.STATIONARY_POSITION)) {
            // UPDATETYPE_CREATE_OBJECT2 for some gameobject types...
            if (isType(TypeMask.GAME_OBJECT)) {
                switch (toGameObject().getGoType()) {
                    case TRAP:
                    case DUEL_ARBITER:
                    case FLAG_STAND:
                    case FLAG_DROP:
                        updateType = ObjectUpdateType.CREATE_OBJECT2;
                        break;
                    default:
                        break;
                }
            }
        }

        if (this instanceof Unit unit && unit.getVictim() != null) {
            flags.addFlag(ObjectUpdateFlag.HAS_TARGET);
        }


        WorldPacket buff = WorldPacket.wrap(data.getBuffer());

        buff.writeInt8(updateType.ordinal());
        buff.writeGuid(getGUID());
        buff.writeInt8(objectTypeId.ordinal());

        buildMovementUpdate(buff, flags, target);
        buildValuesUpdate(updateType, buff, target);
        buildDynamicValuesUpdate(updateType, buff, target);
        data.addUpdateBlock();

    }


    public final void sendUpdateToPlayer(Player player) {
        // send create update to player
        UpdateData upd = new UpdateData(player.getLocation().getMapId());

        if (player.haveAtClient(this)) {
            buildValuesUpdateBlockForPlayer(upd, player);
        } else {
            buildCreateUpdateBlockForPlayer(upd, player);
        }

        WorldPacket packet = upd.buildPacket();

        player.sendPacket(packet);
    }

    public final void buildValuesUpdateBlockForPlayer(UpdateData data, Player target) {

        WorldPacket buffer = prepareValuesUpdateBuffer(data);
        buffer.writeInt8(ObjectUpdateType.VALUES.ordinal());
        buffer.writeGuid(getGUID());

        buildValuesUpdate(ObjectUpdateType.VALUES, buffer, target);
        buildDynamicValuesUpdate(ObjectUpdateType.VALUES, buffer, target);

        data.addUpdateBlock();
    }


    void buildValuesUpdate(ObjectUpdateType updateType, WorldPacket data, Player target) {
        if (target == null)
            return;

        int blockCount = UpdateMask.getBlockCount(valuesCount);

        int[] flags = getUpdateFieldFlagsFor(target);
        int visibleFlag = getUpdateVisibleFlagFor(target);
        // fill update mask block
        data.writeInt8(blockCount);
        var maskPos = data.writerIndex();
        data.writeBytes(new byte[blockCount * Integer.BYTES]);

        for (int index = 0; index < valuesCount; index++) {
            if ((fieldNotifyFlags & flags[index]) != 0
                    || (updateType == ObjectUpdateType.VALUES ? changesMask.get(index) : int32Values[index] != 0
                    && (flags[index] & visibleFlag) != 0)) {
                UpdateMask.setUpdateBit(data, maskPos, index);
                data.writeInt32(int32Values[index]);
            }
        }

    }


    private int[] getUpdateFieldFlagsFor(Player target) {
        return switch (objectTypeId) {
            case ITEM, CONTAINER -> ITEM_UPDATE_FIELD_FLAGS;
            case UNIT, PLAYER -> UNIT_UPDATE_FIELD_FLAGS;
            case GAME_OBJECT -> GAME_OBJECT_UPDATE_FIELD_FLAGS;
            case DYNAMIC_OBJECT -> DYNAMIC_OBJECT_UPDATE_FIELD_FLAGS;
            case CORPSE -> CORPSE_UPDATE_FIELD_FLAGS;
            case AREA_TRIGGER -> AREA_TRIGGER_UPDATE_FIELD_FLAGS;
            case SCENE_OBJECT -> SCENE_OBJECT_UPDATE_FIELD_FLAGS;
            case CONVERSATION -> CONVERSATION_UPDATE_FIELD_FLAGS;
            default -> throw new IllegalStateException("Unexpected value: " + objectTypeId);
        };
    }


    private int getUpdateVisibleFlagFor(Player target) {
        int visibleFlag = UF_FLAG_PUBLIC;

        if (target == this) visibleFlag |= UF_FLAG_PRIVATE;

        switch (objectTypeId) {
            case ITEM, CONTAINER -> {
                if (this instanceof Item item && Objects.equals(item.getOwnerGUID(), target.getGUID())) {
                    visibleFlag |= UF_FLAG_OWNER | UF_FLAG_ITEM_OWNER;
                }
            }
            case UNIT, PLAYER -> {
                Player plr = toUnit().getCharmerOrOwnerPlayerOrPlayerItself();
                if (Objects.equals(toUnit().getOwnerGUID(), target.getGUID())) {

                    visibleFlag |= UF_FLAG_OWNER;
                }

                if (hasFlag(OBJECT_DYNAMIC_FLAGS, UnitDynFlag.SPECIAL_INFO) && toUnit().hasAuraTypeWithCaster(AuraType.EMPATHY, target.getGUID()))
                    visibleFlag |= UF_FLAG_SPECIAL_INFO;

                if (plr != null && plr.isInSameRaidWith(target)) visibleFlag |= UF_FLAG_PARTY_MEMBER;
            }
            case GAME_OBJECT -> {
                if (Objects.equals(toGameObject().getOwnerGUID(), target.getGUID())) visibleFlag |= UF_FLAG_OWNER;
            }
            case DYNAMIC_OBJECT -> {
                if (Objects.equals(toDynObject().getOwnerGUID(), target.getGUID())) visibleFlag |= UF_FLAG_OWNER;
            }
            case CORPSE -> {
                if (Objects.equals(toCorpse().getOwnerGUID(), target.getGUID())) visibleFlag |= UF_FLAG_OWNER;
            }
            case OBJECT -> Assert.fail();
        }
        return visibleFlag;
    }


    int[] getDynamicUpdateFieldFlagsFor(Player target) {
        return switch (objectTypeId) {
            case ITEM, CONTAINER -> ITEM_DYNAMIC_UPDATE_FIELD_FLAGS;
            case UNIT, PLAYER -> UNIT_DYNAMIC_UPDATE_FIELD_FLAGS;
            case GAME_OBJECT -> GAME_OBJECT_DYNAMIC_UPDATE_FIELD_FLAGS;
            case CONVERSATION -> CONVERSATION_DYNAMIC_UPDATE_FIELD_FLAGS;
            default -> null;
        };
    }


    int getDynamicUpdateVisibleFlagFor(Player target) {

        int visibleFlag = UF_FLAG_PUBLIC;

        if (target == this) visibleFlag |= UF_FLAG_PRIVATE;

        switch (objectTypeId) {
            case ITEM:
            case CONTAINER:
                if (this instanceof Item item && Objects.equals(item.getOwnerGUID(), target.getGUID())) {
                    visibleFlag |= UF_FLAG_OWNER | UF_FLAG_ITEM_OWNER;
                    break;
                }
            case UNIT:
            case PLAYER: {
                Player plr = toUnit().getCharmerOrOwnerPlayerOrPlayerItself();
                if (Objects.equals(toUnit().getOwnerGUID(), target.getGUID())) {
                    visibleFlag |= UF_FLAG_OWNER;

                    if (hasFlag(OBJECT_DYNAMIC_FLAGS, UnitDynFlag.SPECIAL_INFO) && toUnit().hasAuraTypeWithCaster(AuraType.EMPATHY, target.getGUID()))
                        visibleFlag |= UF_FLAG_SPECIAL_INFO;

                    if (plr != null && plr.isInSameRaidWith(target)) visibleFlag |= UF_FLAG_PARTY_MEMBER;
                    break;
                }
            }

            case CONVERSATION:
                if (Objects.equals(toConversation().getOwnerGUID(), target.getGUID())) {
                    visibleFlag |= UF_FLAG_0x100;
                    break;
                }
        }
        return visibleFlag;
    }


    void buildDynamicValuesUpdate(ObjectUpdateType updateType, WorldPacket buffer, Player target) {
        if (target == null)
            return;

        int blockCount = UpdateMask.getBlockCount(dynamicValuesCount);
        int[] flags = getDynamicUpdateFieldFlagsFor(target);
        int visibleFlag = getDynamicUpdateVisibleFlagFor(target);

        // fill update mask block
        buffer.writeInt8(blockCount);
        var maskPos = buffer.writerIndex();
        buffer.writeBytes(new byte[blockCount * Integer.BYTES]);

        for (int index = 0; index < dynamicValuesCount; ++index) {
            IntArray dynamicValue = dynamicValues[index];

            if ((fieldNotifyFlags & flags[index]) != 0
                    || ((updateType == ObjectUpdateType.VALUES ? dynamicChangesMask[index] != DynFieldChangeType.UNCHANGED : !dynamicValue.isEmpty())
                    && (flags[index] & visibleFlag) != 0)) {

                UpdateMask.setUpdateBit(buffer, maskPos, index);

                int arrayBlockCount = UpdateMask.getBlockCount(dynamicValuesCount);
                buffer.writeInt16(UpdateMask.encodeDynamicFieldChangeType(arrayBlockCount, dynamicChangesMask[index], updateType));
                if (dynamicChangesMask[index] == DynFieldChangeType.VALUE_AND_SIZE_CHANGED && updateType == ObjectUpdateType.VALUES) {
                    buffer.writeInt32(dynamicValue.size);
                }
                var arrayMaskPos = buffer.writerIndex();
                buffer.writeBytes(new byte[arrayBlockCount * Integer.BYTES]);

                for (int v = 0; v < dynamicValue.size; ++v) {
                    if (updateType != ObjectUpdateType.VALUES || dynamicChangesArrayMask[index].get(v)) {
                        UpdateMask.setUpdateBit(buffer, arrayMaskPos, v);
                        buffer.writeInt32(dynamicValue.get(v));
                    }
                }
            }
        }
    }


    public final void buildDestroyUpdateBlock(UpdateData data) {
        data.addDestroyObject(getGUID());
    }

    public final void buildOutOfRangeUpdateBlock(UpdateData data) {
        data.addOutOfRangeGUID(getGUID());
    }


    public final WorldPacket prepareValuesUpdateBuffer(UpdateData data) {
        WorldPacket buffer = WorldPacket.wrap(data.getBuffer());
        buffer.writeInt8(ObjectUpdateType.VALUES.ordinal());
        buffer.writeGuid(getGUID());
        return buffer;
    }

    public void destroyForPlayer(Player target) {
        UpdateData updateData = new UpdateData(target.getLocation().getMapId());
        buildDestroyUpdateBlock(updateData);
        target.sendPacket(updateData.buildPacket());
    }

    public final void sendOutOfRangeForPlayer(Player target) {
        UpdateData updateData = new UpdateData(target.getLocation().getMapId());
        buildOutOfRangeUpdateBlock(updateData);
        target.sendPacket(updateData.buildPacket());
    }

    private void buildMovementUpdate(WorldPacket data, EnumFlag<ObjectUpdateFlag> flags, Player target) {
        boolean noBirthAnim = false;
        boolean enablePortals = false;
        boolean playHoverAnim = false;
        boolean hasMovementUpdate = flags.hasFlag(ObjectUpdateFlag.LIVING);
        boolean hasMovementTransport = flags.hasFlag(ObjectUpdateFlag.TRANSPORT_POSITION);
        boolean stationary = flags.hasFlag(ObjectUpdateFlag.STATIONARY_POSITION);
        boolean combatVictim = flags.hasFlag(ObjectUpdateFlag.HAS_TARGET);
        boolean serverTime = flags.hasFlag(ObjectUpdateFlag.TRANSPORT);
        boolean vehicleCreate = flags.hasFlag(ObjectUpdateFlag.VEHICLE);
        boolean animKitCreate = flags.hasFlag(ObjectUpdateFlag.ANIM_KITS);
        boolean rotation = flags.hasFlag(ObjectUpdateFlag.ROTATION);
        boolean hasAreaTrigger = flags.hasFlag(ObjectUpdateFlag.AREA_TRIGGER);
        boolean hasGameObject = flags.hasFlag(ObjectUpdateFlag.GAME_OBJECT);
        boolean thisIsYou = flags.hasFlag(ObjectUpdateFlag.SELF);
        boolean smoothPhasing = false;
        boolean sceneObjCreate = false;
        boolean playerCreateData = isPlayer() && toUnit().getPowerIndex(Power.RUNES) != Power.MAX_POWERS.index;

        IntArray pauseTimes = null;
        if (this instanceof GameObject go) {
            pauseTimes = go.getPauseTimes();
        }

        data.writeBit(noBirthAnim);
        data.writeBit(enablePortals);
        data.writeBit(playHoverAnim);
        data.writeBit(hasMovementUpdate);
        data.writeBit(hasMovementTransport);
        data.writeBit(stationary);
        data.writeBit(combatVictim);
        data.writeBit(serverTime);
        data.writeBit(vehicleCreate);
        data.writeBit(animKitCreate);
        data.writeBit(rotation);
        data.writeBit(hasAreaTrigger);
        data.writeBit(hasGameObject);
        data.writeBit(smoothPhasing);
        data.writeBit(thisIsYou);
        data.writeBit(sceneObjCreate);
        data.writeBit(playerCreateData);
        data.flushBits();

        if (hasMovementUpdate) {
            Unit unit = toUnit();
            boolean hasFallDirection = unit.hasUnitMovementFlag(MovementFlag.FALLING);
            boolean hasFall = hasFallDirection || unit.getMovementInfo().getJump().getFallTime() != 0;
            boolean hasSpline = unit.isSplineEnabled();


            data.writeGuid(getGUID()); // MoverGUID

            data.writeInt32(unit.movementInfo.getTime()); // MoveTime
            data.writeFloat(unit.getLocation().getX());
            data.writeFloat(unit.getLocation().getY());
            data.writeFloat(unit.getLocation().getZ());
            data.writeFloat(unit.getLocation().getO());

            data.writeFloat(unit.getMovementInfo().getPitch()); // Pitch
            data.writeFloat(unit.getMovementInfo().getStepUpStartElevation()); // StepUpStartElevation

            data.writeInt32(0); // RemoveForcesIDs.size()
            data.writeInt32(0); // MoveIndex

            //for (public uint i = 0; i < RemoveForcesIDs.count; ++i)
            //    *data << objectGuid(RemoveForcesIDs);


            data.writeBits(unit.getUnitMovementFlags(), 30);
            data.writeBits(unit.getExtraUnitMovementFlags(), 18);

            data.writeBit(!unit.getMovementInfo().getTransport().getGuid().isEmpty()); // HasTransport
            data.writeBit(hasFall); // hasFall
            data.writeBit(hasSpline); // HasSpline - marks that the unit uses spline movement
            data.writeBit(false); // HeightChangeFailed
            data.writeBit(false); // RemoteTimeValid

            if (!unit.getMovementInfo().getTransport().getGuid().isEmpty()) {
                MovementIOUtil.writeTransportInfo(data, unit.getMovementInfo().getTransport());
            }


            if (hasFall) {
                data.writeInt32(unit.getMovementInfo().getJump().getFallTime()); // Time
                data.writeFloat(unit.getMovementInfo().getJump().zSpeed); // JumpVelocity

                if (data.writeBit(hasFallDirection)) {
                    data.writeFloat(unit.getMovementInfo().getJump().getSinAngle()); // Direction
                    data.writeFloat(unit.getMovementInfo().getJump().getCosAngle());
                    data.writeFloat(unit.getMovementInfo().getJump().xySpeed); // Speed
                }
            }

            data.writeFloat(unit.getSpeed(UnitMoveType.WALK));
            data.writeFloat(unit.getSpeed(UnitMoveType.RUN));
            data.writeFloat(unit.getSpeed(UnitMoveType.RUN_BACK));
            data.writeFloat(unit.getSpeed(UnitMoveType.SWIM));
            data.writeFloat(unit.getSpeed(UnitMoveType.SWIM_BACK));
            data.writeFloat(unit.getSpeed(UnitMoveType.FLIGHT));
            data.writeFloat(unit.getSpeed(UnitMoveType.FLIGHT_BACK));
            data.writeFloat(unit.getSpeed(UnitMoveType.TURN_RATE));
            data.writeFloat(unit.getSpeed(UnitMoveType.PITCH_RATE));

            var movementForces = unit.getMovementForces();

            if (movementForces != null) {
                data.writeInt32(movementForces.forces.size());
                data.writeFloat(movementForces.modMagnitude); // MovementForcesModMagnitude
            } else {
                data.writeInt32(0);
                data.writeFloat(1.0f); // MovementForcesModMagnitude
            }

            data.writeBit(hasSpline);
            data.flushBits();

            if (movementForces != null) {
                for (var force : movementForces.forces) {
                    MovementIOUtil.writeMovementForceWithDirection(force, data, unit.getLocation());
                }
            }

            // HasMovementSpline - marks that spline data is present in packet
            if (hasSpline) {
                MovementIOUtil.writeCreateObjectSplineDataBlock(unit.getMoveSpline(), data);
            }
        }

        data.writeInt32(pauseTimes != null ? pauseTimes.size : 0);

        if (stationary) {
            var self = (WorldObject) this;
            data.writeFloat(self.getStationaryX());
            data.writeFloat(self.getStationaryY());
            data.writeFloat(self.getStationaryZ());
            data.writeFloat(self.getStationaryO());
        }

        if (combatVictim) {
            data.writeGuid(toUnit().getVictim().getGUID()); // CombatVictim
        }

        if (serverTime) {
            data.writeInt32(GameTime.getGameTimeMS());
        }

        if (vehicleCreate) {
            var unit = toUnit();
            data.writeInt32(unit.getVehicleKit().getVehicleInfo().getId()); // RecID
            data.writeFloat(unit.getLocation().getO()); // InitialRawFacing
        }

        if (animKitCreate) {
            var worldObject = (WorldObject) this;
            data.writeInt16(worldObject.getAIAnimKitId()); // AiID
            data.writeInt16(worldObject.getMovementAnimKitId()); // MovementID
            data.writeInt16(worldObject.getMeleeAnimKitId()); // MeleeID
        }

        if (rotation) {
            data.writeInt64(toGameObject().getPackedLocalRotation()); // Rotation
        }

        if (pauseTimes != null && !pauseTimes.isEmpty()) {
            for (var stopFrame : pauseTimes.items) {
                data.writeInt32(stopFrame);
            }
        }

        if (hasMovementTransport) {
            var self = (WorldObject) this;
            MovementIOUtil.writeTransportInfo(data, self.getMovementInfo().getTransport());
        }

        if (hasAreaTrigger) {
            var areaTrigger = toAreaTrigger();
            var createProperties = areaTrigger.getCreateProperties();
            var areaTriggerTemplate = areaTrigger.getTemplate();
            var shape = areaTrigger.getShape();

            data.writeInt32(areaTrigger.getTimeSinceCreated());

            data.writeVector3(areaTrigger.getRollPitchYaw());

            var hasAbsoluteOrientation = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.ABSOLUTE_ORIENTATION);
            var hasDynamicShape = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.DYNAMIC_SHAPE);
            var hasAttached = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.ATTACHED);
            var hasFaceMovementDir = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.FACE_MOVEMENT_DIR);
            var hasFollowsTerrain = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.FOLLOWS_TERRAIN);
            var hasUnk1 = areaTriggerTemplate != null && areaTriggerTemplate.hasFlag(AreaTriggerCreatePropertiesFlag.UNK1);
            var hasTargetRollPitchYaw = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.TARGET_ROLL_PITCH_YAW);
            var hasScaleCurveID = createProperties != null && createProperties.ScaleCurveId != 0;
            var hasMorphCurveID = createProperties != null && createProperties.MorphCurveId != 0;
            var hasFacingCurveID = createProperties != null && createProperties.FacingCurveId != 0;
            var hasMoveCurveID = createProperties != null && createProperties.MoveCurveId != 0;
            var hasAnimation = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.ANIM_ID);
            var visualAnimIsDecay = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.VISUAL_ANIM_IS_DECAY);
            var hasAnimKitID = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.ANIM_KIT_ID);

            var hasAreaTriggerSphere = shape.isSphere();
            var hasAreaTriggerBox = shape.isBox();
            var hasAreaTriggerPolygon = createProperties != null && shape.isPolygon();
            var hasAreaTriggerCylinder = shape.isCylinder();
            var hasAreaTriggerSpline = areaTrigger.getHasSplines();
            var hasOrbit = areaTrigger.hasOrbit();

            data.writeBit(hasAbsoluteOrientation);
            data.writeBit(hasDynamicShape);
            data.writeBit(hasAttached);
            data.writeBit(hasFaceMovementDir);
            data.writeBit(hasFollowsTerrain);
            data.writeBit(hasUnk1);
            data.writeBit(hasTargetRollPitchYaw);
            data.writeBit(hasScaleCurveID);
            data.writeBit(hasMorphCurveID);
            data.writeBit(hasFacingCurveID);
            data.writeBit(hasMoveCurveID);
            data.writeBit(hasAnimation);
            data.writeBit(visualAnimIsDecay);
            data.writeBit(hasAnimKitID);
            data.writeBit(hasAreaTriggerSphere);
            data.writeBit(hasAreaTriggerBox);
            data.writeBit(hasAreaTriggerPolygon);
            data.writeBit(hasAreaTriggerCylinder);
            data.writeBit(hasAreaTriggerSpline);
            data.writeBit(hasOrbit);


            if (visualAnimIsDecay)
                data.writeBit(false);//data->WriteBit(0);

            data.flushBits();

            if (hasAreaTriggerSpline) {
                data.writeInt32(areaTrigger.getTimeToTarget());
                data.writeInt32(areaTrigger.getElapsedTimeForMovement());
                MovementIOUtil.writeCreateObjectAreaTriggerSpline(areaTrigger.getSpline(), data);
            }

            if (hasTargetRollPitchYaw) {
                data.writeVector3(areaTrigger.getTargetRollPitchYaw());
            }

            if (hasScaleCurveID) {
                data.writeInt32(createProperties.ScaleCurveId);
            }

            if (hasMorphCurveID) {
                data.writeInt32(createProperties.MorphCurveId);
            }

            if (hasFacingCurveID) {
                data.writeInt32(createProperties.FacingCurveId);
            }

            if (hasMoveCurveID) {
                data.writeInt32(createProperties.MoveCurveId);
            }

            if (hasAnimation) {
                data.writeInt32(createProperties.AnimId);
            }

            if (hasAnimKitID) {
                data.writeInt32(createProperties.AnimKitId);
            }

            if (hasAreaTriggerSphere) {
                data.writeFloat(shape.SphereData.Radius);
                data.writeFloat(shape.SphereData.RadiusTarget);
            }

            if (hasAreaTriggerBox) {
                data.writeFloat(shape.BoxData.Extents[0]);
                data.writeFloat(shape.BoxData.Extents[1]);
                data.writeFloat(shape.BoxData.Extents[2]);
                data.writeFloat(shape.BoxData.ExtentsTarget[0]);
                data.writeFloat(shape.BoxData.ExtentsTarget[1]);
                data.writeFloat(shape.BoxData.ExtentsTarget[2]);
            }

            if (hasAreaTriggerPolygon) {
                data.writeInt32(shape.PolygonVertices.size());
                data.writeInt32(shape.PolygonVerticesTarget.size());
                data.writeFloat(shape.PolygonDatas.Height);
                data.writeFloat(shape.PolygonDatas.HeightTarget);

                for (var item : shape.PolygonVertices) {
                    data.writeVector2(item);
                }

                for (var item : shape.PolygonVerticesTarget) {
                    data.writeVector2(item);
                }
            }

            if (hasAreaTriggerCylinder) {
                data.writeFloat(shape.CylinderData.Radius);
                data.writeFloat(shape.CylinderData.RadiusTarget);
                data.writeFloat(shape.CylinderData.Height);
                data.writeFloat(shape.CylinderData.HeightTarget);
                data.writeFloat(shape.CylinderData.LocationZOffset);
                data.writeFloat(shape.CylinderData.LocationZOffsetTarget);
            }

            if (hasOrbit) {
                AreaTriggerOrbitInfo orbit = areaTrigger.getCircularMovementInfo();
                data.writeBit(orbit.pathTarget != null);
                data.writeBit(orbit.center != null);
                data.writeBit(orbit.counterClockwise);
                data.writeBit(orbit.canLoop);

                data.writeInt32(orbit.timeToTarget);
                data.writeInt32(orbit.elapsedTimeForMovement);
                data.writeInt32(orbit.startDelay);
                data.writeFloat(orbit.radius);
                data.writeFloat(orbit.blendFromRadius);
                data.writeFloat(orbit.initialAngle);
                data.writeFloat(orbit.ZOffset);

                if (orbit.pathTarget != null) {
                    data.writeGuid(orbit.pathTarget);
                }

                if (orbit.center != null) {
                    data.writeVector3(orbit.center);
                }
            }
        }

        if (hasGameObject) {
            GameObject gameObject = toGameObject();
            Transport transport = gameObject.toTransport();

            boolean bit8 = false;

            data.writeInt32(gameObject.getWorldEffectID());

            data.writeBit(bit8);
            data.writeBit(transport != null);
            data.writeBit(gameObject.getPathProgressForClient() != null);
            data.flushBits();
            if (transport != null) {
                int period = transport.getTransportPeriod();

                data.writeInt32((((transport.getTimer() - GameTime.getGameTimeMS()) % period) + period) % period);  // TimeOffset
                data.writeInt32(Objects.requireNonNullElse(transport.getNextStopTimestamp(), 0));
                data.writeBit(transport.getNextStopTimestamp() != null);
                data.writeBit(transport.isStopped());
                data.writeBit(false);
                data.flushBits();
            }

            if (bit8)
                data.writeInt32(0);

            if (gameObject.getPathProgressForClient() != null)
                data.writeFloat(gameObject.getPathProgressForClient());
        }

        if (smoothPhasing) {

            WorldObject self = (WorldObject) this;
            var smoothPhasingInfo = self.getSmoothPhasing().getInfoForSeer(target.getGUID());

            data.writeBit(smoothPhasingInfo.getReplaceActive());
            data.writeBit(smoothPhasingInfo.getStopAnimKits());
            data.writeBit(smoothPhasingInfo.replaceObject != null);
            data.flushBits();

            if (smoothPhasingInfo.replaceObject != null) {
                data.writeGuid(smoothPhasingInfo.replaceObject);
            }
        }

        if (sceneObjCreate) {
            data.writeBit(false); // HasLocalScriptData
            data.writeBit(false); // HasPetBattleFullUpdate
            data.flushBits();
        }

        if (playerCreateData) {
            var player = toPlayer();

            var hasSceneInstanceIDs = !player.getSceneMgr().getSceneTemplateByInstanceMap().isEmpty();
            var hasRuneState = toUnit().getPowerIndex(Power.RUNES) != Power.MAX_POWERS.index;
            var hasActionButtons = true;


            data.writeBit(hasSceneInstanceIDs);
            data.writeBit(hasRuneState);
            data.writeBit(hasActionButtons);
            data.flushBits();

            if (hasSceneInstanceIDs) {
                data.writeInt32(player.getSceneMgr().getSceneTemplateByInstanceMap().size());

                for (var pair : player.getSceneMgr().getSceneTemplateByInstanceMap().entrySet()) {
                    data.writeInt32(pair.getKey());
                }
            }

            if (hasRuneState) {

                data.writeInt8((byte) ((1 << PlayerDefine.MAX_RUNES) - 1));
                data.writeInt8(player.getRunesState());
                data.writeInt32(PlayerDefine.MAX_RUNES);
                for (var i = 0; i <PlayerDefine. MAX_RUNES; ++i)
                    data.writeInt8((byte) ((1.0f - player.getRuneCooldown(i)) * 255));

            }
            if (hasActionButtons) {
                var actionButtonList = player.getActionButtons();
                for (int i = 0; i < PlayerDefine.MAX_ACTION_BUTTONS; ++i) {
                    var button = actionButtonList.get(i);
                    if (button != null && button.uState != ActionButtonUpdateState.DELETED)
                        data.writeInt32((int) button.getPackedData());
                    else
                        data.writeInt32(0);
                }
            }
        }
    }


    protected abstract boolean addToObjectUpdate();
    protected abstract void removeFromObjectUpdate();


    private void addToObjectUpdateIfNeeded() {
        if (inWorld && !objectUpdated) {
            addToObjectUpdate();
            objectUpdated = true;
        }
    }


    protected final void setInt32Value(int index, int value) {
        checkIndex(index, true);
        int oldVal = int32Values[index];
        if (oldVal != value) {
            int32Values[index] = value;
            changesMask.set(index);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setInt32Value(int index, EnumFlag.FlagValue value) {
        setInt32Value(index, value.getValue());
    }


    protected final void updateInt32Value(int index, int value) {
        checkIndex(index, true);
        int32Values[index] = value;
        changesMask.set(index);
    }

    protected final void setInt64Value(int index, long value) {
        Assert.isTrue(value < 0, "Overflowed value {}.", value);
        checkIndex(index + 1, true);
        int i0 = int32Values[index];
        int i1 = int32Values[index + 1];
        long oldValue = ((long) i1 << 32) | (long) i0;
        if (oldValue != value) {
            int32Values[index] = (int) (value & 0x00000000ffffffffL);
            int32Values[index + 1] = (int) (value >>> 32);
            changesMask.set(index);
            changesMask.set(index + 1);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final boolean addGuidValue(int index, ObjectGuid value) {
        checkIndex(index + 3, true);
        ObjectGuid oldValue = getGuidValue(index);
        if (!value.isEmpty() && oldValue.isEmpty()) {
            setGuidValue(index, value);
            return true;
        }
        return false;
    }

    protected final boolean removeGuidValue(int index, ObjectGuid value) {
        checkIndex(index + 3, true);
        if (!value.isEmpty() && getGuidValue(index).equals(value)) {
            int32Values[index] = 0;
            int32Values[index + 1] = 0;
            int32Values[index + 2] = 0;
            int32Values[index + 3] = 0;

            changesMask.set(index);
            changesMask.set(index + 1);
            changesMask.set(index + 2);
            changesMask.set(index + 3);

            addToObjectUpdateIfNeeded();
            return true;
        }
        return false;
    }

    protected final void setFloatValue(int index, float value) {
        checkIndex(index, true);
        int intValue = Float.floatToIntBits(value);
        if (int32Values[index] != intValue) {
            int32Values[index] = intValue;
            changesMask.set(index);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setByteValue(int index, byte offset, int value) {
        checkIndex(index, true);
        Assert.isTrue(offset < 4, "getByteValue: wrong offset {}.", offset);
        int i = int32Values[index];
        if ((byte) (i >>> (offset * 8)) != value) {
            i &= ~(0xFF << (offset * 8));
            i |= (value & 0xff << (offset * 8));

            int32Values[index] = i;
            changesMask.set(index);

            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setInt16Value(int index, int offset, short value) {
        checkIndex(index, true);
        Assert.isTrue(offset < 2, "setUInt16Value: wrong offset {}.", offset);
        int i = int32Values[index];
        if ((short) (i >>> (offset * 16)) != value) {
            i &= ~(0xFFFF << (offset * 16));
            i |= value << (offset * 16);
            int32Values[index] = i;
            changesMask.set(index);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setGuidValue(int index, ObjectGuid value) {
        checkIndex(index + 3, true);

        if (!getGuidValue(index).equals(value)) {
            long l0 = value.lowValue();
            long l1 = value.highValue();
            int32Values[index] = (int) (l0 & 0x00000000ffffffffL);
            int32Values[index + 1] = (int) (l0 >>> 32);
            int32Values[index + 2] = (int) (l1 & 0x00000000ffffffffL);
            int32Values[index + 3] = (int) (l1 >>> 32);

            changesMask.set(index);
            changesMask.set(index + 1);
            changesMask.set(index + 2);
            changesMask.set(index + 3);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setStatFloatValue(int index, float value) {
        if (value < 0) value = 0.0f;

        setFloatValue(index, value);
    }

    protected final void setStatInt32Value(int index, int value) {
        if (value < 0) value = 0;
        setInt32Value(index, value);
    }


    protected final void applyModInt32Value(int index, int val, boolean apply) {
        int cur = getInt32Value(index);
        cur += (apply ? val : -val);
        setInt32Value(index, cur);
    }

    protected final void applyModUInt16Value(int index, byte offset, short val, boolean apply) {
        int cur = getInt16Value(index, offset);
        cur += apply ? val : -val;
        if (cur < 0) cur = 0;
        setInt16Value(index, offset, (short) cur);
    }

    protected final void applyModSignedFloatValue(int index, float val, boolean apply) {
        float cur = getFloatValue(index);
        cur += (apply ? val : -val);
        setFloatValue(index, cur);
    }

    private static float applyPercentModFloatVar(float var, float val, boolean apply) {
        if (val == -100.0f)     // prevent set var to zero
            val = -99.99f;
        return var * (apply ? (100.0f + val) / 100.0f : 100.0f / (100.0f + val));
    }


    protected final void applyModPositiveFloatValue(int index, float val, boolean apply) {
        float cur = getFloatValue(index);
        cur += (apply ? val : -val);
        if (cur < 0) cur = 0;
        setFloatValue(index, cur);
    }

    protected final void setFlag(int index, EnumFlag.FlagValue flag) {
        setFlag(index, flag.getValue());
    }

    protected final void setFlag(int index, int newFlag) {

        int oldVal = getInt32Value(index);
        int newVal = oldVal | newFlag;

        if (oldVal != newVal) {
            setInt32Value(index, newVal);
        }
    }

    protected final void removeFlag(int index, EnumFlag.FlagValue flag) {
        removeFlag(index, flag.getValue());
    }

    protected final void removeFlag(int index, int oldFlag) {

        int oldVal = getInt32Value(index);
        int newVal = oldVal & ~oldFlag;

        if (oldVal != newVal) {
            setInt32Value(index, newVal);
        }
    }

    protected final void toggleFlag(int index, EnumFlag.FlagValue flag) {
        toggleFlag(index, flag.getValue());
    }

    protected final void toggleFlag(int index, int flag) {
        if (hasFlag(index, flag)) removeFlag(index, flag);
        else setFlag(index, flag);
    }


    protected final boolean hasFlag(int index, EnumFlag.FlagValue flag) {
        return hasFlag(index, flag.getValue());
    }

    protected final boolean hasFlag(int index, int flag) {
        int value = getInt32Value(index);

        return (value & flag) != 0;
    }

    protected final void applyModFlag(int index, int flag, boolean apply) {
        if (apply) setFlag(index, flag);
        else removeFlag(index, flag);
    }

    protected final void setByteFlag(int index, byte offset, EnumFlag.FlagValue flag) {
        setByteFlag(index, offset, (byte) flag.getValue());
    }

    protected final void setByteFlag(int index, byte offset, byte newFlag) {
        checkIndex(index, true);
        Assert.notOutOfBound(offset, 4, "SetByteFlag: wrong offset {}.", offset);
        int i = int32Values[index];
        if (((byte) (i >>> (offset * 8)) & newFlag) == 0) {
            i |= newFlag << (offset * 8);
            int32Values[index] = i;
            changesMask.set(index);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void removeByteFlag(int index, byte offset, EnumFlag.FlagValue flag) {
        removeByteFlag(index, offset, (byte) flag.getValue());
    }

    protected final void removeByteFlag(int index, byte offset, byte oldFlag) {
        checkIndex(index, true);
        Assert.notOutOfBound(offset, 4, "RemoveByteFlag: wrong offset {}.", offset);

        int i = int32Values[index];

        if (((byte) (i >>> (offset * 8)) & offset) != 0) {
            i &= ~oldFlag << (offset * 8);
            int32Values[index] = i;
            changesMask.set(index);
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void toggleByteFlag(int index, byte offset, EnumFlag.FlagValue flag) {
        toggleByteFlag(index, offset, (byte) flag.getValue());
    }

    protected final void toggleByteFlag(int index, byte offset, byte flag) {
        if (hasByteFlag(index, offset, flag))
            removeByteFlag(index, offset, flag);
        else
            setByteFlag(index, offset, flag);
    }

    protected final boolean hasByteFlag(int index, byte offset, EnumFlag.FlagValue flag) {
        return hasByteFlag(index, offset, (byte) flag.getValue());
    }

    protected final boolean hasByteFlag(int index, byte offset, byte flag) {
        checkIndex(index, true);
        Assert.notOutOfBound(offset, 4, "HasByteFlag: wrong offset {}.", offset);
        int i = int32Values[index];
        return ((byte) (i >>> (offset * 8)) & flag) != 0;
    }

    protected final void setFlag64(int index, long newFlag) {
        long oldVal = getInt64Value(index);
        long newVal = oldVal | newFlag;
        setInt64Value(index, newVal);
    }

    protected final void removeFlag64(int index, long oldFlag) {
        long oldVal = getInt64Value(index);
        long newVal = oldVal & ~oldFlag;
        setInt64Value(index, newVal);
    }

    protected final void toggleFlag64(int index, long flag) {
        if (hasFlag64(index, flag)) removeFlag64(index, flag);
        else setFlag64(index, flag);
    }

    protected final boolean hasFlag64(int index, long flag) {
        return (getInt64Value(index) & flag) != 0;
    }

    protected final void applyModFlag64(int index, long flag, boolean apply) {
        if (apply) {
            setFlag64(index, flag);
        } else {
            removeFlag64(index, flag);
        }
    }

    protected void clearUpdateMask(boolean remove) {
        changesMask.clear();
        Arrays.fill(dynamicChangesMask, DynFieldChangeType.UNCHANGED);
        for (var bitSet : dynamicChangesArrayMask) {
            bitSet.clear();
        }
        if (objectUpdated) {
            if (remove)
                removeFromObjectUpdate();
            objectUpdated = false;
        }
    }

    protected final int[] getDynamicValues(int index) {
        checkDynamicIndex(index, false);
        return dynamicValues[index].items;
    }

    protected final int getDynamicValue(int index, short offset) {
        checkDynamicIndex(index, false);
        int[] dynamicValue = dynamicValues[index].items;
        Assert.notOutOfBound(offset, dynamicValue.length, "GetDynamicValue: wrong offset {}.", offset);
        return dynamicValue[offset];
    }

    protected final boolean hasDynamicValue(int index, int value) {
        checkDynamicIndex(index, false);
        int[] dynamicValue = dynamicValues[index].items;
        for (int i : dynamicValue) {
            if (i == value) return true;
        }
        return false;
    }

    protected final void addDynamicValue(int index, int value) {
        checkDynamicIndex(index, false);
        setDynamicValue(index, dynamicValues[index].size, value);
    }

    protected final void removeDynamicValue(int index, int value) {
        checkDynamicIndex(index, false);
        int[] values = dynamicValues[index].items;
        for (int i : values) {
            if (i == value) {
                values[i] = 0;
                dynamicChangesMask[index] = DynFieldChangeType.VALUE_CHANGED;
                dynamicChangesArrayMask[index].set(i);
                addToObjectUpdateIfNeeded();
            }
        }
    }

    protected final void clearDynamicValue(int index) {
        checkDynamicIndex(index, false);

        if (!dynamicValues[index].isEmpty()) {
            dynamicValues[index].clear();
            dynamicChangesMask[index] = DynFieldChangeType.VALUE_AND_SIZE_CHANGED;
            dynamicChangesArrayMask[index].clear();
            addToObjectUpdateIfNeeded();
        }
    }

    protected final void setDynamicValue(int index, int offset, int value) {
        checkDynamicIndex(index, true);

        var changeType = DynFieldChangeType.VALUE_CHANGED;
        IntArray values = dynamicValues[index];
        if (values.size <= offset) {
            values.setSize(offset + 1);
            changeType = DynFieldChangeType.VALUE_AND_SIZE_CHANGED;
        }

        if (values.items[offset] != value || changeType == DynFieldChangeType.VALUE_AND_SIZE_CHANGED) {
            values.items[offset] = value;
            dynamicChangesMask[index] = changeType;
            dynamicChangesArrayMask[index].set(offset);
            addToObjectUpdateIfNeeded();
        }
    }


    public final int getInt32Value(int index) {
        checkIndex(index, false);
        return int32Values[index];
    }


    protected final long getInt64Value(int index) {
        checkIndex(index + 1, false);
        int i0 = int32Values[index];
        int i1 = int32Values[index + 1];

        return (((long) i1) << 32) | ((long) i0 & 0xffffffffL);
    }


    protected final float getFloatValue(int index) {
        checkIndex(index, false);
        int intValue = int32Values[index];
        return Float.intBitsToFloat(intValue);
    }

    protected final byte getByteValue(int index, int offset) {
        checkIndex(index, false);
        Assert.notOutOfBound(offset, 4, "getByteValue: wrong offset {}.", offset);

        int i = int32Values[index];

        return (byte) (i >>> offset * 8);
    }

    protected final short getInt16Value(int index, int offset) {
        checkIndex(index, false);
        Assert.notOutOfBound(offset, 2, "getInt16Value: wrong offset {}.", offset);
        int i = int32Values[index];
        return (short) (i >>> offset);
    }


    protected final ObjectGuid getGuidValue(int index) {
        checkIndex(index, false);
        int i0 = int32Values[index];
        int i1 = int32Values[index + 1];
        int i2 = int32Values[index + 2];
        int i3 = int32Values[index + 3];
        return new ObjectGuid(((long) i3 << 32) | i2, ((long) i1 << 32) | i0);
    }

    private <T extends GenericObject> T cast(Class<T> kclass) {
        return kclass.isInstance(this) ? kclass.cast(this) : null;

    }

    public final WorldObject toWorldObject() {
        return cast(WorldObject.class);
    }

    public final Player toPlayer() {
        return cast(Player.class);
    }

    public final GameObject toGameObject() {
        return cast(GameObject.class);
    }

    public final Unit toUnit() {
        return cast(Unit.class);
    }

    public final Conversation toConversation() {
        return cast(Conversation.class);
    }

    public final Creature toCreature() {
        return cast(Creature.class);
    }

    public final Corpse toCorpse() {
        return cast(Corpse.class);
    }

    public final AreaTrigger toAreaTrigger() {
        return cast(AreaTrigger.class);
    }

    public final DynamicObject toDynObject() {
        return cast(DynamicObject.class);
    }

    public final SceneObject toSceneObject() {
        return cast(SceneObject.class);
    }

    public final Item toItem() {
        return cast(Item.class);
    }


    public final boolean isWorldObject() {
        return isType(TypeMask.WORLD_OBJECT);
    }

    public final boolean isPlayer() {
        return objectTypeId == TypeId.PLAYER;
    }

    public final boolean isCreature() {
        return objectTypeId == TypeId.UNIT;
    }

    public final boolean isUnit() {
        return isType(TypeMask.UNIT);
    }

    public final boolean isGameObject() {
        return objectTypeId == TypeId.GAME_OBJECT;
    }

    public final boolean isCorpse() {
        return objectTypeId == TypeId.CORPSE;
    }

    public final boolean isDynObject() {
        return objectTypeId == TypeId.DYNAMIC_OBJECT;
    }

    public final boolean isAreaTrigger() {
        return objectTypeId == TypeId.AREA_TRIGGER;
    }

    public final boolean isSceneObject() {
        return objectTypeId == TypeId.SCENE_OBJECT;
    }

    public final boolean isConversation() {
        return objectTypeId == TypeId.CONVERSATION;
    }

    public final boolean isItem() {
        return objectTypeId == TypeId.ITEM;
    }



    private void checkIndex(int index, boolean set) {
        Assert.notOutOfBound(index, valuesCount, "Attempt to {} non-existing value field: {} (count: {}) for object typeId: {} type mask: {}", (set ? "set value to" : "get value from"), index, valuesCount, objectTypeId, objectType);
    }

    private void checkDynamicIndex(int index, boolean set) {
        Assert.notOutOfBound(index, dynamicValuesCount, "Attempt to {} non-existing dynamic value field: {} (count: {}) for object typeId: {} type mask: {}", (set ? "set dynamic value to" : "get dynamic value from"), index, dynamicValuesCount, objectTypeId, objectType);
    }
}
