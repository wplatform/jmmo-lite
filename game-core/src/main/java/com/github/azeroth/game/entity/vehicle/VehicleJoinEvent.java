package com.github.azeroth.game.entity.vehicle;


import Framework.Dynamic.*;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnAddPassenger;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnInstallAccessory;
import game.entities.Vehicle;


public class VehicleJoinEvent extends BasicEvent {
    public Vehicle target;
    public Unit passenger;
    public java.util.Map.Entry<Byte, VehicleSeat> seat;

    public VehicleJoinEvent(Vehicle v, Unit u) {
        target = v;
        passenger = u;
        seat = target.seats.Last();
    }

    
//ORIGINAL LINE: public override bool Execute(ulong etime, uint pTime)
    @Override
    public boolean execute(long etime, int pTime) {
        var vehicleAuras = target.getBase().getAuraEffectsByType(AuraType.ControlVehicle);
        var aurEffect = tangible.ListHelper.find(vehicleAuras, aurEff = game.entities.Objects.equals( > aurEff.CasterGuid, passenger.getGUID().clone()))
        ;

        var aurApp = aurEffect.getBase().getApplicationOfTarget(target.getBase().getGUID().clone());

        target.removePendingEventsForSeat(seat.getKey());
        target.removePendingEventsForPassenger(passenger);

        // Passenger might've died in the meantime - abort if this is the case
        if (!passenger.isAlive()) {
            abort(0);

            return true;
        }

        //It's possible that multiple vehicle join
        //events are executed in the same update
        if (passenger.getVehicle1() != null) {
            passenger.exitVehicle();
        }

        passenger.setVehicle1(target);
        seat.getValue().Passenger.Guid = passenger.getGUID().clone();
        seat.getValue().Passenger.IsUninteractible = passenger.hasUnitFlag(UnitFlags.Uninteractible);
        seat.getValue().Passenger.IsGravityDisabled = passenger.hasUnitMovementFlag(MovementFlag.DisableGravity);

        if (seat.getValue().SeatInfo.CanEnterOrExit()) {
            --target.usableSeatNum;

            if (target.usableSeatNum == 0) {
                if (target.getBase().isTypeId(TypeId.PLAYER)) {
                    target.getBase().removeNpcFlag(NPCFlags.PlayerVehicle);
                } else {
                    target.getBase().removeNpcFlag(NPCFlags.SpellClick);
                }
            }
        }

        passenger.interruptNonMeleeSpells(false);
        passenger.removeAurasByType(AuraType.Mounted);

        var veSeat = seat.getValue().SeatInfo;
        var veSeatAddon = seat.getValue().SeatAddon;

        var player = passenger.toPlayer();

        if (player != null) {
            // drop flag
            var bg = player.getBattleground();

            if (bg) {
                bg.eventPlayerDroppedFlag(player);
            }

            player.stopCastingCharm();
            player.stopCastingBindSight();
            player.sendOnCancelExpectedVehicleRideAura();

            if (!veSeat.HasFlag(VehicleSeatFlagsB.KeepPet)) {
                player.unsummonPetTemporaryIfAny();
            }
        }

        if (veSeat.HasFlag(VehicleSeatFlags.DisableGravity)) {
            passenger.setDisableGravity(true);
        }

        var o = veSeatAddon != null ? veSeatAddon.SeatOrientationOffset : 0.0f;
        var x = veSeat.AttachmentOffset.X;
        var y = veSeat.AttachmentOffset.Y;
        var z = veSeat.AttachmentOffset.Z;

        passenger.movementInfo.transport.pos.relocate(x, y, z, o);
        passenger.movementInfo.transport.time = 0;
        passenger.movementInfo.transport.seat = seat.getKey();
        passenger.movementInfo.transport.guid = target.getBase().getGUID().clone();

        if (target.getBase().isTypeId(TypeId.Unit) && passenger.isTypeId(TypeId.PLAYER) && seat.getValue().SeatInfo.HasFlag(VehicleSeatFlags.CanControl)) {
            // handles SMSG_CLIENT_CONTROL
            if (!target.getBase().setCharmedBy(passenger, CharmType.Vehicle, aurApp)) {
                // charming failed, probably aura was removed by relocation/scripts/whatever
                abort(0);

                return true;
            }
        }

        passenger.sendClearTarget(); // SMSG_BREAK_TARGET
        passenger.setControlled(true, UnitState.Root); // SMSG_FORCE_ROOT - In some cases we send SMSG_SPLINE_MOVE_ROOT here (for creatures)
        // also adds MOVEMENTFLAG_ROOT

        var initializer = (MoveSplineInit init) -> {
            init.disableTransportPathTransformations();
            init.MoveTo(x, y, z, false, true);
            init.setFacing(o);
            init.setTransportEnter();
        };

        passenger.getMotionMaster().launchMoveSpline(initializer, EventId.VehicleBoard, MovementGeneratorPriority.Highest);

        for (var(_, threatRef) : passenger.getThreatManager().getThreatenedByMeList()) {
            threatRef.Owner.GetThreatManager().AddThreat(target.getBase(), threatRef.Threat, null, true, true);
        }

        var creature = target.getBase().toCreature();

        if (creature != null) {
            var ai = creature.getAI();

            if (ai != null) {
                ai.passengerBoarded(passenger, seat.getKey(), true);
            }

            Global.getScriptMgr().<IVehicleOnAddPassenger>runScript(p -> p.OnAddPassenger(target, passenger, seat.getKey()), target.getBase().toCreature().getScriptId());

            // Actually quite a redundant hook. Could just use OnAddPassenger and check for unit typemask inside script.
            if (passenger.hasUnitTypeMask(UnitTypeMask.Accessory)) {
                Global.getScriptMgr().<IVehicleOnInstallAccessory>runScript(p -> p.OnInstallAccessory(target, passenger.toCreature()), target.getBase().toCreature().getScriptId());
            }
        }

        return true;
    }

    
//ORIGINAL LINE: public override void Abort(ulong e_time)
    @Override
    public void abort(long eTime) {
        // Check if the Vehicle was already uninstalled, in which case all auras were removed already
        if (target != null) {
            Log.outDebug(LogFilter.Vehicle, "Passenger GuidLow: {0}, Entry: {1}, board on vehicle GuidLow: {2}, Entry: {3} SeatId: {4} cancelled", passenger.getGUID().toString(), passenger.getEntry(), target.getBase().getGUID().toString(), target.getBase().getEntry(), seat.getKey());

            // Remove the pending event when Abort was called on the event directly
            target.removePendingEvent(this);

            // @SPELL_AURA_CONTROL_VEHICLE auras can be applied even when the passenger is not (yet) on the vehicle.
            // When this code is triggered it means that something went wrong in @Vehicle.AddPassenger, and we should remove
            // the aura manually.
            target.getBase().removeAurasByType(AuraType.ControlVehicle, passenger.getGUID().clone());
        } else {
            Log.outDebug(LogFilter.Vehicle, "Passenger GuidLow: {0}, Entry: {1}, board on uninstalled vehicle SeatId: {2} cancelled", passenger.getGUID().toString(), passenger.getEntry(), seat.getKey());
        }

        if (passenger.isInWorld && passenger.hasUnitTypeMask(UnitTypeMask.Accessory)) {
            passenger.toCreature().despawnOrUnsummon();
        }
    }
}