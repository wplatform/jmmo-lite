package com.github.azeroth.game.entity.object;


import com.github.azeroth.time.GameTime;
import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.defines.Power;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerCreatePropertiesFlag;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerOrbitInfo;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerShapeType;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.object.enums.TypeMask;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.UnitMoveType;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.conversation.Conversation;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
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
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.movement.MovementIOUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Objects;


@Getter
@Setter
public abstract class GenericObject {

    private static final byte CGObjectActiveMask   = 0x1;
    private static final byte CGObjectChangedMask  = 0x2;
    private static final byte CGObjectUpdateMask   = CGObjectActiveMask | CGObjectChangedMask;


    private static final byte ENTITY_FRAGMENT_SERIALIZATION_TYPE_FULL   = 0;
    private static final byte ENTITY_FRAGMENT_SERIALIZATION_TYPE_PARTIAL  = 1;



    protected final EntityMarkUpdater entityMarkUpdater = new EntityMarkUpdater(this);
    //UF::UpdateField<UF::ObjectData, int32(WowCS::EntityFragment::CGObject), TYPEID.OBJECT> m_objectData;

    protected final ObjectData objectData = new ObjectData();

    protected boolean objectUpdated;
    protected final EnumFlag<TypeMask> objectType = EnumFlag.of(TypeMask.class, 0);
    protected TypeId objectTypeId;
    protected final CreateObjectBits updateFlag = new CreateObjectBits();
    protected MovementInfo movementInfo;


    private ObjectGuid guid;
    private boolean inWorld;
    private boolean newObject;
    private boolean destroyedObject;





    private static float applyPercentModFloatVar(float var, float val, boolean apply) {
        if (val == -100.0f)     // prevent set var to zero
            val = -99.99f;
        return var * (apply ? (100.0f + val) / 100.0f : 100.0f / (100.0f + val));
    }


    public final ObjectGuid getGUID() {
        return this.guid;
    }

    public final int getEntry() {
        return objectData.getEntry();
    }

    public final void setEntry(int entry) {
        objectData.setEntry(entry);
    }

    public final float getObjectScale() {
        return objectData.getScale();
    }

    public final void setObjectScale(float scale) {
        objectData.setScale(scale);
    }

    protected final int getDynamicFlags()  { return objectData.getDynamicFlags(); }
    protected final boolean hasDynamicFlag(EnumFlag.FlagValue flag) { return (getDynamicFlags() & flag.getValue()) != 0; }
    protected final void setDynamicFlag(EnumFlag.FlagValue flag) { objectData.setDynamicFlags(getDynamicFlags() | flag.getValue()); }
    protected final void removeDynamicFlag(EnumFlag.FlagValue flag) { objectData.setDynamicFlags(getDynamicFlags() & ~flag.getValue()); }
    protected final void replaceAllDynamicFlags(EnumFlag.FlagValue flag) { objectData.setDynamicFlags(flag.getValue()); }


    protected final boolean isType(TypeMask mask) {
        return objectType.hasFlag(mask);
    }


    protected void addToWorld() {
        if (inWorld)
            return;

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

    public void buildCreateUpdateBlockForPlayer(UpdateData data, Player target) {
        if (target == null) return;

        ObjectUpdateType updateType = newObject ? ObjectUpdateType.CREATE_OBJECT2 : ObjectUpdateType.CREATE_OBJECT;
        TypeId objectType = objectTypeId;
        CreateObjectBits flags = new CreateObjectBits(updateFlag);

        if (target == this)                                      // building packet for yourself
        {
            updateFlag.thisIsYou = true;
            updateFlag.activePlayer = true;
            objectType = TypeId.ACTIVE_PLAYER;
        }


        if (isWorldObject()) {
            WorldObject worldObject = toWorldObject();
            if (!flags.movementUpdate && !worldObject.movementInfo.transport.guid.isEmpty())
                flags.movementTransport = true;

            if (worldObject.getAIAnimKitId() != 0f || worldObject.getMovementAnimKitId() != 0f || worldObject.getMeleeAnimKitId() != 0f)
                flags.animKit = true;

            if (worldObject.getSmoothPhasing() != null && worldObject.getSmoothPhasing().getInfoForSeer(target.getGUID()) != null)
                flags.smoothPhasing = true;
        }

        Unit unit = toUnit();
        if (unit != null) {
            flags.playHoverAnim = unit.isPlayingHoverAnim();
            if (unit.getVictim() != null)
                flags.combatVictim = true;
        }


        WorldPacket buff = WorldPacket.wrap(data.getBuffer());

        buff.writeInt8(updateType.ordinal());
        buff.writeGuid(getGUID());
        buff.writeInt8(objectType.ordinal());

        buildMovementUpdate(buff, flags, target);

        EnumFlag<UpdateFieldFlag> fieldFlags = getUpdateFieldFlagsFor(target);
        int sizePos = buff.writerIndex();
        buff.writeInt32(0);
        buff.writeInt8(fieldFlags.getFlag());
        buildEntityFragments(buff, entityMarkUpdater.getIds());
        buff.writeInt8(1);  // IndirectFragmentActive: CGObject
        buildValuesCreate(buff, fieldFlags, target);
        buff.setInt32(sizePos, buff.writerIndex() - sizePos - 4);

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

        EnumFlag<UpdateFieldFlag> fieldFlags = getUpdateFieldFlagsFor(target);

        int sizePos = buffer.writerIndex();
        buffer.writeInt32(0);
        buffer.writeInt8(fieldFlags.hasFlag(UpdateFieldFlag.Owner) ? 1 : 0);
        buffer.writeInt8(entityMarkUpdater.isIdsChanged() ? 1 : 0);
        if (entityMarkUpdater.isIdsChanged()) {
            buffer.writeInt8(ENTITY_FRAGMENT_SERIALIZATION_TYPE_FULL);
            buildEntityFragments(buffer, entityMarkUpdater.getIds());
        }
        buffer.writeInt8(entityMarkUpdater.getContentsChangedMask());

        buildValuesUpdate(buffer, fieldFlags, target);
        buffer.setInt32(sizePos, buffer.writerIndex() - sizePos - 4);

        data.addUpdateBlock();
    }


    void buildValuesUpdateBlockForPlayerWithFlag(UpdateData data, EnumFlag<UpdateFieldFlag> flags, Player target) {
        WorldPacket buf = prepareValuesUpdateBuffer(data);

        int sizePos = buf.writerIndex();
        buf.writeInt32(0);
        buildEntityFragmentsForValuesUpdateForPlayerWithMask(buf, flags);
        buildValuesUpdateWithFlag(buf, flags, target);
        buf.setInt32(sizePos, buf.writerIndex() - sizePos - 4);
        data.addUpdateBlock();
    }

    void buildEntityFragments(WorldPacket buffer, EntityFragment[] fragments) {
        for (EntityFragment fragment : fragments) {
            buffer.writeInt8(fragment.value);
        }
        buffer.writeInt8(EntityFragment.End.value);
    }

    void buildEntityFragmentsForValuesUpdateForPlayerWithMask(WorldPacket buffer, EnumFlag<UpdateFieldFlag> flags) {
        int contentsChangedMask = CGObjectChangedMask;
        for (var fragment : entityMarkUpdater.getUpdatableIds())
            if (fragment.isIndirectFragment())
                contentsChangedMask |= entityMarkUpdater.getUpdateMaskFor(fragment) >> 1;   // set the "fragment exists" bit

        buffer.writeInt8(flags.hasFlag(UpdateFieldFlag.Owner) ? 1 : 0);
        buffer.writeInt8(0); // entityFragments.IdsChanged
        buffer.writeInt8(contentsChangedMask);
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

    private void buildMovementUpdate(WorldPacket data, CreateObjectBits flags, Player target) {

        IntArray pauseTimes = null;
        if (this instanceof GameObject go) {
            pauseTimes = go.getPauseTimes();
        }

        data.writeBit(isWorldObject()); // HasPositionFragment
        data.writeBit(flags.noBirthAnim);
        data.writeBit(flags.enablePortals);
        data.writeBit(flags.playHoverAnim);
        data.writeBit(flags.movementUpdate);
        data.writeBit(flags.movementTransport);
        data.writeBit(flags.stationary);
        data.writeBit(flags.combatVictim);
        data.writeBit(flags.serverTime);
        data.writeBit(flags.vehicle);
        data.writeBit(flags.animKit);
        data.writeBit(flags.rotation);
        data.writeBit(flags.areaTrigger);
        data.writeBit(flags.gameObject);
        data.writeBit(flags.smoothPhasing);
        data.writeBit(flags.thisIsYou);
        data.writeBit(flags.sceneObject);
        data.writeBit(flags.activePlayer);
        data.writeBit(flags.conversation);
        data.flushBits();

        if (flags.movementUpdate) {
            Unit unit = toUnit();
            boolean HasFallDirection = unit.hasUnitMovementFlag(MovementFlag.FALLING);
            boolean HasFall = HasFallDirection || unit.getMovementInfo().getJump().getFallTime() != 0;
            boolean HasSpline = unit.isSplineEnabled();
            boolean HasInertia = unit.getMovementInfo().inertia != null;
            boolean HasAdvFlying = unit.getMovementInfo().advFlying != null;
            boolean HasStandingOnGameObjectGUID = unit.getMovementInfo().standingOnGameObjectGUID != null;


            data.writeGuid(getGUID()); // MoverGUID

            data.writeInt32(unit.getUnitMovementFlags());
            data.writeInt32(unit.getExtraUnitMovementFlags());
            data.writeInt32(unit.getExtraUnitMovementFlags2());


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

            data.writeBit(HasStandingOnGameObjectGUID);                    // HasStandingOnGameObjectGUID
            data.writeBit(!unit.getMovementInfo().getTransport().getGuid().isEmpty()); // HasTransport
            data.writeBit(HasFall);                                        // HasFall
            data.writeBit(HasSpline);                                      // HasSpline - marks that the unit uses spline movement
            data.writeBit(false);                                          // HeightChangeFailed
            data.writeBit(false);                                          // RemoteTimeValid
            data.writeBit(HasInertia);                                     // HasInertia
            data.writeBit(HasAdvFlying);                                   // HasAdvFlying


            if (!unit.getMovementInfo().getTransport().getGuid().isEmpty()) {
                MovementIOUtil.writeTransportInfo(data, unit.getMovementInfo().getTransport());
            }

            if (HasStandingOnGameObjectGUID)
                data.writeGuid(unit.getMovementInfo().standingOnGameObjectGUID);

            if (HasInertia) {
                data.writeInt32(unit.getMovementInfo().inertia.id);
                data.writeFloat(unit.getMovementInfo().inertia.force.getX());
                data.writeFloat(unit.getMovementInfo().inertia.force.getY());
                data.writeFloat(unit.getMovementInfo().inertia.force.getZ());
                data.writeInt32(unit.getMovementInfo().inertia.lifetime);
            }

            if (HasAdvFlying) {
                data.writeFloat(unit.getMovementInfo().advFlying.forwardVelocity);
                data.writeFloat(unit.getMovementInfo().advFlying.upVelocity);
            }

            if (HasFall) {
                data.writeInt32(unit.getMovementInfo().jump.fallTime); // Time
                data.writeFloat(unit.getMovementInfo().jump.zSpeed); // JumpVelocity

                if (data.writeBit(HasFallDirection)) {
                    data.writeFloat(unit.getMovementInfo().jump.sinAngle); // Direction
                    data.writeFloat(unit.getMovementInfo().jump.cosAngle);
                    data.writeFloat(unit.getMovementInfo().jump.xySpeed); // Speed
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
                data.writeFloat(1.0f);                                       // MovementForcesModMagnitude
            }

            data.writeFloat(2.0f);                                           // advFlyingAirFriction
            data.writeFloat(65.0f);                                          // advFlyingMaxVel
            data.writeFloat(1.0f);                                           // advFlyingLiftCoefficient
            data.writeFloat(3.0f);                                           // advFlyingDoubleJumpVelMod
            data.writeFloat(10.0f);                                          // advFlyingGlideStartMinHeight
            data.writeFloat(100.0f);                                         // advFlyingAddImpulseMaxSpeed
            data.writeFloat(90.0f);                                          // advFlyingMinBankingRate
            data.writeFloat(140.0f);                                         // advFlyingMaxBankingRate
            data.writeFloat(180.0f);                                         // advFlyingMinPitchingRateDown
            data.writeFloat(360.0f);                                         // advFlyingMaxPitchingRateDown
            data.writeFloat(90.0f);                                          // advFlyingMinPitchingRateUp
            data.writeFloat(270.0f);                                         // advFlyingMaxPitchingRateUp
            data.writeFloat(30.0f);                                          // advFlyingMinTurnVelocityThreshold
            data.writeFloat(80.0f);                                          // advFlyingMaxTurnVelocityThreshold
            data.writeFloat(2.75f);                                          // advFlyingSurfaceFriction
            data.writeFloat(7.0f);                                           // advFlyingOverMaxDeceleration
            data.writeFloat(0.4f);                                           // advFlyingLaunchSpeedCoefficient



            data.writeBit(HasSpline);
            data.flushBits();

            if (movementForces != null) {
                for (var force : movementForces.forces) {
                    MovementIOUtil.writeMovementForceWithDirection(force, data, unit.getLocation());
                }
            }

            // HasMovementSpline - marks that spline data is present in packet
            if (HasSpline) {
                MovementIOUtil.writeCreateObjectSplineDataBlock(unit.getMoveSpline(), data);
            }
        }

        data.writeInt32(pauseTimes != null ? pauseTimes.size : 0);

        if (flags.stationary) {
            var self = toWorldObject();
            data.writeFloat(self.getStationaryX());
            data.writeFloat(self.getStationaryY());
            data.writeFloat(self.getStationaryZ());
            data.writeFloat(self.getStationaryO());
        }

        if (flags.combatVictim) {
            data.writeGuid(toUnit().getVictim().getGUID()); // CombatVictim
        }

        if (flags.serverTime) {
            data.writeInt32(GameTime.getGameTimeMS());
        }

        if (flags.vehicle) {
            var unit = toUnit();
            data.writeInt32(unit.getVehicleKit().getVehicleInfo().getId()); // RecID
            data.writeFloat(unit.getLocation().getO()); // InitialRawFacing
        }

        if (flags.animKit) {
            var worldObject = toWorldObject();
            data.writeInt16(worldObject.getAIAnimKitId()); // AiID
            data.writeInt16(worldObject.getMovementAnimKitId()); // MovementID
            data.writeInt16(worldObject.getMeleeAnimKitId()); // MeleeID
        }

        if (flags.rotation) {
            data.writeInt64(toGameObject().getPackedLocalRotation()); // Rotation
        }

        if (pauseTimes != null && !pauseTimes.isEmpty()) {
            for (var stopFrame : pauseTimes.items) {
                data.writeInt32(stopFrame);
            }
        }

        if (flags.movementTransport) {
            var self = toWorldObject();
            MovementIOUtil.writeTransportInfo(data, self.getMovementInfo().getTransport());
        }

        if (flags.areaTrigger) {
            var areaTrigger = toAreaTrigger();
            var createProperties = areaTrigger.getCreateProperties();
            var areaTriggerTemplate = areaTrigger.getTemplate();
            var shape = areaTrigger.getShape();

            data.writeInt32(areaTrigger.getTimeSinceCreated());
            data.writeVector3(areaTrigger.getRollPitchYaw());


            switch (shape.Type) {
                case AreaTriggerShapeType.Sphere:
                    data.writeInt8(0);
                    data.writeFloat(shape.SphereData.Radius);
                    data.writeFloat(shape.SphereData.RadiusTarget);
                    break;
                case AreaTriggerShapeType.Box:
                    data.writeInt8(1);
                    data.writeFloat(shape.BoxData.Extents[0]);
                    data.writeFloat(shape.BoxData.Extents[1]);
                    data.writeFloat(shape.BoxData.Extents[2]);
                    data.writeFloat(shape.BoxData.ExtentsTarget[0]);
                    data.writeFloat(shape.BoxData.ExtentsTarget[1]);
                    data.writeFloat(shape.BoxData.ExtentsTarget[2]);
                    break;
                case AreaTriggerShapeType.Polygon:
                    data.writeInt8(3);
                    data.writeInt32(shape.PolygonVertices.size());
                    data.writeInt32(shape.PolygonVerticesTarget.size());
                    data.writeFloat(shape.PolygonDatas.Height);
                    data.writeFloat(shape.PolygonDatas.HeightTarget);

                    for (var vertice : shape.PolygonVertices)
                        data.writeVector2(vertice);

                    for (var vertice : shape.PolygonVerticesTarget)
                        data.writeVector2(vertice);
                    break;
                case AreaTriggerShapeType.Cylinder:
                    data.writeInt8(4);
                    data.writeFloat(shape.CylinderData.Radius);
                    data.writeFloat(shape.CylinderData.RadiusTarget);
                    data.writeFloat(shape.CylinderData.Height);
                    data.writeFloat(shape.CylinderData.HeightTarget);
                    data.writeFloat(shape.CylinderData.LocationZOffset);
                    data.writeFloat(shape.CylinderData.LocationZOffsetTarget);
                    break;
                case AreaTriggerShapeType.Disk:
                    data.writeInt8(7);
                    data.writeFloat(shape.DiskData.InnerRadius);
                    data.writeFloat(shape.DiskData.InnerRadiusTarget);
                    data.writeFloat(shape.DiskData.OuterRadius);
                    data.writeFloat(shape.DiskData.OuterRadiusTarget);
                    data.writeFloat(shape.DiskData.Height);
                    data.writeFloat(shape.DiskData.HeightTarget);
                    data.writeFloat(shape.DiskData.LocationZOffset);
                    data.writeFloat(shape.DiskData.LocationZOffsetTarget);
                    break;
                case AreaTriggerShapeType.BoundedPlane:
                    data.writeInt8(8);
                    data.writeFloat(shape.BoundedPlaneData.Extents[0]);
                    data.writeFloat(shape.BoundedPlaneData.Extents[1]);
                    data.writeFloat(shape.BoundedPlaneData.ExtentsTarget[0]);
                    data.writeFloat(shape.BoundedPlaneData.ExtentsTarget[1]);
                    break;
                default:
                    break;
            }

            var hasAbsoluteOrientation = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.ABSOLUTE_ORIENTATION);
            var hasDynamicShape        = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.DYNAMIC_SHAPE);
            var hasAttached            = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.ATTACHED);
            var hasFaceMovementDir     = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.FACE_MOVEMENT_DIR);
            var hasFollowsTerrain      = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.FOLLOWS_TERRAIN);
            var hasUnk1                = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.UNK1);
            var hasUnknown1025         = false;
            var hasTargetRollPitchYaw  = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.TARGET_ROLL_PITCH_YAW);
            var hasScaleCurveID        = createProperties != null && createProperties.ScaleCurveId != 0;
            var hasMorphCurveID        = createProperties != null && createProperties.MorphCurveId != 0;
            var hasFacingCurveID       = createProperties != null && createProperties.FacingCurveId != 0;
            var hasMoveCurveID         = createProperties != null && createProperties.MoveCurveId != 0;
            var hasAnimation           = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.ANIM_ID);
            var visualAnimIsDecay      = createProperties != null && createProperties.Flags.hasFlag(AreaTriggerCreatePropertiesFlag.VISUAL_ANIM_IS_DECAY);
            var hasAnimKitID           = createProperties != null && createProperties.AnimKitId != 0;
            var hasAnimProgress        = false;
            var hasAreaTriggerSpline   = areaTrigger.HasSplines();
            var hasOrbit               = areaTrigger.HasOrbit();
            var hasMovementScript      = false;
            var hasPositionalSoundKitID = false;



            data.writeBit(hasAbsoluteOrientation);
            data.writeBit(hasDynamicShape);
            data.writeBit(hasAttached);
            data.writeBit(hasFaceMovementDir);
            data.writeBit(hasFollowsTerrain);
            data.writeBit(hasUnk1);
            data.writeBit(hasUnknown1025);
            data.writeBit(hasTargetRollPitchYaw);
            data.writeBit(hasScaleCurveID);
            data.writeBit(hasMorphCurveID);
            data.writeBit(hasFacingCurveID);
            data.writeBit(hasMoveCurveID);
            data.writeBit(hasPositionalSoundKitID);
            data.writeBit(hasAnimation);
            data.writeBit(hasAnimKitID);
            data.writeBit(visualAnimIsDecay);
            data.writeBit(hasAnimProgress);
            data.writeBit(hasAreaTriggerSpline);
            data.writeBit(hasOrbit);
            data.writeBit(hasMovementScript);


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

            if (hasPositionalSoundKitID)
                data.writeInt32(0);

            if (hasAnimation) {
                data.writeInt32(createProperties.AnimId);
            }

            if (hasAnimKitID) {
                data.writeInt32(createProperties.AnimKitId);
            }

            if (hasAnimProgress)
                data.writeInt32(0);

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

        if (flags.gameObject) {
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

        if (flags.smoothPhasing) {

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

        if (flags.sceneObject) {
            data.writeBit(false); // HasLocalScriptData
            data.writeBit(false); // HasPetBattleFullUpdate
            data.flushBits();
        }

        if (flags.activePlayer) {
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

        if (flags.conversation) {
            Conversation self = toConversation();
            if (data.writeBit(self.getTextureKitId() != 0))
                data.writeInt32(self.getTextureKitId());

            data.flushBits();
        }
    }


    protected abstract EnumFlag<UpdateFieldFlag> getUpdateFieldFlagsFor(Player target);
    protected abstract void buildValuesCreate(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target);
    protected abstract void buildValuesUpdate(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target);
    public abstract void buildValuesUpdateWithFlag(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target);

    protected abstract boolean addToObjectUpdate();
    protected abstract void removeFromObjectUpdate();

    public void addToObjectUpdateIfNeeded() {
        if (inWorld && !objectUpdated) {
            objectUpdated = addToObjectUpdate();
        }
    }

    protected void clearUpdateMask(boolean remove) {
        entityMarkUpdater.clearChangesMask(objectData);
        entityMarkUpdater.setIdsChanged(false);

        if (objectUpdated) {
            if (remove)
                removeFromObjectUpdate();
            objectUpdated = false;
        }
    }


    public void buildFieldsUpdate(Player player, HashMap<Player, UpdateData> dataMap) {
        UpdateData data = dataMap.computeIfAbsent(player, p -> new UpdateData(p.getLocation().getMapId()));
        buildValuesUpdateBlockForPlayer(data, player);
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


}
