package com.github.azeroth.game.movement;


import com.github.azeroth.game.ai.AISelector;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.generator.*;
import com.github.azeroth.game.movement.spline.MoveSpline;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.utils.MathUtil;

import java.util.ArrayList;
import java.util.TreeSet;

public class MotionMaster {
    public static final float GRAVITY = 19.29110527038574f;
    public static final float SPEED_CHARGE = 42.0f;
    private static final IdleMovementGenerator staticIdleMovement = new IdleMovementGenerator();

    private static int splineId;

    private final Unit owner;
    private final TreeSet<MovementGenerator> generators = new TreeSet<MovementGenerator>(new movementGeneratorComparator());

    private final MultiMap<Integer, MovementGenerator> baseUnitStatesMap = new MultiMap<Integer, MovementGenerator>();
    private final ConcurrentQueue<DelayedAction> delayedActions = new ConcurrentQueue<DelayedAction>();
    private MovementGenerator defaultGenerator;
    private MotionMasterFlags flags = MotionMasterFlags.values()[0];

    public MotionMaster(Unit unit) {
        owner = unit;
        setFlags(MotionMasterFlags.InitializationPending);
    }


    public static int getSplineId() {
        return splineId++;
    }

    public static MovementGenerator getIdleMovementGenerator() {
        return staticIdleMovement;
    }

    public static boolean isStatic(MovementGenerator movement) {
        return (movement == getIdleMovementGenerator());
    }

    public static boolean isInvalidMovementGeneratorType(MovementGeneratorType type) {
        return type == MovementGeneratorType.MaxDB || type.getValue() >= MovementGeneratorType.max.getValue();
    }

    public static boolean isInvalidMovementSlot(MovementSlot slot) {
        return slot.getValue() >= MovementSlot.max.getValue();
    }

    private Unit getOwner() {
        return owner;
    }

    private MovementGenerator getDefaultGenerator() {
        return defaultGenerator;
    }

    private void setDefaultGenerator(MovementGenerator value) {
        defaultGenerator = value;
    }

    private TreeSet<MovementGenerator> getGenerators() {
        return generators;
    }


    private MultiMap<Integer, MovementGenerator> getBaseUnitStatesMap() {
        return baseUnitStatesMap;
    }

    private ConcurrentQueue<DelayedAction> getDelayedActions() {
        return delayedActions;
    }

    private MotionMasterFlags getFlags() {
        return flags;
    }

    private void setFlags(MotionMasterFlags value) {
        flags = value;
    }

    public final void initialize() {
        if (hasFlag(MotionMasterFlags.InitializationPending)) {
            return;
        }

        if (hasFlag(MotionMasterFlags.Update)) {

            getDelayedActions().Enqueue(new DelayedAction(this::Initialize, MotionMasterDelayedActionType.Initialize));

            return;
        }

        directInitialize();
    }

    public final void initializeDefault() {
        add(AISelector.selectMovementGenerator(getOwner()), MovementSlot.Default);
    }

    public final void addToWorld() {
        if (!hasFlag(MotionMasterFlags.InitializationPending)) {
            return;
        }

        addFlag(MotionMasterFlags.Initializing);
        removeFlag(MotionMasterFlags.InitializationPending);

        directInitialize();
        resolveDelayedActions();

        removeFlag(MotionMasterFlags.Initializing);
    }

    public final boolean empty() {
        synchronized (getGenerators()) {
            return getDefaultGenerator() == null && getGenerators().isEmpty();
        }
    }

    public final int size() {
        synchronized (getGenerators()) {
            return (getDefaultGenerator() != null ? 1 : 0) + getGenerators().size();
        }
    }

    public final ArrayList<MovementGeneratorInformation> getMovementGeneratorsInformation() {
        ArrayList<MovementGeneratorInformation> list = new ArrayList<>();

        if (getDefaultGenerator() != null) {
            list.add(new MovementGeneratorInformation(getDefaultGenerator().getMovementGeneratorType(), ObjectGuid.Empty, ""));
        }

        synchronized (getGenerators()) {
            for (var movement : getGenerators()) {
                var type = movement.getMovementGeneratorType();

                switch (type) {
                    case MovementGeneratorType.Chase:
                    case MovementGeneratorType.Follow:
                        var followInformation = movement instanceof FollowMovementGenerator ? (FollowMovementGenerator) movement : null;

                        if (followInformation != null) {
                            var target = followInformation.getTarget();

                            if (target != null) {
                                list.add(new MovementGeneratorInformation(type, target.getGUID(), target.getName()));
                            } else {
                                list.add(new MovementGeneratorInformation(type, ObjectGuid.Empty));
                            }
                        } else {
                            list.add(new MovementGeneratorInformation(type, ObjectGuid.Empty));
                        }

                        break;
                    default:
                        list.add(new MovementGeneratorInformation(type, ObjectGuid.Empty));

                        break;
                }
            }
        }

        return list;
    }

    public final MovementSlot getCurrentSlot() {
        synchronized (getGenerators()) {
            if (!getGenerators().isEmpty()) {
                return MovementSlot.active;
            }
        }

        if (getDefaultGenerator() != null) {
            return MovementSlot.Default;
        }

        return MovementSlot.max;
    }

    public final MovementGenerator getCurrentMovementGenerator() {
        synchronized (getGenerators()) {
            if (!getGenerators().isEmpty()) {
                return getGenerators().FirstOrDefault();
            }
        }

        if (getDefaultGenerator() != null) {
            return getDefaultGenerator();
        }

        return null;
    }

    public final MovementGeneratorType getCurrentMovementGeneratorType() {
        if (empty()) {
            return MovementGeneratorType.max;
        }

        var movement = getCurrentMovementGenerator();

        if (movement == null) {
            return MovementGeneratorType.max;
        }

        return movement.getMovementGeneratorType();
    }

    public final MovementGeneratorType getCurrentMovementGeneratorType(MovementSlot slot) {
        if (empty() || isInvalidMovementSlot(slot)) {
            return MovementGeneratorType.max;
        }

        synchronized (getGenerators()) {
            if (slot == MovementSlot.active && !getGenerators().isEmpty()) {
                return getGenerators().FirstOrDefault().getMovementGeneratorType();
            }
        }

        if (slot == MovementSlot.Default && getDefaultGenerator() != null) {
            return getDefaultGenerator().getMovementGeneratorType();
        }

        return MovementGeneratorType.max;
    }

    public final MovementGenerator getCurrentMovementGenerator(MovementSlot slot) {
        if (empty() || isInvalidMovementSlot(slot)) {
            return null;
        }

        synchronized (getGenerators()) {
            if (slot == MovementSlot.active && !getGenerators().isEmpty()) {
                return getGenerators().FirstOrDefault();
            }
        }

        if (slot == MovementSlot.Default && getDefaultGenerator() != null) {
            return getDefaultGenerator();
        }

        return null;
    }

    public final MovementGenerator getMovementGenerator(Func<MovementGenerator, Boolean> filter) {
        return getMovementGenerator(filter, MovementSlot.active);
    }

    public final MovementGenerator getMovementGenerator(tangible.Func1Param<MovementGenerator, Boolean> filter, MovementSlot slot) {
        if (empty() || isInvalidMovementSlot(slot)) {
            return null;
        }

        MovementGenerator movement = null;

        switch (slot) {
            case Default:
                if (getDefaultGenerator() != null && filter.invoke(getDefaultGenerator())) {
                    movement = getDefaultGenerator();
                }

                break;
            case Active:
                synchronized (getGenerators()) {
                    if (!getGenerators().isEmpty()) {
                        var itr = getGenerators().FirstOrDefault(filter);

                        if (itr != null) {
                            movement = itr;
                        }
                    }
                }

                break;
            default:
                break;
        }

        return movement;
    }

    public final boolean hasMovementGenerator(Func<MovementGenerator, Boolean> filter) {
        return hasMovementGenerator(filter, MovementSlot.active);
    }

    public final boolean hasMovementGenerator(tangible.Func1Param<MovementGenerator, Boolean> filter, MovementSlot slot) {
        if (empty() || isInvalidMovementSlot(slot)) {
            return false;
        }

        var value = false;

        switch (slot) {
            case Default:
                if (getDefaultGenerator() != null && filter.invoke(getDefaultGenerator())) {
                    value = true;
                }

                break;
            case Active:
                synchronized (getGenerators()) {
                    if (!getGenerators().isEmpty()) {
                        var itr = getGenerators().FirstOrDefault(filter);
                        value = itr != null;
                    }
                }

                break;
            default:
                break;
        }

        return value;
    }


    public final void update(int diff) {
        try {
            if (!getOwner()) {
                return;
            }

            if (hasFlag(MotionMasterFlags.InitializationPending.getValue() | MotionMasterFlags.Initializing.getValue())) {
                return;
            }

            addFlag(MotionMasterFlags.Update);

            var top = getCurrentMovementGenerator();

            if (hasFlag(MotionMasterFlags.StaticInitializationPending) && isStatic(top)) {
                removeFlag(MotionMasterFlags.StaticInitializationPending);
                top.initialize(getOwner());
            }

            if (top.hasFlag(MovementGeneratorFlags.InitializationPending)) {
                top.initialize(getOwner());
            }

            if (top.hasFlag(MovementGeneratorFlags.Deactivated)) {
                top.reset(getOwner());
            }

            if (!top.update(getOwner(), diff)) {
                // Since all the actions that modify any slot are delayed, this movement is guaranteed to be top
                synchronized (getGenerators()) {
                    pop(true, true); // Natural, and only, call to MovementInform
                }
            }

            resolveDelayedActions();
        } catch (RuntimeException ex) {
            Log.outException(ex, "");
        } finally {
            removeFlag(MotionMasterFlags.Update);
        }
    }

    public final void remove(MovementGenerator movement) {
        remove(movement, MovementSlot.active);
    }

    public final void remove(MovementGenerator movement, MovementSlot slot) {
        if (movement == null || isInvalidMovementSlot(slot)) {
            return;
        }

        if (hasFlag(MotionMasterFlags.Delayed)) {
            getDelayedActions().Enqueue(new DelayedAction(() -> remove(movement, slot), MotionMasterDelayedActionType.Remove));

            return;
        }

        if (empty()) {
            return;
        }

        switch (slot) {
            case Default:
                if (getDefaultGenerator() != null && getDefaultGenerator() == movement) {
                    directClearDefault();
                }

                break;
            case Active:
                synchronized (getGenerators()) {
                    if (!getGenerators().isEmpty()) {
                        if (getGenerators().contains(movement)) {
                            remove(movement, getCurrentMovementGenerator() == movement, false);
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    public final void remove(MovementGeneratorType type) {
        remove(type, MovementSlot.active);
    }

    public final void remove(MovementGeneratorType type, MovementSlot slot) {
        if (isInvalidMovementGeneratorType(type) || isInvalidMovementSlot(slot)) {
            return;
        }

        if (hasFlag(MotionMasterFlags.Delayed)) {
            getDelayedActions().Enqueue(new DelayedAction(() -> remove(type, slot), MotionMasterDelayedActionType.RemoveType));

            return;
        }

        if (empty()) {
            return;
        }

        switch (slot) {
            case Default:
                if (getDefaultGenerator() != null && getDefaultGenerator().getMovementGeneratorType() == type) {
                    directClearDefault();
                }

                break;
            case Active:
                synchronized (getGenerators()) {
                    if (!getGenerators().isEmpty()) {
                        var itr = getGenerators().FirstOrDefault(a -> a.getMovementGeneratorType() == type);

                        if (itr != null) {
                            remove(itr, getCurrentMovementGenerator() == itr, false);
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    public final void clear() {
        if (hasFlag(MotionMasterFlags.Delayed)) {
            getDelayedActions().Enqueue(new DelayedAction(this::Clear, MotionMasterDelayedActionType.Clear));

            return;
        }

        if (!empty()) {
            directClear();
        }
    }

    public final void clear(MovementSlot slot) {
        if (isInvalidMovementSlot(slot)) {
            return;
        }

        if (hasFlag(MotionMasterFlags.Delayed)) {
            getDelayedActions().Enqueue(new DelayedAction(() -> clear(slot), MotionMasterDelayedActionType.ClearSlot));

            return;
        }

        if (empty()) {
            return;
        }

        switch (slot) {
            case Default:
                directClearDefault();

                break;
            case Active:
                directClear();

                break;
            default:
                break;
        }
    }

    public final void clear(MovementGeneratorMode mode) {
        if (hasFlag(MotionMasterFlags.Delayed)) {
            getDelayedActions().Enqueue(new DelayedAction(() -> clear(mode), MotionMasterDelayedActionType.ClearMode));

            return;
        }

        if (empty()) {
            return;
        }

        directClear(a -> a.mode == mode);
    }

    public final void clear(MovementGeneratorPriority priority) {
        if (hasFlag(MotionMasterFlags.Delayed)) {

            getDelayedActions().Enqueue(new DelayedAction(() -> clear(priority), MotionMasterDelayedActionType.ClearPriority));

            return;
        }

        if (empty()) {
            return;
        }

        directClear(a -> a.priority == priority);
    }

    public final void propagateSpeedChange() {
        if (empty()) {
            return;
        }

        var movement = getCurrentMovementGenerator();

        if (movement == null) {
            return;
        }

        movement.unitSpeedChanged();
    }

    public final boolean getDestination(tangible.OutObject<Float> x, tangible.OutObject<Float> y, tangible.OutObject<Float> z) {
        x.outArgValue = 0f;
        y.outArgValue = 0f;
        z.outArgValue = 0f;

        if (getOwner().getMoveSpline().finalized()) {
            return false;
        }

        var dest = getOwner().getMoveSpline().finalDestination();
        x.outArgValue = dest.X;
        y.outArgValue = dest.Y;
        z.outArgValue = dest.Z;

        return true;
    }

    public final boolean stopOnDeath() {
        var movementGenerator = getCurrentMovementGenerator();

        if (movementGenerator != null) {
            if (movementGenerator.hasFlag(MovementGeneratorFlags.PersistOnDeath)) {
                return false;
            }
        }

        if (getOwner().isInWorld()) {
            // Only clear MotionMaster for entities that exists in world
            // Avoids crashes in the following conditions :
            //  * Using 'call pet' on dead pets
            //  * Using 'call stabled pet'
            //  * Logging in with dead pets
            clear();
            moveIdle();
        }

        getOwner().stopMoving();

        return true;
    }

    public final void moveIdle() {
        add(getIdleMovementGenerator(), MovementSlot.Default);
    }

    public final void moveTargetedHome() {
        var owner = getOwner().toCreature();

        if (owner == null) {
            Log.outError(LogFilter.movement, String.format("MotionMaster::MoveTargetedHome: '%1$s', attempted to move towards target home.", getOwner().getGUID()));

            return;
        }

        clear();

        var target = owner.getCharmerOrOwner();

        if (target == null) {
            add(new HomeMovementGenerator<Creature>());
        } else {
            add(new FollowMovementGenerator(target, SharedConst.PetFollowDist, new chaseAngle(SharedConst.PetFollowAngle)));
        }
    }

    public final void moveRandom(float wanderDistance) {
        moveRandom(wanderDistance, null);
    }

    public final void moveRandom(float wanderDistance, Duration duration) {
        if (getOwner().isTypeId(TypeId.UNIT)) {
            add(new RandomMovementGenerator(wanderDistance, duration), MovementSlot.Default);
        }
    }

    public final void moveFollow(Unit target, float dist, float angle) {
        moveFollow(target, dist, angle, MovementSlot.active);
    }

    public final void moveFollow(Unit target, float dist) {
        moveFollow(target, dist, 0.0f, MovementSlot.active);
    }

    public final void moveFollow(Unit target, float dist, float angle, MovementSlot slot) {
        moveFollow(target, dist, new chaseAngle(angle), slot);
    }

    public final void moveFollow(Unit target, float dist, ChaseAngle angle) {
        moveFollow(target, dist, angle, MovementSlot.active);
    }

    public final void moveFollow(Unit target, float dist, ChaseAngle angle, MovementSlot slot) {
        // Ignore movement request if target not exist
        if (!target || target == getOwner()) {
            return;
        }

        add(new FollowMovementGenerator(target, dist, angle), slot);
    }

    public final void moveChase(Unit target, float dist) {
        moveChase(target, dist, 0.0f);
    }

    public final void moveChase(Unit target, float dist, float angle) {
        moveChase(target, new ChaseRange(dist), new chaseAngle(angle));
    }

    public final void moveChase(Unit target, float dist) {
        moveChase(target, new ChaseRange(dist));
    }

    public final void moveChase(Unit target, ChaseRange dist) {
        moveChase(target, dist, null);
    }

    public final void moveChase(Unit target) {
        moveChase(target, null, null);
    }

    public final void moveChase(Unit target, ChaseRange dist, ChaseAngle angle) {
        // Ignore movement request if target not exist
        if (!target || target == getOwner()) {
            return;
        }

        add(new ChaseMovementGenerator(target, dist, angle));
    }

    public final void moveConfused() {
        if (getOwner().isTypeId(TypeId.PLAYER)) {
            add(new ConfusedMovementGenerator<Player>());
        } else {
            add(new ConfusedMovementGenerator<Creature>());
        }
    }


    public final void moveFleeing(Unit enemy, int time) {
        if (!enemy) {
            return;
        }

        if (getOwner().isCreature()) {
            if (time != 0) {
                add(new TimedFleeingMovementGenerator(enemy.getGUID(), time));
            } else {
                add(new FleeingMovementGenerator<ObjectGuid.opGreaterThan(CREATURE, (enemy.getGUID())));
            }
        } else {
            add(new FleeingMovementGenerator<ObjectGuid.opGreaterThan(player, (enemy.getGUID())));
        }
    }

    public final void movePoint(int id, Position pos, boolean generatePath, Float finalOrient, float speed, MovementWalkRunSpeedSelectionMode speedSelectionMode) {
        movePoint(id, pos, generatePath, finalOrient, speed, speedSelectionMode, 0);
    }

    public final void movePoint(int id, Position pos, boolean generatePath, Float finalOrient, float speed) {
        movePoint(id, pos, generatePath, finalOrient, speed, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, Position pos, boolean generatePath, Float finalOrient) {
        movePoint(id, pos, generatePath, finalOrient, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, Position pos, boolean generatePath) {
        movePoint(id, pos, generatePath, null, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, Position pos) {
        movePoint(id, pos, true, null, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, Position pos, boolean generatePath, Float finalOrient, float speed, MovementWalkRunSpeedSelectionMode speedSelectionMode, float closeEnoughDistance) {
        movePoint(id, pos.getX(), pos.getY(), pos.getZ(), generatePath, finalOrient, speed, speedSelectionMode, closeEnoughDistance);
    }

    public final void movePoint(int id, float x, float y, float z, boolean generatePath, Float finalOrient, float speed, MovementWalkRunSpeedSelectionMode speedSelectionMode) {
        movePoint(id, x, y, z, generatePath, finalOrient, speed, speedSelectionMode, 0);
    }

    public final void movePoint(int id, float x, float y, float z, boolean generatePath, Float finalOrient, float speed) {
        movePoint(id, x, y, z, generatePath, finalOrient, speed, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, float x, float y, float z, boolean generatePath, Float finalOrient) {
        movePoint(id, x, y, z, generatePath, finalOrient, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, float x, float y, float z, boolean generatePath) {
        movePoint(id, x, y, z, generatePath, null, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, float x, float y, float z) {
        movePoint(id, x, y, z, true, null, 0, MovementWalkRunSpeedSelectionMode.Default, 0);
    }

    public final void movePoint(int id, float x, float y, float z, boolean generatePath, Float finalOrient, float speed, MovementWalkRunSpeedSelectionMode speedSelectionMode, float closeEnoughDistance) {
        add(new PointMovementGenerator(id, x, y, z, generatePath, speed, finalOrient, null, null, speedSelectionMode, closeEnoughDistance));
    }


    public final void moveCloserAndStop(int id, Unit target, float distance) {
        var distanceToTravel = getOwner().getLocation().getExactdist2D(target.getLocation()) - distance;

        if (distanceToTravel > 0.0f) {
            var angle = getOwner().getLocation().getAbsoluteAngle(target.getLocation());
            var destx = getOwner().getLocation().getX() + distanceToTravel * (float) Math.cos(angle);
            var desty = getOwner().getLocation().getY() + distanceToTravel * (float) Math.sin(angle);
            movePoint(id, destx, desty, target.getLocation().getZ());
        } else {
            // We are already close enough. We just need to turn toward the target without changing position.
            var initializer = (MoveSplineInit init) ->
            {
                init.moveTo(getOwner().getLocation().getX(), getOwner().getLocation().getY(), getOwner().getLocation().getZ());
                var refreshedTarget = global.getObjAccessor().GetUnit(getOwner(), target.getGUID());

                if (refreshedTarget != null) {
                    init.setFacing(refreshedTarget);
                }
            };

            add(new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id));
        }
    }

    public final void moveLand(int id, Position pos) {
        moveLand(id, pos, null);
    }

    public final void moveLand(int id, Position pos, Float velocity) {
        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(pos, false);
            init.setAnimation(animTier.ground);

            if (velocity != null) {
                init.setVelocity(velocity.floatValue());
            }
        };

        add(new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id));
    }

    public final void moveTakeoff(int id, Position pos) {
        moveTakeoff(id, pos, null);
    }

    public final void moveTakeoff(int id, Position pos, Float velocity) {
        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(pos, false);
            init.setAnimation(animTier.Hover);

            if (velocity != null) {
                init.setVelocity(velocity.floatValue());
            }
        };

        add(new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id));
    }

    public final void moveCharge(float x, float y, float z, float speed, int id, boolean generatePath, Unit target) {
        moveCharge(x, y, z, speed, id, generatePath, target, null);
    }

    public final void moveCharge(float x, float y, float z, float speed, int id, boolean generatePath) {
        moveCharge(x, y, z, speed, id, generatePath, null, null);
    }

    public final void moveCharge(float x, float y, float z, float speed, int id) {
        moveCharge(x, y, z, speed, id, false, null, null);
    }

    public final void moveCharge(float x, float y, float z, float speed) {
        moveCharge(x, y, z, speed, eventId.charge, false, null, null);
    }

    public final void moveCharge(float x, float y, float z) {
        moveCharge(x, y, z, SPEED_CHARGE, eventId.charge, false, null, null);
    }

    public final void moveCharge(float x, float y, float z, float speed, int id, boolean generatePath, Unit target, SpellEffectExtraData spellEffectExtraData) {
		/*
		if (_slot[(int)MovementSlot.Controlled] != null && _slot[(int)MovementSlot.Controlled].getMovementGeneratorType() != MovementGeneratorType.Distract)
			return;
		*/

        PointMovementGenerator movement = new PointMovementGenerator(id, x, y, z, generatePath, speed, null, target, spellEffectExtraData);
        movement.priority = MovementGeneratorPriority.Highest;
        movement.baseUnitState = UnitState.Charging;
        add(movement);
    }

    public final void moveCharge(PathGenerator path, float speed, Unit target) {
        moveCharge(path, speed, target, null);
    }

    public final void moveCharge(PathGenerator path, float speed) {
        moveCharge(path, speed, null, null);
    }

    public final void moveCharge(PathGenerator path) {
        moveCharge(path, SPEED_CHARGE, null, null);
    }

    public final void moveCharge(PathGenerator path, float speed, Unit target, SpellEffectExtraData spellEffectExtraData) {
        var dest = path.getActualEndPosition();

        moveCharge(dest.X, dest.Y, dest.Z, SPEED_CHARGE, eventId.ChargePrepath);

        // Charge movement is not started when using EVENT_CHARGE_PREPATH
        MoveSplineInit init = new MoveSplineInit(getOwner());
        init.movebyPath(path.getPath());
        init.setVelocity(speed);

        if (target != null) {
            init.setFacing(target);
        }

        if (spellEffectExtraData != null) {
            init.setSpellEffectExtraData(spellEffectExtraData);
        }

        init.launch();
    }

    public final void moveKnockbackFrom(Position origin, float speedXY, float speedZ) {
        moveKnockbackFrom(origin, speedXY, speedZ, null);
    }

    public final void moveKnockbackFrom(Position origin, float speedXY, float speedZ, SpellEffectExtraData spellEffectExtraData) {
        //This function may make players fall below map
        if (getOwner().isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (speedXY < 0.01f) {
            return;
        }

        Position dest = getOwner().getLocation();
        var moveTimeHalf = (float) (speedZ / GRAVITY);
        var dist = 2 * moveTimeHalf * speedXY;
        var max_height = -MoveSpline.computeFallElevation(moveTimeHalf, false, -speedZ);

        // Use a mmap raycast to get a valid destination.
        getOwner().movePositionToFirstCollision(dest, dist, getOwner().getLocation().getRelativeAngle(origin) + MathUtil.PI);

        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(dest.getX(), dest.getY(), dest.getZ(), false);
            init.setParabolic(max_height, 0);
            init.setOrientationFixed(true);
            init.setVelocity(speedXY);

            if (spellEffectExtraData != null) {
                init.setSpellEffectExtraData(spellEffectExtraData);
            }
        };

        GenericMovementGenerator movement = new GenericMovementGenerator(initializer, MovementGeneratorType.effect, 0);
        movement.priority = MovementGeneratorPriority.Highest;
        movement.addFlag(MovementGeneratorFlags.PersistOnDeath);
        add(movement);
    }

    public final void moveJumpTo(float angle, float speedXY, float speedZ) {
        //This function may make players fall below map
        if (getOwner().isTypeId(TypeId.PLAYER)) {
            return;
        }

        var moveTimeHalf = (float) (speedZ / GRAVITY);
        var dist = 2 * moveTimeHalf * speedXY;
        Position nearPoint2D = getOwner().getNearPoint2D(null, dist, getOwner().getLocation().getO() + angle);
        var y = nearPoint2D.getY();
        var x = nearPoint2D.getX();
        var z = getOwner().getLocation().getZ();
        z = getOwner().updateAllowedPositionZ(x, y, z);
        moveJump(x, y, z, 0.0f, speedXY, speedZ);
    }

    public final void moveJump(Position pos, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast) {
        moveJump(pos, speedXY, speedZ, id, hasOrientation, arrivalCast, null);
    }

    public final void moveJump(Position pos, float speedXY, float speedZ, int id, boolean hasOrientation) {
        moveJump(pos, speedXY, speedZ, id, hasOrientation, null, null);
    }

    public final void moveJump(Position pos, float speedXY, float speedZ, int id) {
        moveJump(pos, speedXY, speedZ, id, false, null, null);
    }

    public final void moveJump(Position pos, float speedXY, float speedZ) {
        moveJump(pos, speedXY, speedZ, eventId.jump, false, null, null);
    }

    public final void moveJump(Position pos, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast, SpellEffectExtraData spellEffectExtraData) {
        moveJump(pos.getX(), pos.getY(), pos.getZ(), pos.getO(), speedXY, speedZ, id, hasOrientation, arrivalCast, spellEffectExtraData);
    }

    public final void moveJump(float x, float y, float z, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast) {
        moveJump(x, y, z, speedXY, speedZ, id, hasOrientation, arrivalCast, null);
    }

    public final void moveJump(float x, float y, float z, float speedXY, float speedZ, int id, boolean hasOrientation) {
        moveJump(x, y, z, speedXY, speedZ, id, hasOrientation, null, null);
    }

    public final void moveJump(float x, float y, float z, float speedXY, float speedZ, int id) {
        moveJump(x, y, z, speedXY, speedZ, id, false, null, null);
    }

    public final void moveJump(float x, float y, float z, float speedXY, float speedZ) {
        moveJump(x, y, z, speedXY, speedZ, eventId.jump, false, null, null);
    }

    public final void moveJump(float x, float y, float z, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast, SpellEffectExtraData spellEffectExtraData) {
        moveJump(x, y, z, 0, speedXY, speedZ, id, hasOrientation, arrivalCast, spellEffectExtraData);
    }

    public final void moveJump(float x, float y, float z, float o, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast) {
        moveJump(x, y, z, o, speedXY, speedZ, id, hasOrientation, arrivalCast, null);
    }

    public final void moveJump(float x, float y, float z, float o, float speedXY, float speedZ, int id, boolean hasOrientation) {
        moveJump(x, y, z, o, speedXY, speedZ, id, hasOrientation, null, null);
    }

    public final void moveJump(float x, float y, float z, float o, float speedXY, float speedZ, int id) {
        moveJump(x, y, z, o, speedXY, speedZ, id, false, null, null);
    }

    public final void moveJump(float x, float y, float z, float o, float speedXY, float speedZ) {
        moveJump(x, y, z, o, speedXY, speedZ, eventId.jump, false, null, null);
    }

    public final void moveJump(float x, float y, float z, float o, float speedXY, float speedZ, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast, SpellEffectExtraData spellEffectExtraData) {
        Log.outDebug(LogFilter.Server, "Unit ({0}) jump to point (X: {1} Y: {2} Z: {3})", getOwner().getGUID().toString(), x, y, z);

        if (speedXY < 0.01f) {
            return;
        }

        var moveTimeHalf = (float) (speedZ / GRAVITY);
        var max_height = -MoveSpline.computeFallElevation(moveTimeHalf, false, -speedZ);

        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(x, y, z, false);
            init.setParabolic(max_height, 0);
            init.setVelocity(speedXY);

            if (hasOrientation) {
                init.setFacing(o);
            }

            if (spellEffectExtraData != null) {
                init.setSpellEffectExtraData(spellEffectExtraData);
            }
        };

        int arrivalSpellId = 0;
        var arrivalSpellTargetGuid = ObjectGuid.Empty;

        if (arrivalCast != null) {
            arrivalSpellId = arrivalCast.spellId;
            arrivalSpellTargetGuid = arrivalCast.target;
        }

        GenericMovementGenerator movement = new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id, arrivalSpellId, arrivalSpellTargetGuid);
        movement.priority = MovementGeneratorPriority.Highest;
        movement.baseUnitState = UnitState.Jumping;
        add(movement);
    }

    public final void moveJumpWithGravity(Position pos, float speedXY, float gravity, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast) {
        moveJumpWithGravity(pos, speedXY, gravity, id, hasOrientation, arrivalCast, null);
    }

    public final void moveJumpWithGravity(Position pos, float speedXY, float gravity, int id, boolean hasOrientation) {
        moveJumpWithGravity(pos, speedXY, gravity, id, hasOrientation, null, null);
    }

    public final void moveJumpWithGravity(Position pos, float speedXY, float gravity, int id) {
        moveJumpWithGravity(pos, speedXY, gravity, id, false, null, null);
    }

    public final void moveJumpWithGravity(Position pos, float speedXY, float gravity) {
        moveJumpWithGravity(pos, speedXY, gravity, eventId.jump, false, null, null);
    }

    public final void moveJumpWithGravity(Position pos, float speedXY, float gravity, int id, boolean hasOrientation, JumpArrivalCastArgs arrivalCast, SpellEffectExtraData spellEffectExtraData) {
        Log.outDebug(LogFilter.movement, String.format("MotionMaster.MoveJumpWithGravity: '%1$s', jumps to point Id: %2$s (%3$s)", getOwner().getGUID(), id, pos));

        if (speedXY < 0.01f) {
            return;
        }

        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(pos.getX(), pos.getY(), pos.getZ(), false);
            init.setParabolicVerticalAcceleration(gravity, 0);
            init.setUncompressed();
            init.setVelocity(speedXY);
            init.setUnlimitedSpeed();

            if (hasOrientation) {
                init.setFacing(pos.getO());
            }

            if (spellEffectExtraData != null) {
                init.setSpellEffectExtraData(spellEffectExtraData);
            }
        };

        int arrivalSpellId = 0;
        ObjectGuid arrivalSpellTargetGuid = null;

        if (arrivalCast != null) {
            arrivalSpellId = arrivalCast.spellId;
            arrivalSpellTargetGuid = arrivalCast.target;
        }

        var movement = new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id, arrivalSpellId, arrivalSpellTargetGuid);
        movement.priority = MovementGeneratorPriority.Highest;
        movement.baseUnitState = UnitState.Jumping;
        movement.addFlag(MovementGeneratorFlags.PersistOnDeath);
        add(movement);
    }


    public final void moveCirclePath(float x, float y, float z, float radius, boolean clockwise, byte stepCount) {
        var initializer = (MoveSplineInit init) ->
        {
            var step = 2 * MathUtil.PI / stepCount * (clockwise ? -1.0f : 1.0f);
            Position pos = new Position(x, y, z, 0.0f);
            var angle = pos.getAbsoluteAngle(getOwner().getLocation().getX(), getOwner().getLocation().getY());

            // add the owner's current position as starting point as it gets removed after entering the cycle
            init.path().add(new Vector3(getOwner().getLocation().getX(), getOwner().getLocation().getY(), getOwner().getLocation().getZ()));

            for (byte i = 0; i < stepCount; angle += step, ++i) {
                Vector3 point = new Vector3();
                point.X = (float) (x + radius * Math.cos(angle));
                point.Y = (float) (y + radius * Math.sin(angle));

                if (getOwner().isFlying()) {
                    point.Z = z;
                } else {
                    point.Z = getOwner().getMapHeight(point.X, point.Y, z) + getOwner().getHoverOffset();
                }

                init.path().add(point);
            }

            if (getOwner().isFlying()) {
                init.setFly();
                init.setCyclic();
                init.setAnimation(animTier.Hover);
            } else {
                init.setWalk(true);
                init.setCyclic();
            }
        };

        add(new GenericMovementGenerator(initializer, MovementGeneratorType.effect, 0));
    }

    public final void moveSmoothPath(int pointId, Vector3[] pathPoints, int pathSize, boolean walk) {
        moveSmoothPath(pointId, pathPoints, pathSize, walk, false);
    }

    public final void moveSmoothPath(int pointId, Vector3[] pathPoints, int pathSize) {
        moveSmoothPath(pointId, pathPoints, pathSize, false, false);
    }

    public final void moveSmoothPath(int pointId, Vector3[] pathPoints, int pathSize, boolean walk, boolean fly) {
        var initializer = (MoveSplineInit init) ->
        {
            init.movebyPath(pathPoints);
            init.setWalk(walk);

            if (fly) {
                init.setFly();
                init.setUncompressed();
                init.setSmooth();
            }
        };

        // This code is not correct
        // GenericMovementGenerator does not affect UNIT_STATE_ROAMING_MOVE
        // need to call PointMovementGenerator with various pointIds
        add(new GenericMovementGenerator(initializer, MovementGeneratorType.effect, pointId));
    }


    public final void moveAlongSplineChain(int pointId, int dbChainId, boolean walk) {
        var owner = getOwner().toCreature();

        if (!owner) {
            Log.outError(LogFilter.misc, "MotionMaster.MoveAlongSplineChain: non-creature {0} tried to walk along DB spline chain. Ignoring.", getOwner().getGUID().toString());

            return;
        }

        var chain = global.getScriptMgr().getSplineChain(owner, (byte) dbChainId);

        if (chain.isEmpty()) {
            Log.outError(LogFilter.misc, "MotionMaster.MoveAlongSplineChain: creature with entry {0} tried to walk along non-existing spline chain with DB id {1}.", owner.getEntry(), dbChainId);

            return;
        }

        moveAlongSplineChain(pointId, chain, walk);
    }

    public final void moveFall() {
        moveFall(0);
    }

    public final void moveFall(int id) {
        // Use larger distance for vmap height search than in most other cases
        var tz = getOwner().getMapHeight(getOwner().getLocation().getX(), getOwner().getLocation().getY(), getOwner().getLocation().getZ(), true, MapDefine.MaxFallDistance);

        if (tz <= MapDefine.INVALID_HEIGHT) {
            return;
        }

        // Abort too if the ground is very near
        if (Math.abs(getOwner().getLocation().getZ() - tz) < 0.1f) {
            return;
        }

        // rooted units don't move (also setting falling+root flag causes client freezes)
        if (getOwner().hasUnitState(UnitState.Root.getValue() | UnitState.Stunned.getValue())) {
            return;
        }

        getOwner().setFall(true);

        // Don't run spline movement for players
        if (getOwner().isTypeId(TypeId.PLAYER)) {
            getOwner().toPlayer().setFallInformation(0, getOwner().getLocation().getZ());

            return;
        }

        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(getOwner().getLocation().getX(), getOwner().getLocation().getY(), tz + getOwner().getHoverOffset(), false);
            init.setFall();
        };

        GenericMovementGenerator movement = new GenericMovementGenerator(initializer, MovementGeneratorType.effect, id);
        movement.priority = MovementGeneratorPriority.Highest;
        add(movement);
    }

    public final void moveSeekAssistance(float x, float y, float z) {
        var creature = getOwner().toCreature();

        if (creature != null) {
            Log.outDebug(LogFilter.movement, String.format("MotionMaster::MoveSeekAssistance: '%1$s', seeks assistance (X: %2$s, Y: %3$s, Z: %4$s)", creature.getGUID(), x, y, z));
            creature.attackStop();
            creature.castStop();
            creature.doNotReacquireSpellFocusTarget();
            creature.setReactState(ReactStates.Passive);
            add(new AssistanceMovementGenerator(eventId.AssistMove, x, y, z));
        } else {
            Log.outError(LogFilter.Server, String.format("MotionMaster::MoveSeekAssistance: %1$s, attempted to seek assistance", getOwner().getGUID()));
        }
    }


    public final void moveSeekAssistanceDistract(int time) {
        if (getOwner().isCreature()) {
            add(new AssistanceDistractMovementGenerator(time, getOwner().getLocation().getO()));
        } else {
            Log.outError(LogFilter.Server, String.format("MotionMaster::MoveSeekAssistanceDistract: %1$s attempted to call distract after assistance", getOwner().getGUID()));
        }
    }


    public final void moveTaxiFlight(int path, int pathnode) {
        if (getOwner().isTypeId(TypeId.PLAYER)) {
            if (path < CliDB.TaxiPathNodesByPath.size()) {
                Log.outDebug(LogFilter.Server, String.format("MotionMaster::MoveTaxiFlight: %1$s taxi to Path Id: %2$s (node %3$s)", getOwner().getGUID(), path, pathnode));

                // Only one FLIGHT_MOTION_TYPE is allowed
                var hasExisting = hasMovementGenerator(gen -> gen.getMovementGeneratorType() == MovementGeneratorType.flight);

                FlightPathMovementGenerator movement = new FlightPathMovementGenerator();
                movement.loadPath(getOwner().toPlayer());
                add(movement);
            } else {
                Log.outError(LogFilter.movement, String.format("MotionMaster::MoveTaxiFlight: '%1$s', attempted taxi to non-existing path Id: %2$s (node: %3$s)", getOwner().getGUID(), path, pathnode));
            }
        } else {
            Log.outError(LogFilter.movement, String.format("MotionMaster::MoveTaxiFlight: '%1$s', attempted taxi to path Id: %2$s (node: %3$s)", getOwner().getGUID(), path, pathnode));
        }
    }


    public final void moveDistract(int timer, float orientation) {
		/*
		if (_slot[(int)MovementSlot.Controlled] != null)
			return;
		*/

        add(new DistractMovementGenerator(timer, orientation));
    }


    public final void movePath(int pathId, boolean repeatable) {
        if (pathId == 0) {
            return;
        }

        add(new WaypointMovementGenerator(pathId, repeatable), MovementSlot.Default);
    }

    public final void movePath(WaypointPath path, boolean repeatable) {
        add(new WaypointMovementGenerator(path, repeatable), MovementSlot.Default);
    }


    public final void moveRotate(int id, int time, RotateDirection direction) {
        if (time == 0) {
            return;
        }

        add(new RotateMovementGenerator(id, time, direction));
    }


    public final void moveFormation(Unit leader, float range, float angle, int point1, int point2) {
        if (getOwner().getObjectTypeId() == TypeId.UNIT && leader != null) {
            add(new FormationMovementGenerator(leader, range, angle, point1, point2), MovementSlot.Default);
        }
    }

    public final void launchMoveSpline(action<MoveSplineInit> initializer, int id, MovementGeneratorPriority priority) {
        launchMoveSpline(initializer, id, priority, MovementGeneratorType.effect);
    }

    public final void launchMoveSpline(action<MoveSplineInit> initializer, int id) {
        launchMoveSpline(initializer, id, MovementGeneratorPriority.NORMAL, MovementGeneratorType.effect);
    }

    public final void launchMoveSpline(action<MoveSplineInit> initializer) {
        launchMoveSpline(initializer, 0, MovementGeneratorPriority.NORMAL, MovementGeneratorType.effect);
    }

    public final void launchMoveSpline(tangible.Action1Param<MoveSplineInit> initializer, int id, MovementGeneratorPriority priority, MovementGeneratorType type) {
        if (isInvalidMovementGeneratorType(type)) {
            Log.outDebug(LogFilter.movement, String.format("MotionMaster::LaunchMoveSpline: '%1$s', tried to launch a spline with an invalid MovementGeneratorType: %2$s (Id: %3$s, Priority: %4$s)", getOwner().getGUID(), type, id, priority));

            return;
        }

        GenericMovementGenerator movement = new GenericMovementGenerator(initializer, type, id);
        movement.priority = priority;
        add(movement);
    }

    private void add(MovementGenerator movement) {
        add(movement, MovementSlot.active);
    }

    private void add(MovementGenerator movement, MovementSlot slot) {
        if (movement == null) {
            return;
        }

        if (isInvalidMovementSlot(slot)) {
            return;
        }

        if (hasFlag(MotionMasterFlags.Delayed)) {

            getDelayedActions().Enqueue(new DelayedAction(() -> add(movement, slot), MotionMasterDelayedActionType.Add));
        } else {
            directAdd(movement, slot);
        }
    }


    private void moveAlongSplineChain(int pointId, ArrayList<SplineChainLink> chain, boolean walk) {
        add(new SplineChainMovementGenerator(pointId, chain, walk));
    }

    private void resumeSplineChain(SplineChainResumeInfo info) {
        if (info.isEmpty()) {
            Log.outError(LogFilter.movement, "MotionMaster.ResumeSplineChain: unit with entry {0} tried to resume a spline chain from empty info.", getOwner().getEntry());

            return;
        }

        add(new SplineChainMovementGenerator(info));
    }

    private void resolveDelayedActions() {
        while (getDelayedActions().count != 0) {
            T action;
            tangible.OutObject<DelayedAction> tempOut_action = new tangible.OutObject<DelayedAction>();
            if (getDelayedActions().TryDequeue(tempOut_action) && action != null) {
                action = tempOut_action.outArgValue;
                action.resolve();
            } else {
                action = tempOut_action.outArgValue;
            }
        }
    }

    private void remove(MovementGenerator movement, boolean active, boolean movementInform) {
        getGenerators().remove(movement);
        delete(movement, active, movementInform);
    }

    private void pop(boolean active, boolean movementInform) {
        if (!getGenerators().isEmpty()) {
            remove(getGenerators().FirstOrDefault(), active, movementInform);
        }
    }

    private void directInitialize() {
        // Clear ALL movement generators (including default)
        directClearDefault();
        directClear();
        initializeDefault();
    }

    private void directClear() {
        synchronized (getGenerators()) {
            // First delete Top
            if (!getGenerators().isEmpty()) {
                pop(true, false);
            }

            // Then the rest
            while (!getGenerators().isEmpty()) {
                pop(false, false);
            }
        }

        // Make sure the storage is empty
        clearBaseUnitStates();
    }

    private void directClearDefault() {
        if (getDefaultGenerator() != null) {
            deleteDefault(getGenerators().isEmpty(), false);
        }
    }

    private void directClear(tangible.Func1Param<MovementGenerator, Boolean> filter) {
        if (getGenerators().isEmpty()) {
            return;
        }

        var top = getCurrentMovementGenerator();

        for (var movement : getGenerators().ToList()) {
            if (filter.invoke(movement)) {
                getGenerators().remove(movement);
                delete(movement, movement == top, false);
            }
        }
    }


    private void directAdd(MovementGenerator movement) {
        directAdd(movement, MovementSlot.active);
    }

    private void directAdd(MovementGenerator movement, MovementSlot slot) {
		/*
		IMovementGenerator curr = _slot[(int)slot];
		if (curr != null)
		{
			_slot[(int)slot] = null; // in case a new one is generated in this slot during directdelete
			if (_top == (int)slot && Convert.ToBoolean(_cleanFlag & MotionMasterCleanFlag.Update))
				DelayedDelete(curr);
			else
				DirectDelete(curr);
		}
		else if (_top < (int)slot)
		{
			_top = (int)slot;
		}

		_slot[(int)slot] = m;
		if (_top > (int)slot)
			_initialize[(int)slot] = true;
		else
		{
			_initialize[(int)slot] = false;
			m.initialize(owner);
		}
		*/

        /*
         * NOTE: This mimics old behaviour: only one MOTION_SLOT_IDLE, MOTION_SLOT_ACTIVE, MOTION_SLOT_CONTROLLED
         * On future changes support for multiple will be added
         */
        switch (slot) {
            case Default:
                if (getDefaultGenerator() != null) {
                    synchronized (getGenerators()) {
                        getDefaultGenerator().finalize(getOwner(), getGenerators().isEmpty(), false);
                    }
                }

                setDefaultGenerator(movement);

                if (isStatic(movement)) {
                    addFlag(MotionMasterFlags.StaticInitializationPending);
                }

                break;
            case Active:
                synchronized (getGenerators()) {
                    if (!getGenerators().isEmpty()) {
                        if (movement.priority.getValue() >= getGenerators().FirstOrDefault().priority) {
                            var itr = getGenerators().FirstOrDefault();

                            if (movement.priority == itr.priority) {
                                remove(itr, true, false);
                            } else {
                                itr.deactivate(getOwner());
                            }
                        } else {
                            var pointer = getGenerators().FirstOrDefault(a -> a.priority == movement.priority);

                            if (pointer != null) {
                                remove(pointer, false, false);
                            }
                        }
                    } else {
                        getDefaultGenerator().deactivate(getOwner());
                    }

                    getGenerators().add(movement);
                }
                addBaseUnitState(movement);

                break;
        }
    }

    private void delete(MovementGenerator movement, boolean active, boolean movementInform) {
        movement.finalize(getOwner(), active, movementInform);
        clearBaseUnitState(movement);
    }

    private void deleteDefault(boolean active, boolean movementInform) {
        getDefaultGenerator().finalize(getOwner(), active, movementInform);
        setDefaultGenerator(getIdleMovementGenerator());
        addFlag(MotionMasterFlags.StaticInitializationPending);
    }

    private void addBaseUnitState(MovementGenerator movement) {
        if (movement == null || movement.baseUnitState == 0) {
            return;
        }

        synchronized (getBaseUnitStatesMap()) {
            getBaseUnitStatesMap().add((int) movement.baseUnitState.getValue(), movement);
        }
        getOwner().addUnitState(movement.baseUnitState);
    }

    private void clearBaseUnitState(MovementGenerator movement) {
        if (movement == null || movement.baseUnitState == 0) {
            return;
        }
        synchronized (getBaseUnitStatesMap()) {
            getBaseUnitStatesMap().remove((int) movement.baseUnitState.getValue(), movement);
        }

        if (!getBaseUnitStatesMap().ContainsKey((int) movement.baseUnitState.getValue())) {
            getOwner().clearUnitState(movement.baseUnitState);
        }
    }

    private void clearBaseUnitStates() {
        int unitState = 0;

        synchronized (getBaseUnitStatesMap()) {
            for (var itr : getBaseUnitStatesMap().KeyValueList) {
                unitState |= itr.key;
            }

            getOwner().clearUnitState(UnitState.forValue(unitState));
            getBaseUnitStatesMap().clear();
        }
    }

    private void addFlag(MotionMasterFlags flag) {
        setFlags(MotionMasterFlags.forValue(getFlags().getValue() | flag.getValue()));
    }

    private boolean hasFlag(MotionMasterFlags flag) {
        return (getFlags().getValue() & flag.getValue()) != 0;
    }

    private void removeFlag(MotionMasterFlags flag) {
        setFlags(MotionMasterFlags.forValue(getFlags().getValue() & ~flag.getValue()));
    }
}
