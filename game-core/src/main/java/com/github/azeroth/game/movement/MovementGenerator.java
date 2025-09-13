package com.github.azeroth.game.movement;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public abstract class MovementGenerator implements IEquatable<MovementGenerator> {
    public MovementGeneratormode mode = MovementGeneratorMode.values()[0];
    public MovementGeneratorpriority priority = MovementGeneratorPriority.values()[0];
    public MovementGeneratorflags flags = MovementGeneratorFlags.values()[0];
    public UnitState baseUnitState = UnitState.values()[0];

    public final boolean equals(MovementGenerator other) {
        if (mode == other.mode && priority == other.priority) {
            return true;
        }

        return false;
    }

    // on top first update
    public void initialize(Unit owner) {
    }

    // on top reassign
    public void reset(Unit owner) {
    }

    // on top on MotionMaster::Update
    public abstract boolean update(Unit owner, int diff);

    // on current top if another movement replaces
    public void deactivate(Unit owner) {
    }

    // on movement delete
    public void finalize(Unit owner, boolean active, boolean movementInform) {
    }

    public abstract MovementGeneratorType getMovementGeneratorType();

    public void unitSpeedChanged() {
    }

    // timer in ms

    public void pause() {
        pause(0);
    }

    public void pause(int timer) {
    }

    // timer in ms

    public void resume() {
        resume(0);
    }

    public void resume(int overrideTimer) {
    }

    // used by Evade code for select point to evade with expected restart default movement
    public boolean getResetPosition(Unit u, tangible.OutObject<Float> x, tangible.OutObject<Float> y, tangible.OutObject<Float> z) {
        x.outArgValue = y.outArgValue = z.outArgValue = 0.0f;

        return false;
    }

    public final void addFlag(MovementGeneratorFlags flag) {
        flags = MovementGeneratorFlags.forValue(flags.getValue() | flag.getValue());
    }

    public final boolean hasFlag(MovementGeneratorFlags flag) {
        return (flags.getValue() & flag.getValue()) != 0;
    }

    public final void removeFlag(MovementGeneratorFlags flag) {
        flags = MovementGeneratorFlags.forValue(flags.getValue() & ~flag.getValue());
    }

    public final int hashCode(MovementGenerator obj) {
        return obj.mode.hashCode() ^ obj.priority.hashCode();
    }

    public String getDebugInfo() {
        return String.format("Mode: %1$s Priority: %2$s Flags: %3$s BaseUniteState: %4$s", mode, priority, flags, baseUnitState);
    }
}
