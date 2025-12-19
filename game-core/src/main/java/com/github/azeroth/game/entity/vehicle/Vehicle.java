package com.github.azeroth.game.entity.vehicle;


import com.github.azeroth.dbc.domain.VehicleEntry;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnInstall;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnRemovePassenger;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnReset;
import com.github.azeroth.game.listener.interfaces.ivehicle.IVehicleOnUninstall;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Vehicle implements TransportObject {
    //C# TO JAVA CONVERTER TODO TASK: The following operator overload is not converted by C# to Java Converter:
    public static implicit operator
    private final Unit me;
    private final VehicleEntry vehicleInfo; //< DBC data for vehicle
    
//ORIGINAL LINE: readonly uint _creatureEntry;
    private final int creatureEntry; //< Can be different than the entry of _me in case of players
    private final ArrayList<VehicleJoinEvent> pendingJoinEvents = new ArrayList<VehicleJoinEvent>();
    public HashMap<Byte, VehicleSeat> seats = new HashMap<Byte, VehicleSeat>();
    
//ORIGINAL LINE: public uint UsableSeatNum;
    public int usableSeatNum; //< Number of seats that match VehicleSeatEntry.UsableByPlayer, used for proper display flags
    private Status status = Status.values()[0]; //< Internal variable for sanity checks

    
//ORIGINAL LINE: public Vehicle(Unit unit, VehicleRecord vehInfo, uint creatureEntry)
    public Vehicle(Unit unit, VehicleEntry vehInfo, int creatureEntry) {
        me = unit;
        vehicleInfo = vehInfo;
        this.creatureEntry = creatureEntry;
        status = Status.None;


//ORIGINAL LINE: for (uint i = 0; i < SharedConst.MaxVehicleSeats; ++i)
        for (int i = 0; i < SharedConst.MaxVehicleSeats; ++i) {

//ORIGINAL LINE: uint seatId = _vehicleInfo.SeatID[i];
            int seatId = vehicleInfo.seatID[i];

            if (seatId != 0) {
                var veSeat = CliDB.vehicleSeatStorage.get(seatId);

                if (veSeat != null) {
                    var addon = Global.getObjectMgr().getVehicleSeatAddon(seatId);
                    seats.put((byte) i, new VehicleSeat(veSeat, addon));

                    if (veSeat.CanEnterOrExit()) {
                        ++usableSeatNum;
                    }
                }
            }
        }

        // Set or remove correct flags based on available seats. Will overwrite db data (if wrong).
        if (usableSeatNum != 0) {
            me.setNpcFlag(me.isTypeId(TypeId.PLAYER) ? NPCFlags.PlayerVehicle : NPCFlags.SpellClick);
        } else {
            me.removeNpcFlag(me.isTypeId(TypeId.PLAYER) ? NPCFlags.PlayerVehicle : NPCFlags.SpellClick);
        }

        initMovementInfoForBase();
    }

    boolean(Vehicle vehicle) {
        return vehicle != null;
    }

    public final TransportObject removePassenger(WorldObject passenger) {
        var unit = passenger.toUnit();

        if (unit == null) {
            return null;
        }

        if (unit.getVehicle1() != this) {
            return null;
        }

        var seat = getSeatKeyValuePairForPassenger(unit);

        Log.outDebug(LogFilter.Vehicle, "Unit {0} exit vehicle entry {1} id {2} dbguid {3} seat {4}", unit.getName(), me.getEntry(), vehicleInfo.id, me.getGUID().toString(), seat.getKey());

        if (seat.getValue().SeatInfo.CanEnterOrExit() && ++usableSeatNum != 0) {
            me.setNpcFlag(me.isTypeId(TypeId.PLAYER) ? NPCFlags.PlayerVehicle : NPCFlags.SpellClick);
        }

        // Enable gravity for passenger when he did not have it active before entering the vehicle
        if (seat.getValue().SeatInfo.HasFlag(VehicleSeatFlags.DisableGravity) && !seat.getValue().Passenger.IsGravityDisabled) {
            unit.setDisableGravity(false);
        }

        // Remove UNIT_FLAG_NOT_SELECTABLE if passenger did not have it before entering vehicle
        if (seat.getValue().SeatInfo.HasFlag(VehicleSeatFlags.PassengerNotSelectable) && !seat.getValue().Passenger.IsUninteractible) {
            unit.removeUnitFlag(UnitFlags.Uninteractible);
        }

        seat.getValue().Passenger.Reset();

        if (me.isTypeId(TypeId.Unit) && unit.isTypeId(TypeId.PLAYER) && seat.getValue().SeatInfo.HasFlag(VehicleSeatFlags.CanControl)) {
            me.removeCharmedBy(unit);
        }

        if (me.isInWorld) {
            unit.movementInfo.resetTransport();
        }

        // only for flyable vehicles
        if (unit.isFlying()) {
            me.CastSpell(unit, SharedConst.VehicleSpellParachute, true);
        }

        if (me.isTypeId(TypeId.Unit) && me.toCreature().isAIEnabled()) {
            me.toCreature().getAi().passengerBoarded(unit, seat.getKey(), false);
        }

        if (getBase().isTypeId(TypeId.Unit)) {
            Global.getScriptMgr().<IVehicleOnRemovePassenger>runScript(p -> p.OnRemovePassenger(this, unit), getBase().toCreature().getScriptId());
        }

        unit.setVehicle1(null);

        return this;
    }

    public final ObjectGuid getTransportGUID() {
        return getBase().getGUID().clone();
    }

    public final float getTransportOrientation() {
        return getBase().location.getOrientation();
    }

    public final void addPassenger(WorldObject passenger) {
        Log.outFatal(LogFilter.Vehicle, "Vehicle cannot directly gain passengers without auras");
    }

    public final void calculatePassengerPosition(Position pos) {
        itransport.calculatePassengerPosition(pos, getBase().location.x, getBase().location.y, getBase().location.z, getBase().location.getOrientation());
    }

    public final void calculatePassengerOffset(Position pos) {
        itransport.calculatePassengerOffset(pos, getBase().location.x, getBase().location.y, getBase().location.z, getBase().location.getOrientation());
    }

    public final int getMapIdForSpawning() {
        return (int) getBase().location.mapId;
    }

    public final void install() {
        status = Status.Installed;

        if (getBase().isTypeId(TypeId.Unit)) {
            Global.getScriptMgr().<IVehicleOnInstall>runScript(p -> p.OnInstall(this), getBase().toCreature().getScriptId());
        }
    }

    public final void installAllAccessories(boolean evading) {
        if (getBase().isTypeId(TypeId.PLAYER) || !evading) {
            removeAllPassengers(); // We might have aura's saved in the DB with now invalid casters - remove
        }

        var accessories = getBase().getWorldContext().getObjectManager().getVehicleAccessoryList(this);

        if (accessories == null) {
            return;
        }

        for (var acc : accessories) {
            if (!evading || acc.isMinion()) { // only install minions on evade mode
                installAccessory(acc.getAccessoryEntry(), acc.getSeatId(), acc.isMinion(), acc.getSummonType(), acc.getSummonTimer());
            }
        }
    }

    public final void uninstall() {
        // @Prevent recursive uninstall call. (Bad script in OnUninstall/OnRemovePassenger/PassengerBoarded hook.)
        if (status == Status.UnInstalling && !getBase().hasUnitTypeMask(UnitTypeMask.Minion)) {
            Log.outError(LogFilter.Vehicle, "Vehicle GuidLow: {0}, Entry: {1} attempts to uninstall, but already has STATUS_UNINSTALLING! " + "Check Uninstall/PassengerBoarded script hooks for errors.", me.getGUID().toString(), me.getEntry());

            return;
        }

        status = Status.UnInstalling;
        Log.outDebug(LogFilter.Vehicle, "Vehicle.Uninstall Entry: {0}, GuidLow: {1}", creatureEntry, me.getGUID().toString());
        removeAllPassengers();

        if (getBase().isTypeId(TypeId.Unit)) {
            Global.getScriptMgr().<IVehicleOnUninstall>runScript(p -> p.OnUninstall(this), getBase().toCreature().getScriptId());
        }
    }

    public final void reset() {
        reset(false);
    }

    
//ORIGINAL LINE: public void Reset(bool evading = false)
    public final void reset(boolean evading) {
        if (!getBase().isTypeId(TypeId.Unit)) {
            return;
        }

        Log.outDebug(LogFilter.Vehicle, "Vehicle.Reset (Entry: {0}, GuidLow: {1}, DBGuid: {2})", getCreatureEntry(), me.getGUID().toString(), me.toCreature().spawnId);

        applyAllImmunities();

        if (getBase().isAlive()) {
            installAllAccessories(evading);
        }

        Global.getScriptMgr().<IVehicleOnReset>runScript(p -> p.OnReset(this), getBase().toCreature().getScriptId());
    }

    public final void removeAllPassengers() {
        Log.outDebug(LogFilter.Vehicle, "Vehicle.RemoveAllPassengers. Entry: {0}, GuidLow: {1}", creatureEntry, me.getGUID().toString());

        // Setting to_Abort to true will cause @VehicleJoinEvent.Abort to be executed on next @Unit.UpdateEvents call
        // This will properly "reset" the pending join process for the passenger.
        {
            // Update vehicle in every pending join event - Abort may be called after vehicle is deleted
            var eventVehicle = status != Status.UnInstalling ? this : null;

            while (!pendingJoinEvents.Empty()) {
                var e = pendingJoinEvents.get(0);
                e.ScheduleAbort();
                e.Target = eventVehicle;
                pendingJoinEvents.remove(pendingJoinEvents.get(0));
            }
        }

        // Passengers always cast an aura with SPELL_AURA_CONTROL_VEHICLE on the vehicle
        // We just remove the aura and the unapply handler will make the target leave the vehicle.
        // We don't need to iterate over Seats
        me.removeAurasByType(AuraType.ControlVehicle);
    }

    public final boolean hasEmptySeat(byte seatId) {
        var seat = seats.get(seatId);

        if (seat == null) {
            return false;
        }

        return seat.IsEmpty();
    }

    public final Unit getPassenger(byte seatId) {
        var seat = seats.get(seatId);

        if (seat == null) {
            return null;
        }

        return Global.getObjAccessor().getUnit(getBase(), seat.Passenger.Guid);
    }

    public final VehicleSeat getNextEmptySeat(byte seatId, boolean next) {
        var seat = seats.get(seatId);

        if (seat == null) {
            return null;
        }

        var newSeatId = seatId;

        while (!seat.IsEmpty() || hasPendingEventForSeat(newSeatId) || (!seat.SeatInfo.CanEnterOrExit() && !seat.SeatInfo.IsUsableByOverride())) {
            if (next) {
                if (!seats.containsKey(++newSeatId)) {
                    newSeatId = 0;
                }
            } else {
                if (!seats.containsKey(newSeatId)) {
                    newSeatId = SharedConst.MaxVehicleSeats;
                }

                --newSeatId;
            }

            // Make sure we don't loop indefinetly
            if (newSeatId == seatId) {
                return null;
            }

            seat = seats.get(newSeatId);
        }

        return seat;
    }

    /**
     * Gets the vehicle seat addon data for the seat of a passenger
     *
     * @param passenger Identifier for the current seat user
     * @return The seat addon data for the currently used seat of a passenger
     */
    public final VehicleSeatAddon getSeatAddonForSeatOfPassenger(Unit passenger) {
        for (var pair : seats.entrySet()) {
            if (!pair.getValue().IsEmpty() && game.entities.Objects.equals(pair.getValue().Passenger.Guid, passenger.getGUID().clone())) {
                return pair.getValue().SeatAddon;
            }
        }

        return null;
    }

    public final boolean addVehiclePassenger(Unit unit, byte seatId) {
        // @Prevent adding passengers when vehicle is uninstalling. (Bad script in OnUninstall/OnRemovePassenger/PassengerBoarded hook.)
        if (status == Status.UnInstalling) {
            Log.outError(LogFilter.Vehicle, "Passenger GuidLow: {0}, Entry: {1}, attempting to board vehicle GuidLow: {2}, Entry: {3} during uninstall! SeatId: {4}", unit.getGUID().toString(), unit.getEntry(), me.getGUID().toString(), me.getEntry(), seatId);

            return false;
        }

        Log.outDebug(LogFilter.Vehicle, "Unit {0} scheduling enter vehicle (entry: {1}, vehicleId: {2}, guid: {3} (dbguid: {4}) on seat {5}", unit.getName(), me.getEntry(), vehicleInfo.id, me.getGUID().toString(), (me.isTypeId(TypeId.Unit) ? me.toCreature().spawnId : 0), seatId);

        // The seat selection code may kick other passengers off the vehicle.
        // While the validity of the following may be arguable, it is possible that when such a passenger
        // exits the vehicle will dismiss. That's why the actual adding the passenger to the vehicle is scheduled
        // asynchronously, so it can be cancelled easily in case the vehicle is uninstalled meanwhile.
        VehicleJoinEvent e = new VehicleJoinEvent(this, unit);
        unit.events.AddEvent(e, unit.events.CalculateTime(TimeSpan.Zero));

        Map.Entry<Byte, VehicleSeat> seat = new KeyValuePair<Byte, VehicleSeat>();

        if (seatId < 0) { // no specific seat requirement
            for (var _seat : seats.entrySet()) {
                seat = _seat;

                if (seat.getValue().IsEmpty() && !hasPendingEventForSeat(seat.getKey()) && (_seat.getValue().SeatInfo.CanEnterOrExit() || _seat.getValue().SeatInfo.IsUsableByOverride())) {
                    break;
                }
            }

            if (seat.getValue() == null) { // no available seat
                e.ScheduleAbort();

                return false;
            }

            e.seat = seat;
            pendingJoinEvents.add(e);
        } else {
            seat = new KeyValuePair<Byte, VehicleSeat>(seatId, seats.get(seatId));

            if (seat.getValue() == null) {
                e.ScheduleAbort();

                return false;
            }

            e.seat = seat;
            pendingJoinEvents.add(e);

            if (!seat.getValue().IsEmpty()) {
                var passenger = Global.getObjAccessor().getUnit(getBase(), seat.getValue().Passenger.Guid);
                passenger.exitVehicle();
            }
        }

        return true;
    }

    public final void relocatePassengers() {
        ArrayList<Tuple<Unit, Position>> seatRelocation = new ArrayList<Tuple<Unit, Position>>();

        // not sure that absolute position calculation is correct, it must depend on vehicle pitch angle
        for (var pair : seats.entrySet()) {
            var passenger = Global.getObjAccessor().getUnit(getBase(), pair.getValue().Passenger.Guid);

            if (passenger != null) {
                var pos = passenger.movementInfo.transport.pos.copy();
                calculatePassengerPosition(pos);

                seatRelocation.add(Tuple.Create(passenger, pos));
            }
        }

        for (var(passenger, position) : seatRelocation) {
            itransport.updatePassengerPosition(this, me.getMap(), passenger, position, false);
        }
    }

    public final boolean isVehicleInUse() {
        for (var pair : seats.entrySet()) {
            if (!pair.getValue().IsEmpty()) {
                return true;
            }
        }

        return false;
    }

    public final boolean isControllableVehicle() {
        for (var itr : seats.entrySet()) {
            if (itr.getValue().SeatInfo.HasFlag(VehicleSeatFlags.CanControl)) {
                return true;
            }
        }

        return false;
    }

    public final VehicleSeatRecord getSeatForPassenger(Unit passenger) {
        for (var pair : seats.entrySet()) {
            if (game.entities.Objects.equals(pair.getValue().Passenger.Guid, passenger.getGUID().clone())) {
                return pair.getValue().SeatInfo;
            }
        }

        return null;
    }

    
//ORIGINAL LINE: public byte GetAvailableSeatCount()
    public final byte getAvailableSeatCount() {

//ORIGINAL LINE: byte ret = 0;
        byte ret = 0;

        for (var pair : seats.entrySet()) {
            if (pair.getValue().IsEmpty() && !hasPendingEventForSeat(pair.getKey()) && (pair.getValue().SeatInfo.CanEnterOrExit() || pair.getValue().SeatInfo.IsUsableByOverride())) {
                ++ret;
            }
        }

        return ret;
    }

    public final void removePendingEvent(VehicleJoinEvent e) {
        for (var event : pendingJoinEvents) {
            if (event == e) {
                pendingJoinEvents.remove(event);

                break;
            }
        }
    }

    public final void removePendingEventsForSeat(byte seatId) {
        for (var i = 0; i < pendingJoinEvents.size(); ++i) {
            var joinEvent = pendingJoinEvents.get(i);

            if (joinEvent.seat.getKey() == seatId) {
                joinEvent.ScheduleAbort();
                pendingJoinEvents.remove(joinEvent);
            }
        }
    }

    public final void removePendingEventsForPassenger(Unit passenger) {
        for (var i = 0; i < pendingJoinEvents.size(); ++i) {
            var joinEvent = pendingJoinEvents.get(i);

            if (joinEvent.passenger == passenger) {
                joinEvent.ScheduleAbort();
                pendingJoinEvents.remove(joinEvent);
            }
        }
    }

    public final Duration getDespawnDelay() {

        Unit base = getBase();

        var vehicleTemplate = base.getWorldContext().getObjectManager().getVehicleTemplate(this);

        if (vehicleTemplate != null) {
            return vehicleTemplate.despawnDelay;
        }

        return Duration.ofMillis(1);
    }

    public final String getDebugInfo() {
        var str = new StringBuilder("Vehicle seats:\n");

        for (var(id, seat) : seats) {
            str.append(String.format("seat %1$s: %2$s\n", id, (seat.IsEmpty() ? "empty" : seat.Passenger.Guid)));
        }

        str.append("Vehicle pending events:");

        if (pendingJoinEvents.Empty()) {
            str.append(" none");
        } else {
            str.append("\n");

            for (var joinEvent : pendingJoinEvents) {
                str.append(String.format("seat %1$s: %2$s\n", joinEvent.seat.getKey(), joinEvent.passenger.getGUID().clone()));
            }
        }

        return str.toString();
    }

    public final Unit getBase() {
        return me;
    }

    public final VehicleEntry getVehicleInfo() {
        return vehicleInfo;
    }

    
//ORIGINAL LINE: public uint GetCreatureEntry()
    public final int getCreatureEntry() {
        return creatureEntry;
    }

    private void applyAllImmunities() {
        // This couldn't be done in DB, because some spells have MECHANIC_NONE

        // Vehicles should be immune on Knockback ...
        me.ApplySpellImmune(0, SpellImmunity.Effect, SpellEffectName.KnockBack, true);
        me.ApplySpellImmune(0, SpellImmunity.Effect, SpellEffectName.KnockBackDest, true);

        // Mechanical units & vehicles ( which are not Bosses, they have own immunities in DB ) should be also immune on healing ( exceptions in switch below )
        if (me.isTypeId(TypeId.Unit) && me.toCreature().getCreatureTemplate().type == CreatureType.Mechanical && !me.toCreature().isWorldBoss()) {
            // Heal & dispel ...
            me.ApplySpellImmune(0, SpellImmunity.Effect, SpellEffectName.Heal, true);
            me.ApplySpellImmune(0, SpellImmunity.Effect, SpellEffectName.HealPct, true);
            me.ApplySpellImmune(0, SpellImmunity.Effect, SpellEffectName.Dispel, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.PeriodicHeal, true);

            // ... Shield & Immunity grant spells ...
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.SchoolImmunity, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModUnattackable, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.SchoolAbsorb, true);

//ORIGINAL LINE: _me.ApplySpellImmune(0, SpellImmunity.Mechanic, (uint)Mechanics.Banish, true);
            me.ApplySpellImmune(0, SpellImmunity.Mechanic, (int) Mechanics.Banish.getValue(), true);

//ORIGINAL LINE: _me.ApplySpellImmune(0, SpellImmunity.Mechanic, (uint)Mechanics.Shield, true);
            me.ApplySpellImmune(0, SpellImmunity.Mechanic, (int) Mechanics.Shield.getValue(), true);

//ORIGINAL LINE: _me.ApplySpellImmune(0, SpellImmunity.Mechanic, (uint)Mechanics.ImmuneShield, true);
            me.ApplySpellImmune(0, SpellImmunity.Mechanic, (int) Mechanics.ImmuneShield.getValue(), true);

            // ... Resistance, Split damage, Change stats ...
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.DamageShield, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.SplitDamagePct, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModResistance, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModStat, true);
            me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModDamagePercentTaken, true);
        }

        // Different immunities for vehicles goes below
        switch (getVehicleInfo().id) {
            // code below prevents a bug with movable cannons
            case 160: // Strand of the Ancients
            case 244: // Wintergrasp
            case 452: // Isle of Conquest
            case 510: // Isle of Conquest
            case 543: // Isle of Conquest
                me.setControlled(true, UnitState.Root);
                // why we need to apply this? we can simple add immunities to slow mechanic in DB
                me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModDecreaseSpeed, true);

                break;
            case 335: // Salvaged Chopper
            case 336: // Salvaged Siege Engine
            case 338: // Salvaged Demolisher
                me.ApplySpellImmune(0, SpellImmunity.State, AuraType.ModDamagePercentTaken, false); // Battering Ram

                break;
            default:
                break;
        }
    }

    
//ORIGINAL LINE: void InstallAccessory(uint entry, sbyte seatId, bool minion, byte type, uint summonTime)
    private void installAccessory(int entry, byte seatId, boolean minion, byte type, int summonTime) {
        // @Prevent adding accessories when vehicle is uninstalling. (Bad script in OnUninstall/OnRemovePassenger/PassengerBoarded hook.)

        if (status == Status.UnInstalling) {
            Log.outError(LogFilter.Vehicle, "Vehicle ({0}, Entry: {1}) attempts to install accessory (Entry: {2}) on seat {3} with STATUS_UNINSTALLING! " + "Check Uninstall/PassengerBoarded script hooks for errors.", me.getGUID().toString(), getCreatureEntry(), entry, seatId);

            return;
        }

        Log.outDebug(LogFilter.Vehicle, "Vehicle ({0}, Entry {1}): installing accessory (Entry: {2}) on seat: {3}", me.getGUID().toString(), getCreatureEntry(), entry, seatId);

        var accessory = me.SummonCreature(entry, me.location, TempSummonType.forValue(type), TimeSpan.FromMilliseconds(summonTime));

        if (minion) {
            accessory.addUnitTypeMask(UnitTypeMask.Accessory);
        }

        me.handleSpellClick(accessory, seatId);

        // If for some reason adding accessory to vehicle fails it will unsummon in
        // @VehicleJoinEvent.Abort
    }

    private void initMovementInfoForBase() {
        var vehicleFlags = VehicleFlags.forValue(getVehicleInfo().flags);

        if (vehicleFlags.hasFlag(VehicleFlags.NoStrafe)) {
            me.addUnitMovementFlag2(MovementFlag2.NoStrafe);
        }

        if (vehicleFlags.hasFlag(VehicleFlags.NoJumping)) {
            me.addUnitMovementFlag2(MovementFlag2.NoJumping);
        }

        if (vehicleFlags.hasFlag(VehicleFlags.Fullspeedturning)) {
            me.addUnitMovementFlag2(MovementFlag2.FullSpeedTurning);
        }

        if (vehicleFlags.hasFlag(VehicleFlags.AllowPitching)) {
            me.addUnitMovementFlag2(MovementFlag2.AlwaysAllowPitching);
        }

        if (vehicleFlags.hasFlag(VehicleFlags.Fullspeedpitching)) {
            me.addUnitMovementFlag2(MovementFlag2.FullSpeedPitching);
        }
    }

    private Map.Entry<Byte, VehicleSeat> getSeatKeyValuePairForPassenger(Unit passenger) {
        for (var pair : seats.entrySet()) {
            if (game.entities.Objects.equals(pair.getValue().Passenger.Guid, passenger.getGUID().clone())) {
                return pair;
            }
        }

        return seats.Last();
    }

    private boolean hasPendingEventForSeat(byte seatId) {
        for (var i = 0; i < pendingJoinEvents.size(); ++i) {
            var joinEvent = pendingJoinEvents.get(i);

            if (joinEvent.seat.getKey() == seatId) {
                return true;
            }
        }

        return false;
    }

    public enum Status {
        None,
        Installed,
        UnInstalling;

        public static final int SIZE = Integer.SIZE;

        public static Status forValue(int value) {
            return values()[value];
        }

        public int getValue() {
            return this.ordinal();
        }
    }
}