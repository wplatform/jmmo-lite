package Movement.Generators;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.spline.MoveSplineInit;


public class HomeMovementGenerator<T extends Creature> extends MovementGeneratorMedium<T> {
    public HomeMovementGenerator() {
        Mode = MovementGeneratorMode.Default;
        Priority = MovementGeneratorPriority.Normal;
        Flags = MovementGeneratorFlags.InitializationPending;
        BaseUnitState = UnitState.Roaming;
    }

    @Override
    public void DoInitialize(T owner) {
        RemoveFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        AddFlag(MovementGeneratorFlags.Initialized);

        owner.SetNoSearchAssistance(false);

        SetTargetLocation(owner);
    }

    @Override
    public void DoReset(T owner) {
        RemoveFlag(MovementGeneratorFlags.Deactivated);
        DoInitialize(owner);
    }

    @Override
    public boolean DoUpdate(T owner, int diff) {
        if (HasFlag(MovementGeneratorFlags.Interrupted) || owner.getMoveSpline().Finalized()) {
            AddFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void DoDeactivate(T owner) {
        AddFlag(MovementGeneratorFlags.Deactivated);
        owner.ClearUnitState(UnitState.RoamingMove);
    }

    @Override
    public void DoFinalize(T owner, boolean active, boolean movementInform) {
        if (!owner.isCreature()) {
            return;
        }

        AddFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.ClearUnitState(UnitState.RoamingMove.getValue() | UnitState.Evade.getValue());
        }

        if (movementInform && HasFlag(MovementGeneratorFlags.InformEnabled)) {
            if (!owner.getHasCanSwimFlagOutOfCombat()) {
                owner.RemoveUnitFlag(UnitFlag.CanSwim);
            }

            owner.SetSpawnHealth();
            owner.LoadCreaturesAddon();

            if (owner.isVehicle()) {
                owner.getVehicleKit().Reset(true);
            }

            var ai = owner.getAI();

            if (ai != null) {
                ai.JustReachedHome();
            }
        }
    }

    @Override
    public MovementGeneratorType GetMovementGeneratorType() {
        return MovementGeneratorType.Home;
    }

    private void SetTargetLocation(T owner) {
        // if we are ROOT/STUNNED/DISTRACTED even after aura clear, finalize on next update - otherwise we would get stuck in evade
        if (owner.HasUnitState(UnitState.Root.getValue() | UnitState.Stunned.getValue().getValue() | UnitState.Distracted.getValue().getValue())) {
            AddFlag(MovementGeneratorFlags.Interrupted);

            return;
        }

        owner.ClearUnitState(UnitState.AllErasable.getValue() & ~UnitState.Evade.getValue());
        owner.AddUnitState(UnitState.RoamingMove);

        var destination = owner.getHomePosition();
        MoveSplineInit init = new MoveSplineInit(owner);
        /*
         * TODO: maybe this never worked, who knows, top is always this generator, so this code calls GetResetPosition on itself
         *
         * if (owner->GetMotionMaster()->empty() || !owner->GetMotionMaster()->top()->GetResetPosition(owner, x, y, z))
         * {
         *     owner->GetHomePosition(x, y, z, o);
         *     init.SetFacing(o);
         * }
         */

        owner.UpdateAllowedPositionZ(destination);
        init.MoveTo(destination);
        init.SetFacing(destination.getO());
        init.SetWalk(false);
        init.Launch();
    }
}
