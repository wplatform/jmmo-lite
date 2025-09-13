package com.github.azeroth.game.entity.areatrigger;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.areatrigger.*;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerOrbitInfo;
import com.github.azeroth.game.domain.areatrigger.AreaTriggerShapeInfo;
import com.github.azeroth.game.entity.object.GirdObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.CellMoveState;
import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.Spline;
import com.github.azeroth.game.movement.model.EvaluationMode;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.listener.AreaTriggerScript;
import com.github.azeroth.game.listener.interfaces.IAreaTriggerScript;
import com.github.azeroth.game.listener.interfaces.iareatrigger.*;
import com.github.azeroth.game.spell.AuraEffect;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.SpellInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
public class AreaTrigger extends WorldObject implements GirdObject {

    private static final ArrayList<IAreaTriggerScript> DUMMY = new ArrayList<>();
    private final AreaTriggerFieldData areaTriggerData;
    private final Spline spline;
    private final HashSet<ObjectGuid> insideUnits = new HashSet<ObjectGuid>();
    private final HashMap<class,ArrayList<IAreaTriggerScript>>scriptsByType =new HashMap<class,ArrayList<IAreaTriggerScript>>();


    private int areaTriggerId;

    private long spawnId;

    private ObjectGuid targetGuid = ObjectGuid.EMPTY;

    private AuraEffect aurEff;

    private AreaTriggerShapeInfo shape;
    private float maxSearchRadius;
    private int duration;
    private int totalDuration;

    private int timeSinceCreated;
    private float previousCheckOrientation;
    private boolean isRemoved;

    private Vector3 rollPitchYaw;
    private Vector3 targetRollPitchYaw;
    private List<Vector2> polygonVertices;

    private boolean reachedDestination;
    private int lastSplineIndex;

    private int movementTime;

    private AreaTriggerOrbitInfo orbitInfo;

    private AreaTriggerCreateProperty areaTriggerCreateProperty;
    private AreaTriggerTemplate areaTriggerTemplate;


    private int periodicProcTimer;

    private int basePeriodicProcTimer;
    private ArrayList<AreaTriggerScript> loadedScripts = new ArrayList<>();

    private Cell currentCell;
    private CellMoveState moveState;
    private Position newPosition = new Position();

    public AreaTrigger() {
        super(false);
        previousCheckOrientation = Float.POSITIVE_INFINITY;
        reachedDestination = true;

        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.areaTrigger.getValue()));
        setObjectTypeId(TypeId.areaTrigger);

        updateFlag.stationary = true;
        updateFlag.areaTrigger = true;

        areaTriggerData = new areaTriggerFieldData();

        spline = new spline<Integer>();
    }

    public static AreaTrigger createAreaTrigger(int areaTriggerCreatePropertiesId, Unit caster, Unit target, SpellInfo spell, Position pos, int duration, SpellCastVisualField spellVisual, ObjectGuid castId) {
        return createAreaTrigger(areaTriggerCreatePropertiesId, caster, target, spell, pos, duration, spellVisual, castId, null);
    }

    public static AreaTrigger createAreaTrigger(int areaTriggerCreatePropertiesId, Unit caster, Unit target, SpellInfo spell, Position pos, int duration, SpellCastVisualField spellVisual) {
        return createAreaTrigger(areaTriggerCreatePropertiesId, caster, target, spell, pos, duration, spellVisual, null, null);
    }

    public static AreaTrigger createAreaTrigger(int areaTriggerCreatePropertiesId, Unit caster, Unit target, SpellInfo spell, Position pos, int duration, SpellCastVisualField spellVisual, ObjectGuid castId, AuraEffect aurEff) {
        AreaTrigger at = new AreaTrigger();

        if (!at.create(areaTriggerCreatePropertiesId, caster, target, spell, pos, duration, spellVisual, castId, aurEff)) {
            return null;
        }

        return at;
    }


    public static ObjectGuid createNewMovementForceId(Map map, int areaTriggerId) {
        return ObjectGuid.create(HighGuid.AreaTrigger, map.getId(), areaTriggerId, map.generateLowGuid(HighGuid.AreaTrigger));
    }


    @Override
    public int getFaction() {
        var caster = getCaster();

        if (caster) {
            return caster.getFaction();
        }

        return 0;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getCasterGuid();
    }

    public final boolean isServerSide() {
        return areaTriggerTemplate.id.isServerSide;
    }

    public final boolean isRemoved() {
        return isRemoved;
    }


    public final int getSpellId() {
        return areaTriggerData.spellID;
    }

    public final AuraEffect getAuraEff() {
        return aurEff;
    }


    public final int getTimeSinceCreated() {
        return timeSinceCreated;
    }


    public final int getTimeToTarget() {
        return areaTriggerData.timeToTarget;
    }


    public final int getTimeToTargetScale() {
        return areaTriggerData.timeToTargetScale;
    }

    public final int getDuration() {
        return duration;
    }

    public final void setDuration(int newDuration) {
        duration = newDuration;
        totalDuration = newDuration;

        // negative duration (permanent areatrigger) sent as 0
        setUpdateFieldValue(getValues().modifyValue(areaTriggerData).modifyValue(areaTriggerData.duration), Math.max(newDuration, 0));
    }

    public final int getTotalDuration() {
        return totalDuration;
    }

    public final HashSet<ObjectGuid> getInsideUnits() {
        return insideUnits;
    }

    public final AreaTriggerCreateProperty getCreateProperties() {
        return areaTriggerCreateProperty;
    }

    public final ObjectGuid getCasterGuid() {
        return areaTriggerData.caster;
    }
    // @todo: research the right value, in sniffs both timers are nearly identical

    public final AreaTriggerShapeInfo getShape() {
        return shape;
    }

    public final Vector3 getRollPitchYaw() {
        return rollPitchYaw;
    }

    public final Vector3 getTargetRollPitchYaw() {
        return targetRollPitchYaw;
    }

    public final boolean getHasSplines() {
        return !spline.isEmpty();
    }

    public final Spline getSpline() {
        return spline;
    }

    public final int getElapsedTimeForMovement() {
        return getTimeSinceCreated();
    }

    public final AreaTriggerOrbitInfo getCircularMovementInfo() {
        return orbitInfo;
    }

    private float getProgress() {
        return getTimeSinceCreated() < getTimeToTargetScale() ? (float) getTimeSinceCreated() / getTimeToTargetScale() : 1.0f;
    }

    private Unit getTarget() {
        return global.getObjAccessor().GetUnit(this, targetGuid);
    }

    private float getMaxSearchRadius() {
        return maxSearchRadius;
    }

    @Override
    public void addToWorld() {
        // Register the AreaTrigger for guid lookup and for caster
        if (!isInWorld()) {

            getMap().getObjectsStore().TryAdd(getGUID(), this);

            if (spawnId != 0) {
                getMap().getAreaTriggerBySpawnIdStore().add(spawnId, this);
            }

            super.addToWorld();
        }
    }

    @Override
    public void removeFromWorld() {
        // Remove the AreaTrigger from the accessor and from all lists of objects in world
        if (isInWorld()) {
            isRemoved = true;

            var caster = getCaster();

            if (caster) {
                caster._UnregisterAreaTrigger(this);
            }

            // Handle removal of all units, calling OnUnitExit & deleting auras if needed
            handleUnitEnterExit(new ArrayList<Unit>());

            this.<IAreaTriggerOnRemove>ForEachAreaTriggerScript(a -> a.onRemove());

            super.removeFromWorld();

            if (spawnId != 0) {
                getMap().getAreaTriggerBySpawnIdStore().remove(spawnId, this);
            }

            tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

            getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
            _ = tempOut__.outArgValue;
        }
    }

    @Override
    public boolean loadFromDB(long spawnId, Map map, boolean addToMap, boolean allowDuplicate) {
        spawnId = spawnId;

        var position = global.getAreaTriggerDataStorage().GetAreaTriggerSpawn(spawnId);

        if (position == null) {
            return false;
        }

        var areaTriggerTemplate = global.getAreaTriggerDataStorage().GetAreaTriggerTemplate(position.triggerId);

        if (areaTriggerTemplate == null) {
            return false;
        }

        return createServer(map, areaTriggerTemplate, position);
    }

    @Override
    public void update(int diff) {
        super.update(diff);
        timeSinceCreated += diff;

        if (!isServerSide()) {
            // "If" order matter here, Orbit > Attached > Splines
            if (hasOrbit()) {
                updateOrbitPosition();
            } else if (getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.HasAttached)) {
                var target = getTarget();

                if (target) {
                    getMap().areaTriggerRelocation(this, target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ(), target.getLocation().getO());
                }
            } else {
                updateSplinePosition(diff);
            }

            if (getDuration() != -1) {
                if (getDuration() > diff) {
                    _UpdateDuration(_duration - diff);
                } else {
                    remove(); // expired

                    return;
                }
            }
        }

        this.<IAreaTriggerOnUpdate>ForEachAreaTriggerScript(a -> a.onUpdate(diff));

        updateTargetList();

        if (basePeriodicProcTimer != 0) {
            if (periodicProcTimer <= diff) {
                this.<IAreaTriggerOnPeriodicProc>ForEachAreaTriggerScript(a -> a.onPeriodicProc());
                periodicProcTimer = basePeriodicProcTimer;
            } else {
                _periodicProcTimer -= diff;
            }
        }
    }

    public final void remove() {
        if (isInWorld()) {
            addObjectToRemoveList();
        }
    }

    public final AreaTriggerTemplate getTemplate() {
        return areaTriggerTemplate;
    }

    public final Unit getCaster() {
        return global.getObjAccessor().GetUnit(this, getCasterGuid());
    }

    public final void updateShape() {
        if (shape.isPolygon()) {
            updatePolygonOrientation();
        }
    }


    public final void initSplines(ArrayList<Vector3> splinePoints, int timeToTarget) {
        if (splinePoints.size() < 2) {
            return;
        }

        movementTime = 0;

        spline.initSpline(splinePoints.toArray(new Vector3[0]), splinePoints.size(), EvaluationMode.Linear);
        spline.initLengths();

        // should be sent in object create packets only
        doWithSuppressingObjectUpdates(() ->
        {
            setUpdateFieldValue(getValues().modifyValue(areaTriggerData).modifyValue(areaTriggerData.timeToTarget), timeToTarget);
            areaTriggerData.clearChanged(areaTriggerData.timeToTarget);
        });

        if (isInWorld()) {
            if (reachedDestination) {
                AreaTriggerRePath reshapeDest = new AreaTriggerRePath();
                reshapeDest.triggerGUID = getGUID();
                sendMessageToSet(reshapeDest, true);
            }

            AreaTriggerRePath reshape = new AreaTriggerRePath();
            reshape.triggerGUID = getGUID();
            reshape.areaTriggerSpline = new AreaTriggerSplineInfo();
            reshape.areaTriggerSpline.elapsedTimeForMovement = getElapsedTimeForMovement();
            reshape.areaTriggerSpline.timeToTarget = timeToTarget;
            reshape.areaTriggerSpline.points = splinePoints;
            sendMessageToSet(reshape, true);
        }

        reachedDestination = false;
    }

    public final boolean hasOrbit() {
        return orbitInfo != null;
    }


    public final void setPeriodicProcTimer(int periodicProctimer) {
        basePeriodicProcTimer = periodicProctimer;
        periodicProcTimer = periodicProctimer;
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt8((byte) flags.getValue());
        getObjectData().writeCreate(buffer, flags, this, target);
        areaTriggerData.writeCreate(buffer, flags, this, target);

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

        if (getValues().hasChanged(TypeId.areaTrigger)) {
            areaTriggerData.writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    public final void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedAreaTriggerMask, Player target) {
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        if (requestedAreaTriggerMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().areaTrigger.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().areaTrigger.getValue())) {
            areaTriggerData.writeUpdate(buffer, requestedAreaTriggerMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(areaTriggerData);
        super.clearUpdateMask(remove);
    }

    @Override
    public boolean isNeverVisibleFor(WorldObject seer) {
        return super.isNeverVisibleFor(seer) || isServerSide();
    }


    public final boolean setDestination(int timeToTarget, Position targetPos) {
        return setDestination(timeToTarget, targetPos, null);
    }

    public final boolean setDestination(int timeToTarget) {
        return setDestination(timeToTarget, null, null);
    }

    public final boolean setDestination(int timeToTarget, Position targetPos, WorldObject startingObject) {
        var path = new PathGenerator(startingObject != null ? startingObject : getCaster());
        var result = path.calculatePath(targetPos != null ? targetPos : getLocation(), true);

        if (!result || (path.getPathType().getValue() & PathType.NOPATH.getValue()) != 0) {
            return false;
        }

        initSplines(path.getPath().ToList(), timeToTarget);

        return true;
    }

    public final void delay(int delaytime) {
        setDuration(getDuration() - delaytime);
    }

    public final <T extends IAreaTriggerScript> ArrayList<IAreaTriggerScript> getAreaTriggerScripts() {
        TValue scripts;
        if (scriptsByType.containsKey(T.class) && (scripts = scriptsByType.get(T.class)) == scripts) {
            return scripts;
        }

        return DUMMY;
    }

    public final <T extends IAreaTriggerScript> void forEachAreaTriggerScript(tangible.Action1Param<T> action) {
        for (T script : this.<T>GetAreaTriggerScripts()) {
            action.invoke(script);
        }
    }


    private boolean create(int areaTriggerCreatePropertiesId, Unit caster, Unit target, SpellInfo spell, Position pos, int duration, SpellCastVisualField spellVisual, ObjectGuid castId, AuraEffect aurEff) {
        areaTriggerId = areaTriggerCreatePropertiesId;
        loadScripts();
        this.<IAreaTriggerOnInitialize>ForEachAreaTriggerScript(a -> a.onInitialize());

        targetGuid = target ? target.getGUID() : ObjectGuid.Empty;
        aurEff = aurEff;

        setMap(caster.getMap());
        getLocation().relocate(pos);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.areaTrigger, String.format("AreaTrigger (areaTriggerCreatePropertiesId: %1$s) not created. Invalid coordinates (X: %2$s Y: %3$s)", areaTriggerCreatePropertiesId, getLocation().getX(), getLocation().getY()));

            return false;
        }

        this.<IAreaTriggerOverrideCreateProperties>ForEachAreaTriggerScript(a -> areaTriggerCreateProperty = a.AreaTriggerCreateProperties);

        if (areaTriggerCreateProperty == null) {
            areaTriggerCreateProperty = global.getAreaTriggerDataStorage().GetAreaTriggerCreateProperties(areaTriggerCreatePropertiesId);

            if (areaTriggerCreateProperty == null) {
                Log.outError(LogFilter.areaTrigger, String.format("AreaTrigger (areaTriggerCreatePropertiesId %1$s) not created. Invalid areatrigger create properties id (%2$s)", areaTriggerCreatePropertiesId, areaTriggerCreatePropertiesId));

                return false;
            }
        }

        areaTriggerTemplate = areaTriggerCreateProperty.template;

        create(ObjectGuid.create(HighGuid.AreaTrigger, getLocation().getMapId(), getTemplate() != null ? getTemplate().id.Id : 0, caster.getMap().generateLowGuid(HighGuid.AreaTrigger)));

        if (getTemplate() != null) {
            setEntry(getTemplate().id.id);
        }

        setDuration(duration);

        setObjectScale(1.0f);

        shape = getCreateProperties().shape;
        maxSearchRadius = getCreateProperties().getMaxSearchRadius();

        var areaTriggerData = getValues().modifyValue(areaTriggerData);
        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.caster), caster.getGUID());
        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.creatingEffectGUID), castId);

        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.spellID), spell.getId());
        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.spellForVisuals), spell.getId());

        SpellCastVisualField spellCastVisual = areaTriggerData.modifyValue(areaTriggerData.spellVisual);
        tangible.RefObject<Integer> tempRef_SpellXSpellVisualID = new tangible.RefObject<Integer>(spellCastVisual.spellXSpellVisualID);
        setUpdateFieldValue(tempRef_SpellXSpellVisualID, spellVisual.spellXSpellVisualID);
        spellCastVisual.spellXSpellVisualID = tempRef_SpellXSpellVisualID.refArgValue;
        tangible.RefObject<Integer> tempRef_ScriptVisualID = new tangible.RefObject<Integer>(spellCastVisual.scriptVisualID);
        setUpdateFieldValue(tempRef_ScriptVisualID, spellVisual.scriptVisualID);
        spellCastVisual.scriptVisualID = tempRef_ScriptVisualID.refArgValue;

        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.timeToTargetScale), getCreateProperties().timeToTargetScale != 0 ? getCreateProperties().TimeToTargetScale : areaTriggerData.duration);
        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.boundsRadius2D), getMaxSearchRadius());
        setUpdateFieldValue(areaTriggerData.modifyValue(areaTriggerData.decalPropertiesID), getCreateProperties().DecalPropertiesId);

        ScaleCurve extraScaleCurve = areaTriggerData.modifyValue(areaTriggerData.extraScaleCurve);

        if (getCreateProperties().ExtraScale.structured.startTimeOffset != 0) {
            setUpdateFieldValue(extraScaleCurve.modifyValue(extraScaleCurve.startTimeOffset), getCreateProperties().ExtraScale.structured.startTimeOffset);
        }

        if (getCreateProperties().ExtraScale.structured.X != 0 || getCreateProperties().ExtraScale.structured.Y != 0) {
            Vector2 point = new Vector2(getCreateProperties().ExtraScale.structured.X, getCreateProperties().ExtraScale.structured.Y);

            setUpdateFieldValue(ref extraScaleCurve.modifyValue(extraScaleCurve.points, 0), point);
        }

        if (getCreateProperties().ExtraScale.structured.Z != 0 || getCreateProperties().ExtraScale.structured.W != 0) {
            Vector2 point = new Vector2(getCreateProperties().ExtraScale.structured.Z, getCreateProperties().ExtraScale.structured.W);

            setUpdateFieldValue(ref extraScaleCurve.modifyValue(extraScaleCurve.points, 1), point);
        }


//		unsafe
//			{
//				if (CreateProperties.ExtraScale.raw.Data[5] != 0)
//					setUpdateFieldValue(extraScaleCurve.modifyValue(extraScaleCurve.parameterCurve), CreateProperties.ExtraScale.raw.Data[5]);
//
//				if (CreateProperties.ExtraScale.structured.overrideActive != 0)
//					setUpdateFieldValue(extraScaleCurve.modifyValue(extraScaleCurve.overrideActive), CreateProperties.ExtraScale.structured.overrideActive != 0);
//			}

        VisualAnim visualAnim = areaTriggerData.modifyValue(areaTriggerData.visualAnim);
        setUpdateFieldValue(visualAnim.modifyValue(visualAnim.animationDataID), getCreateProperties().AnimId);
        setUpdateFieldValue(visualAnim.modifyValue(visualAnim.animKitID), getCreateProperties().animKitId);

        if (getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.unk3)) {
            setUpdateFieldValue(visualAnim.modifyValue(visualAnim.field_C), true);
        }

        PhasingHandler.inheritPhaseShift(this, caster);

        if (target && getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.HasAttached)) {
            getMovementInfo().transport.guid = target.getGUID();
        }

        updatePositionData();
        setZoneScript();

        updateShape();

        var timeToTarget = getCreateProperties().timeToTarget != 0 ? getCreateProperties().TimeToTarget : areaTriggerData.duration;

        if (getCreateProperties().OrbitInfo != null) {
            var orbit = getCreateProperties().OrbitInfo;

            if (target && getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.HasAttached)) {
                orbit.pathTarget = target.getGUID();
            } else {
                orbit.center = new Vector3(pos.getX(), pos.getY(), pos.getZ());
            }

            initOrbit(orbit, timeToTarget);
        } else if (getCreateProperties().HasSplines()) {
            initSplineOffsets(getCreateProperties().SplinePoints, timeToTarget);
        }

        // movement on transport of areatriggers on unit is handled by themself
        var transport = getMovementInfo().transport.guid.isEmpty() ? caster.getTransport() : null;

        if (transport != null) {
            var newPos = pos.Copy();
            transport.calculatePassengerOffset(newPos);
            getMovementInfo().transport.pos.relocate(newPos);

            // This object must be added to transport before adding to map for the client to properly display it
            transport.addPassenger(this);
        }

        // Relocate areatriggers with circular movement again
        if (hasOrbit()) {
            getLocation().relocate(calculateOrbitPosition());
        }

        if (!getMap().addToMap(this)) {
            // Returning false will cause the object to be deleted - remove from transport
            if (transport != null) {
                transport.removePassenger(this);
            }

            return false;
        }

        caster._RegisterAreaTrigger(this);

        this.<IAreaTriggerOnCreate>ForEachAreaTriggerScript(a -> a.onCreate());

        return true;
    }

    private boolean createServer(Map map, AreaTriggerTemplate areaTriggerTemplate, AreaTriggerSpawn position) {
        setMap(map);
        getLocation().relocate(position.spawnPoint);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.areaTrigger, String.format("AreaTriggerServer (id %1$s) not created. Invalid coordinates (X: %2$s Y: %3$s)", areaTriggerTemplate.id, getLocation().getX(), getLocation().getY()));

            return false;
        }

        areaTriggerTemplate = areaTriggerTemplate;

        create(ObjectGuid.create(HighGuid.AreaTrigger, getLocation().getMapId(), areaTriggerTemplate.id.id, getMap().generateLowGuid(HighGuid.AreaTrigger)));

        setEntry(areaTriggerTemplate.id.id);

        setObjectScale(1.0f);

        shape = position.shape;
        maxSearchRadius = shape.getMaxSearchRadius();

        if (position.phaseUseFlags != 0 || position.phaseId != 0 || position.phaseGroup != 0) {
            PhasingHandler.initDbPhaseShift(getPhaseShift(), position.phaseUseFlags, position.phaseId, position.phaseGroup);
        }

        updateShape();

        this.<IAreaTriggerOnCreate>ForEachAreaTriggerScript(a -> a.onCreate());

        return true;
    }

    private void _UpdateDuration(int newDuration) {
        duration = newDuration;

        // should be sent in object create packets only
        doWithSuppressingObjectUpdates(() ->
        {
            setUpdateFieldValue(getValues().modifyValue(areaTriggerData).modifyValue(areaTriggerData.duration), duration);
            areaTriggerData.clearChanged(areaTriggerData.duration);
        });
    }

    private void updateTargetList() {
        ArrayList<Unit> targetList = new ArrayList<>();

        switch (shape.triggerType) {
            case Sphere:
                searchUnitInSphere(targetList);

                break;
            case Box:
                searchUnitInBox(targetList);

                break;
            case Polygon:
                searchUnitInPolygon(targetList);

                break;
            case Cylinder:
                searchUnitInCylinder(targetList);

                break;
            case Disk:
                searchUnitInDisk(targetList);

                break;
            case BoundedPlane:
                searchUnitInBoundedPlane(targetList);

                break;
        }

        if (getTemplate() != null) {
            var conditions = global.getConditionMgr().getConditionsForAreaTrigger(getTemplate().id.id, getTemplate().id.isServerSide);

            if (!conditions.isEmpty()) {
                tangible.ListHelper.removeAll(targetList, target -> !global.getConditionMgr().isObjectMeetToConditions(target, conditions));
            }
        }

        handleUnitEnterExit(targetList);
    }

    private void searchUnits(ArrayList<Unit> targetList, float radius, boolean check3D) {
        var check = new AnyUnitInObjectRangeCheck(this, radius, check3D);

        if (isServerSide()) {
            var searcher = new PlayerListSearcher(this, targetList, check);
            Cell.visitGrid(this, searcher, getMaxSearchRadius());
        } else {
            var searcher = new UnitListSearcher(this, targetList, check, gridType.All);
            Cell.visitGrid(this, searcher, getMaxSearchRadius());
        }
    }

    private void searchUnitInSphere(ArrayList<Unit> targetList) {
        var radius = shape.SphereDatas.radius;

        if (getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.HasDynamicShape)) {
            if (getCreateProperties().MorphCurveId != 0) {
                radius = MathUtil.lerp(shape.SphereDatas.radius, shape.SphereDatas.RadiusTarget, global.getDB2Mgr().GetCurveValueAt(getCreateProperties().MorphCurveId, getProgress()));
            }
        }

        searchUnits(targetList, radius, true);
    }

    private void searchUnitInBox(ArrayList<Unit> targetList) {
        searchUnits(targetList, getMaxSearchRadius(), false);

        Position boxCenter = getLocation();
        float extentsX, extentsY, extentsZ;


//		unsafe
//			{
//				extentsX = shape.BoxDatas.Extents[0];
//				extentsY = shape.BoxDatas.Extents[1];
//				extentsZ = shape.BoxDatas.Extents[2];
//			}

        tangible.ListHelper.removeAll(targetList, unit -> !unit.location.IsWithinBox(boxCenter, extentsX, extentsY, extentsZ));
    }

    private void searchUnitInPolygon(ArrayList<Unit> targetList) {
        searchUnits(targetList, getMaxSearchRadius(), false);

        var height = shape.PolygonDatas.height;
        var minZ = getLocation().getZ() - height;
        var maxZ = getLocation().getZ() + height;

        tangible.ListHelper.removeAll(targetList, unit -> !checkIsInPolygon2D(unit.location) || unit.location.Z < minZ || unit.location.Z > maxZ);
    }

    private void searchUnitInCylinder(ArrayList<Unit> targetList) {
        searchUnits(targetList, getMaxSearchRadius(), false);

        var height = shape.CylinderDatas.height;
        var minZ = getLocation().getZ() - height;
        var maxZ = getLocation().getZ() + height;

        tangible.ListHelper.removeAll(targetList, unit -> unit.location.Z < minZ || unit.location.Z > maxZ);
    }

    private void searchUnitInDisk(ArrayList<Unit> targetList) {
        searchUnits(targetList, getMaxSearchRadius(), false);

        var innerRadius = shape.DiskDatas.InnerRadius;
        var height = shape.DiskDatas.height;
        var minZ = getLocation().getZ() - height;
        var maxZ = getLocation().getZ() + height;

        tangible.ListHelper.removeAll(targetList, unit -> unit.location.IsInDist2d(getLocation(), innerRadius) || unit.location.Z < minZ || unit.location.Z > maxZ);
    }

    private void searchUnitInBoundedPlane(ArrayList<Unit> targetList) {
        searchUnits(targetList, getMaxSearchRadius(), false);

        Position boxCenter = getLocation();
        float extentsX, extentsY;


//		unsafe
//			{
//				extentsX = shape.BoxDatas.Extents[0];
//				extentsY = shape.BoxDatas.Extents[1];
//			}

        tangible.ListHelper.removeAll(targetList, unit ->
        {
            return !unit.location.IsWithinBox(boxCenter, extentsX, extentsY, MapDefine.MapSize);
        });
    }

    private void handleUnitEnterExit(ArrayList<Unit> newTargetList) {
        var exitUnits = insideUnits.ToHashSet();
        insideUnits.clear();

        ArrayList<Unit> enteringUnits = new ArrayList<>();

        for (var unit : newTargetList) {
            if (!exitUnits.remove(unit.getGUID())) // erase(key_type) returns number of elements erased
            {
                enteringUnits.add(unit);
            }

            insideUnits.add(unit.getGUID()); // if the unit is in the new target list we need to add it. This broke rain of fire.
        }

        // Handle after _insideUnits have been reinserted so we can use GetInsideUnits() in hooks
        for (var unit : enteringUnits) {
            var player = unit.toPlayer();

            if (player) {
                if (player.isDebugAreaTriggers()) {
                    player.sendSysMessage(SysMessage.DebugAreatriggerEntered, getEntry());
                }

                player.updateQuestObjectiveProgress(QuestObjectiveType.AreaTriggerEnter, (int) getEntry(), 1);
            }

            doActions(unit);
            this.<IAreaTriggerOnUnitEnter>ForEachAreaTriggerScript(a -> a.onUnitEnter(unit));
        }

        for (var exitUnitGuid : exitUnits) {
            var leavingUnit = global.getObjAccessor().GetUnit(this, exitUnitGuid);

            if (leavingUnit) {
                var player = leavingUnit.toPlayer();

                if (player) {
                    if (player.isDebugAreaTriggers()) {
                        player.sendSysMessage(SysMessage.DebugAreatriggerLeft, getEntry());
                    }

                    player.updateQuestObjectiveProgress(QuestObjectiveType.AreaTriggerExit, (int) getEntry(), 1);
                }

                undoActions(leavingUnit);

                this.<IAreaTriggerOnUnitExit>ForEachAreaTriggerScript(a -> a.onUnitExit(leavingUnit));
            }
        }
    }

    private void updatePolygonOrientation() {
        var newOrientation = getLocation().getO();

        // No need to recalculate, orientation didn't change
        if (MathUtil.fuzzyEq(previousCheckOrientation, newOrientation)) {
            return;
        }

        polygonVertices = getCreateProperties().PolygonVertices;

        var angleSin = (float) Math.sin(newOrientation);
        var angleCos = (float) Math.cos(newOrientation);

        // This is needed to rotate the vertices, following orientation
        for (var i = 0; i < polygonVertices.size(); ++i) {
            var vertice = polygonVertices.get(i);

            vertice.X = vertice.X * angleCos - vertice.Y * angleSin;
            vertice.Y = vertice.Y * angleCos + vertice.X * angleSin;
        }

        previousCheckOrientation = newOrientation;
    }

    private boolean checkIsInPolygon2D(Position pos) {
        var testX = pos.getX();
        var testY = pos.getY();

        //this method uses the ray tracing algorithm to determine if the point is in the polygon
        var locatedInPolygon = false;

        for (var vertex = 0; vertex < polygonVertices.size(); ++vertex) {
            int nextVertex;

            //repeat loop for all sets of points
            if (vertex == (polygonVertices.size() - 1)) {
                //if i is the last vertex, let j be the first vertex
                nextVertex = 0;
            } else {
                //for all-else, let j=(i+1)th vertex
                nextVertex = vertex + 1;
            }

            var vertXi = getLocation().getX() + polygonVertices.get(vertex).X;
            var vertYi = getLocation().getY() + polygonVertices.get(vertex).Y;
            var vertXj = getLocation().getX() + polygonVertices.get(nextVertex).X;
            var vertYj = getLocation().getY() + polygonVertices.get(nextVertex).Y;

            // following statement checks if testPoint.Y is below Y-coord of i-th vertex
            var belowLowY = vertYi > testY;
            // following statement checks if testPoint.Y is below Y-coord of i+1-th vertex
            var belowHighY = vertYj > testY;

			/* following statement is true if testPoint.Y satisfies either (only one is possible)
			-.(i).Y < testPoint.Y < (i+1).Y        OR
			-.(i).Y > testPoint.Y > (i+1).Y

			(note)
			Both of the conditions indicate that a point is located within the edges of the Y-th coordinate
			of the (i)-th and the (i+1)- th vertices of the polygon. If neither of the above
			conditions is satisfied, then it is assured that a semi-infinite horizontal line draw
			to the right from the testpoint will NOT cross the line that connects vertices i and i+1
			of the polygon
			*/
            var withinYsEdges = belowLowY != belowHighY;

            if (withinYsEdges) {
                // this is the slope of the line that connects vertices i and i+1 of the polygon
                var slopeOfLine = (vertXj - vertXi) / (vertYj - vertYi);

                // this looks up the x-coord of a point lying on the above line, given its y-coord
                var pointOnLine = (slopeOfLine * (testY - vertYi)) + vertXi;

                //checks to see if x-coord of testPoint is smaller than the point on the line with the same y-coord
                var isLeftToLine = testX < pointOnLine;

                if (isLeftToLine) {
                    //this statement changes true to false (and vice-versa)
                    locatedInPolygon = !locatedInPolygon; //end if (isLeftToLine)
                }
            } //end if (withinYsEdges
        }

        return locatedInPolygon;
    }

    private boolean unitFitToActionRequirement(Unit unit, Unit caster, AreaTriggerAction action) {
        switch (action.targetType) {
            case Friend:
                return caster.isValidAssistTarget(unit, global.getSpellMgr().getSpellInfo(action.param, caster.getMap().getDifficultyID()));
            case Enemy:
                return caster.isValidAttackTarget(unit, global.getSpellMgr().getSpellInfo(action.param, caster.getMap().getDifficultyID()));
            case Raid:
                return caster.isInRaidWith(unit);
            case Party:
                return caster.isInPartyWith(unit);
            case Caster:
                return Objects.equals(unit.getGUID(), caster.getGUID());
            case Any:
            default:
                break;
        }

        return true;
    }

    private void doActions(Unit unit) {
        var caster = isServerSide() ? unit : getCaster();

        if (caster != null && getTemplate() != null) {
            for (var action : getTemplate().actions) {
                if (isServerSide() || unitFitToActionRequirement(unit, caster, action)) {
                    switch (action.actionType) {
                        case Cast:
                            caster.castSpell(unit, action.param, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCastId(areaTriggerData.creatingEffectGUID.getValue().isCast() ? areaTriggerData.CreatingEffectGUID : ObjectGuid.Empty));

                            break;
                        case AddAura:
                            caster.addAura(action.param, unit);

                            break;
                        case Teleport:
                            var safeLoc = global.getObjectMgr().getWorldSafeLoc(action.param);

                            Player player;
                            tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
                            if (safeLoc != null && caster.isPlayer(tempOut_player)) {
                                player = tempOut_player.outArgValue;
                                player.teleportTo(safeLoc.loc);
                            } else {
                                player = tempOut_player.outArgValue;
                            }

                            break;
                    }
                }
            }
        }
    }

    private void undoActions(Unit unit) {
        if (getTemplate() != null) {
            for (var action : getTemplate().actions) {
                if (action.actionType == AreaTriggerActionType.cast || action.actionType == AreaTriggerActionType.AddAura) {
                    unit.removeAurasDueToSpell(action.param, getCasterGuid());
                }
            }
        }
    }


    private void initSplineOffsets(ArrayList<Vector3> offsets, int timeToTarget) {
        var angleSin = (float) Math.sin(getLocation().getO());
        var angleCos = (float) Math.cos(getLocation().getO());

        // This is needed to rotate the spline, following caster orientation
        ArrayList<Vector3> rotatedPoints = new ArrayList<>();

        for (var offset : offsets) {
            var x = getLocation().getX() + (offset.X * angleCos - offset.Y * angleSin);
            var y = getLocation().getY() + (offset.Y * angleCos + offset.X * angleSin);
            var z = getLocation().getZ();

            z = updateAllowedPositionZ(x, y, z);
            z += offset.Z;

            rotatedPoints.add(new Vector3(x, y, z));
        }

        initSplines(rotatedPoints, timeToTarget);
    }


    private void initOrbit(AreaTriggerOrbitInfo orbit, int timeToTarget) {
        // should be sent in object create packets only
        doWithSuppressingObjectUpdates(() ->
        {
            setUpdateFieldValue(getValues().modifyValue(areaTriggerData).modifyValue(areaTriggerData.timeToTarget), timeToTarget);
            areaTriggerData.clearChanged(areaTriggerData.timeToTarget);
        });

        orbitInfo = orbit;

        orbitInfo.timeToTarget = timeToTarget;
        orbitInfo.elapsedTimeForMovement = 0;

        if (isInWorld()) {
            AreaTriggerRePath reshape = new AreaTriggerRePath();
            reshape.triggerGUID = getGUID();
            reshape.areaTriggerOrbit = orbitInfo;

            sendMessageToSet(reshape, true);
        }
    }

    private Position getOrbitCenterPosition() {
        if (orbitInfo == null) {
            return null;
        }

        if (orbitInfo.pathTarget != null) {
            var center = global.getObjAccessor().GetWorldObject(this, orbitInfo.pathTarget.getValue());

            if (center) {
                return center.getLocation();
            }
        }

        if (orbitInfo.center != null) {
            return new Position(orbitInfo.center.getValue());
        }

        return null;
    }

    private Position calculateOrbitPosition() {
        var centerPos = getOrbitCenterPosition();

        if (centerPos == null) {
            return getLocation();
        }

        var cmi = orbitInfo;

        // AreaTrigger make exactly "Duration / TimeToTarget" loops during his life time
        var pathProgress = (float) cmi.ElapsedTimeForMovement / cmi.timeToTarget;

        // We already made one circle and can't loop
        if (!cmi.canLoop) {
            pathProgress = Math.min(1.0f, pathProgress);
        }

        var radius = cmi.radius;

        if (MathUtil.fuzzyNe(cmi.blendFromRadius, radius)) {
            var blendCurve = (cmi.BlendFromRadius - radius) / radius;
            // 4.f Defines four quarters
            tangible.RefObject<Float> tempRef_blendCurve = new tangible.RefObject<Float>(blendCurve);
            blendCurve = MathUtil.RoundToInterval(tempRef_blendCurve, 1.0f, 4.0f) / 4.0f;
            blendCurve = tempRef_blendCurve.refArgValue;
            var blendProgress = Math.min(1.0f, pathProgress / blendCurve);
            radius = MathUtil.lerp(cmi.blendFromRadius, cmi.radius, blendProgress);
        }

        // Adapt Path progress depending of circle direction
        if (!cmi.counterClockwise) {
            pathProgress *= -1;
        }

        var angle = cmi.initialAngle + 2.0f * MathUtil.PI * pathProgress;
        var x = centerPos.getX() + (radius * (float) Math.cos(angle));
        var y = centerPos.getY() + (radius * (float) Math.sin(angle));
        var z = centerPos.getZ() + cmi.ZOffset;

        return new Position(x, y, z, angle);
    }

    private void updateOrbitPosition() {
        if (orbitInfo.startDelay > getElapsedTimeForMovement()) {
            return;
        }

        orbitInfo.elapsedTimeForMovement = getElapsedTimeForMovement() - orbitInfo.startDelay;

        var pos = calculateOrbitPosition();

        getMap().areaTriggerRelocation(this, pos.getX(), pos.getY(), pos.getZ(), pos.getO());

        debugVisualizePosition();
    }


    private void updateSplinePosition(int diff) {
        if (reachedDestination) {
            return;
        }

        if (!getHasSplines()) {
            return;
        }

        movementTime += diff;

        if (movementTime >= getTimeToTarget()) {
            reachedDestination = true;
            lastSplineIndex = spline.last();

            var lastSplinePosition = spline.getPoint(lastSplineIndex);
            getMap().areaTriggerRelocation(this, lastSplinePosition.X, lastSplinePosition.Y, lastSplinePosition.Z, getLocation().getO());

            debugVisualizePosition();

            this.<IAreaTriggerOnSplineIndexReached>ForEachAreaTriggerScript(a -> a.onSplineIndexReached(lastSplineIndex));
            this.<IAreaTriggerOnDestinationReached>ForEachAreaTriggerScript(a -> a.onDestinationReached());

            return;
        }

        var currentTimePercent = (float) _movementTime / getTimeToTarget();

        if (currentTimePercent <= 0.0f) {
            return;
        }

        if (getCreateProperties().MoveCurveId != 0) {
            var progress = global.getDB2Mgr().GetCurveValueAt(getCreateProperties().MoveCurveId, currentTimePercent);

            if (progress < 0.0f || progress > 1.0f) {
                Log.outError(LogFilter.areaTrigger, String.format("AreaTrigger (Id: %1$s, AreaTriggerCreatePropertiesId: %2$s) has wrong progress (%3$s) caused by curve calculation (MoveCurveId: %4$s)", getEntry(), getCreateProperties().id, progress, getCreateProperties().MorphCurveId));
            } else {
                currentTimePercent = progress;
            }
        }

        var lastPositionIndex = 0;
        float percentFromLastPoint = 0;
        tangible.RefObject<Integer> tempRef_lastPositionIndex = new tangible.RefObject<Integer>(lastPositionIndex);
        tangible.RefObject<Float> tempRef_percentFromLastPoint = new tangible.RefObject<Float>(percentFromLastPoint);
        spline.computeIndex(currentTimePercent, tempRef_lastPositionIndex, tempRef_percentFromLastPoint);
        percentFromLastPoint = tempRef_percentFromLastPoint.refArgValue;
        lastPositionIndex = tempRef_lastPositionIndex.refArgValue;

        Vector3 currentPosition;
        tangible.OutObject<Vector3> tempOut_currentPosition = new tangible.OutObject<Vector3>();
        spline.evaluate_Percent(lastPositionIndex, percentFromLastPoint, tempOut_currentPosition);
        currentPosition = tempOut_currentPosition.outArgValue;

        var orientation = getLocation().getO();

        if (getTemplate() != null && getTemplate().hasFlag(AreaTriggerFlags.HasFaceMovementDir)) {
            var nextPoint = spline.getPoint(lastPositionIndex + 1);
            orientation = getLocation().getAbsoluteAngle(nextPoint.X, nextPoint.Y);
        }

        getMap().areaTriggerRelocation(this, currentPosition.X, currentPosition.Y, currentPosition.Z, orientation);

        debugVisualizePosition();

        if (lastSplineIndex != lastPositionIndex) {
            lastSplineIndex = lastPositionIndex;
            this.<IAreaTriggerOnSplineIndexReached>ForEachAreaTriggerScript(a -> a.onSplineIndexReached(lastSplineIndex));
        }
    }


    private void debugVisualizePosition() {
        var caster = getCaster();

        if (caster) {
            var player = caster.toPlayer();

            if (player) {
                if (player.isDebugAreaTriggers()) {
                    player.summonCreature(1, getLocation(), TempSummonType.TimedDespawn, duration.ofSeconds(getTimeToTarget()));
                }
            }
        }
    }

    private void loadScripts() {
        loadedScripts = global.getScriptMgr().createAreaTriggerScripts(areaTriggerId, this);

        for (var script : loadedScripts) {
            Log.outDebug(LogFilter.spells, "AreaTrigger.LoadScripts: Script `{0}` for AreaTrigger `{1}` is loaded now", script._GetScriptName(), areaTriggerId);
            script.register();

            if (script instanceof IAreaTriggerScript) {
                for (var iFace : script.getClass().getInterfaces()) {
                    if (iFace.name.equals("IAreaTriggerScript")) {
                        continue;
                    }

                    TValue scripts;
                    if (!(scriptsByType.containsKey(iFace) && (scripts = scriptsByType.get(iFace)) == scripts)) {
                        scripts = new ArrayList<>();
                        scriptsByType.put(iFace, scripts);
                    }

                    scripts.add((IAreaTriggerScript) script);
                }
            }
        }
    }

    @Override
    public final void setNewCellPosition(float x, float y, float z, float o) {
        moveState = CellMoveState.ACTIVE;
        newPosition.relocate(x, y, z, o);
    }

    public boolean HasSplines() {
        //TODO
        return false;
    }

    public boolean HasOrbit() {
        //TODO
        return false;
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final AreaTrigger owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final areaTriggerFieldData areaTriggerMask = new areaTriggerFieldData();

        public ValuesUpdateForPlayerWithMaskSender(AreaTrigger owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), areaTriggerMask.getUpdateMask(), player);

            UpdateObject updateObject;
            tangible.OutObject<UpdateObject> tempOut_updateObject = new tangible.OutObject<UpdateObject>();
            udata.buildPacket(tempOut_updateObject);
            updateObject = tempOut_updateObject.outArgValue;
            player.sendPacket(updateObject);
        }
    }
}
