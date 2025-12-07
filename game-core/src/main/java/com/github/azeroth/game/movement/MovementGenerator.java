package com.github.azeroth.game.movement;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
@Getter
@RequiredArgsConstructor
public abstract class MovementGenerator implements Comparable<MovementGenerator> {
    public MovementGeneratorMode mode;
    public MovementGeneratorPriority priority;
    public final EnumFlag<MovementGeneratorFlag> flags = EnumFlag.of(MovementGeneratorFlag.class);
    public UnitState baseUnitState;


    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MovementGenerator that = (MovementGenerator) object;
        return mode == that.mode && priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, priority);
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
    public Position getResetPosition(Unit u) {
        return null;
    }

    public final void addFlag(MovementGeneratorFlag flag) {
        flags.addFlag(flag);
    }

    public final boolean hasFlag(MovementGeneratorFlag flag) {
        return flags.hasFlag(flag);
    }
    public final void removeFlag(MovementGeneratorFlag flag) {
        flags.removeFlag(flag);
    }

    @Override
    public int compareTo(MovementGenerator o) {
        if (equals(o)) {
            return 0;
        }
        int i = mode.compareTo(o.mode);
        return i == 0 ? priority.compareTo(o.priority) : i;
    }

    @Override
    public String toString() {
        return String.format("Mode: %s Priority: %s Flags: %s BaseUniteState: %s", mode, priority, flags, baseUnitState);
    }
}
