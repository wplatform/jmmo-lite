package com.github.azeroth.game.entity.gobject;


import com.github.azeroth.defines.GameObjectDynamicLowFlag;
import com.github.azeroth.game.domain.gobject.GameObjectData;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.transport.TransportTemplate;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.object.update.UpdateData;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.entity.vehicle.ITransport;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.phasing.PhasingHandler;
import com.github.azeroth.game.listener.interfaces.itransport.*;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
public class Transport extends GameObject implements ITransport {
    private final TimeTracker positionChangeTimer = new TimeTracker();
    private final HashSet<WorldObject> passengers = new HashSet<WorldObject>();
    private final HashSet<WorldObject> staticPassengers = new HashSet<WorldObject>();

    private TransportTemplate transportInfo;

    private TransportMovementState movementState = TransportMovementState.values()[0];
    private BitSet eventsToTrigger;
    private int currentPathLeg;
    private Integer nextStopTimestamp = null;
    private int pathProgress;

    private boolean delayedAddModel;

    public transport() {
        updateFlag.serverTime = true;
        updateFlag.stationary = true;
        updateFlag.rotation = true;
    }

    public final void addPassenger(WorldObject passenger) {
        if (!isInWorld()) {
            return;
        }

        synchronized (staticPassengers) {
            if (passengers.add(passenger)) {
                passenger.setTransport(this);
                passenger.getMovementInfo().transport.guid = getGUID();

                var player = passenger.toPlayer();

                if (player) {
                    global.getScriptMgr().<ITransportOnAddPassenger>RunScript(p -> p.OnAddPassenger(this, player), getScriptId());
                }
            }
        }
    }

    public final ITransport removePassenger(WorldObject passenger) {
        synchronized (staticPassengers) {
            if (passengers.remove(passenger) || staticPassengers.remove(passenger)) // static passenger can remove itself in case of grid unload
            {
                passenger.setTransport(null);
                passenger.getMovementInfo().transport.reset();
                Log.outDebug(LogFilter.transport, "Object {0} removed from transport {1}.", passenger.getName(), getName());

                var plr = passenger.toPlayer();

                if (plr != null) {
                    global.getScriptMgr().<ITransportOnRemovePassenger>RunScript(p -> p.OnRemovePassenger(this, plr), getScriptId());
                    plr.setFallInformation(0, plr.getLocation().getZ());
                }
            }
        }

        return this;
    }

    public final void calculatePassengerPosition(Position pos) {
        itransport.calculatePassengerPosition(pos, getLocation().getX(), getLocation().getY(), getLocation().getZ(), getTransportOrientation());
    }

    public final void calculatePassengerOffset(Position pos) {
        itransport.calculatePassengerOffset(pos, getLocation().getX(), getLocation().getY(), getLocation().getZ(), getTransportOrientation());
    }

    public final int getMapIdForSpawning() {
        return getTemplate().moTransport.spawnMap;
    }

    public final ObjectGuid getTransportGUID() {
        return getGUID();
    }

    public final float getTransportOrientation() {
        return getLocation().getO();
    }

    @Override
    public void close() throws IOException {
        unloadStaticPassengers();
        super.close();
    }

    public final boolean create(long guidlow, int entry, float x, float y, float z, float ang) {
        getLocation().relocate(x, y, z, ang);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.transport, String.format("Transport (GUID: %1$s) not created. Suggested coordinates isn't valid (X: %2$s Y: %3$s)", guidlow, x, y));

            return false;
        }

        create(ObjectGuid.create(HighGuid.Transport, guidlow));

        var goinfo = global.getObjectMgr().getGameObjectTemplate(entry);

        if (goinfo == null) {
            Logs.SQL.error(String.format("Transport not created: entry in `gameobject_template` not found, entry: %1$s", entry));

            return false;
        }

        goInfo = goinfo;
        goTemplateAddonProtected = global.getObjectMgr().getGameObjectTemplateAddon(entry);

        var tInfo = global.getTransportMgr().getTransportTemplate(entry);

        if (tInfo == null) {
            Logs.SQL.error("Transport {0} (name: {1}) will not be created, missing `transport_template` entry.", entry, goinfo.name);

            return false;
        }

        transportInfo = tInfo;
        eventsToTrigger = new bitSet(tInfo.getEvents().size(), true);

        var goOverride = getGameObjectOverride();

        if (goOverride != null) {
            setFaction(goOverride.faction);
            replaceAllFlags(goOverride.flags);
        }

        pathProgress = goinfo.moTransport.allowstopping == 0 ? time.MSTime % tInfo.getTotalPathTime() : 0;
        setPathProgressForClient((float) _pathProgress / (float) tInfo.getTotalPathTime());
        setObjectScale(goinfo.size);
        setPeriod(tInfo.getTotalPathTime());
        setEntry(goinfo.entry);
        setDisplayId(goinfo.displayId);
        setGoState(goinfo.moTransport.allowstopping == 0 ? GOState.Ready : GOState.active);
        setGoType(GameObjectTypes.MapObjTransport);
        setGoAnimProgress(255);
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().spawnTrackingStateAnimID), global.getDB2Mgr().GetEmptyAnimStateID());
        setName(goinfo.name);
        setLocalRotation(0.0f, 0.0f, 0.0f, 1.0f);
        setParentRotation(Quaternion.Identity);

        tangible.OutObject<TransportMovementState> tempOut__ = new tangible.OutObject<TransportMovementState>();
        int legIndex;
        tangible.OutObject<Integer> tempOut_legIndex = new tangible.OutObject<Integer>();
        var position = transportInfo.computePosition(pathProgress, tempOut__, tempOut_legIndex);
        legIndex = tempOut_legIndex.outArgValue;
        _ = tempOut__.outArgValue;

        if (position != null) {
            getLocation().relocate(position.getX(), position.getY(), position.getZ(), position.getO());
            currentPathLeg = legIndex;
        }

        createModel();

        return true;
    }

    @Override
    public void cleanupsBeforeDelete(boolean finalCleanup) {
        unloadStaticPassengers();

        while (!passengers.isEmpty()) {
            var obj = passengers.FirstOrDefault();
            removePassenger(obj);
        }

        super.cleanupsBeforeDelete(finalCleanup);
    }

    @Override
    public void update(int diff) {
        var positionUpdateDelay = duration.ofSeconds(200);

        if (getAI() != null) {
            getAI().updateAI(diff);
        } else if (!AIM_Initialize()) {
            Log.outError(LogFilter.transport, "Could not initialize GameObjectAI for Transport");
        }

        global.getScriptMgr().<ITransportOnUpdate>RunScript(p -> p.onUpdate(this, diff), getScriptId());

        positionChangeTimer.update(diff);

        var cycleId = _pathProgress / getTransportPeriod();

        if (getTemplate().moTransport.allowstopping == 0) {
            pathProgress = gameTime.GetGameTimeMS();
        }

        else if (nextStopTimestamp == null || nextStopTimestamp > pathProgress + diff) {
            pathProgress += diff;
        } else {
            pathProgress = nextStopTimestamp.intValue();
        }

        if (_pathProgress / getTransportPeriod() != cycleId) {
            // reset cycle
            eventsToTrigger.set(0, eventsToTrigger.size(), true);
        }

        setPathProgressForClient((float) _pathProgress / (float) getTransportPeriod());

        var timer = _pathProgress % getTransportPeriod();

        var eventToTriggerIndex = -1;

        for (var i = 0; i < eventsToTrigger.size(); i++) {
            if (eventsToTrigger.get(i)) {
                eventToTriggerIndex = i;

                break;
            }
        }

        if (eventToTriggerIndex != -1) {
            while (eventToTriggerIndex < transportInfo.getEvents().size() && transportInfo.getEvents().get(eventToTriggerIndex).timestamp < timer) {
                var leg = transportInfo.getLegForTime(transportInfo.getEvents().get(eventToTriggerIndex).timestamp);

                if (leg != null) {
                    if (leg.getMapId() == getLocation().getMapId()) {
                        GameEvents.trigger(transportInfo.getEvents().get(eventToTriggerIndex).eventId, this, this);
                    }
                }

                eventsToTrigger.set(eventToTriggerIndex, false);
                ++eventToTriggerIndex;
            }
        }

        TransportMovementState moveState;
        tangible.OutObject<TransportMovementState> tempOut_moveState = new tangible.OutObject<TransportMovementState>();
        int legIndex;
        tangible.OutObject<Integer> tempOut_legIndex = new tangible.OutObject<Integer>();
        var newPosition = transportInfo.computePosition(timer, tempOut_moveState, tempOut_legIndex);
        legIndex = tempOut_legIndex.outArgValue;
        moveState = tempOut_moveState.outArgValue;

        if (newPosition != null) {
            var justStopped = movementState == TransportMovementState.Moving && moveState != TransportMovementState.Moving;
            movementState = moveState;

            if (justStopped) {
                if (!nextStopTimestamp.equals(0) && getGoState() != GOState.Ready) {
                    setGoState(GOState.Ready);
                    setDynamicFlag(GameObjectDynamicLowFlag.Stopped);
                }
            }

            if (legIndex != currentPathLeg) {
                var oldMapId = transportInfo.getPathLegs().get(currentPathLeg).getMapId();
                currentPathLeg = legIndex;
                teleportTransport(oldMapId, transportInfo.getPathLegs().get(legIndex).getMapId(), newPosition.getX(), newPosition.getY(), newPosition.getZ(), newPosition.getO());

                return;
            }

            // set position
            if (positionChangeTimer.Passed && getExpectedMapId() == getLocation().getMapId()) {
                positionChangeTimer.reset(positionUpdateDelay);

                if (movementState == TransportMovementState.Moving || justStopped) {
                    updatePosition(newPosition.getX(), newPosition.getY(), newPosition.getZ(), newPosition.getO());
                } else {
					/* There are four possible scenarios that trigger loading/unloading passengers:
					  1. transport moves from inactive to active grid
					  2. the grid that transport is currently in becomes active
					  3. transport moves from active to inactive grid
					  4. the grid that transport is currently in unloads
					*/
                    var gridActive = getMap().isGridLoaded(getLocation().getX(), getLocation().getY());

                    synchronized (staticPassengers) {
                        if (staticPassengers.isEmpty() && gridActive) // 2.
                        {
                            loadStaticPassengers();
                        } else if (!staticPassengers.isEmpty() && !gridActive) {
                            // 4. - if transports stopped on grid edge, some passengers can remain in active grids
                            //      unload all static passengers otherwise passengers won't load correctly when the grid that transport is currently in becomes active
                            unloadStaticPassengers();
                        }
                    }
                }
            }
        }

        // Add model to map after we are fully done with moving maps
        if (delayedAddModel) {
            delayedAddModel = false;

            if (getModel() != null) {
                getMap().insertGameObjectModel(getModel());
            }
        }
    }

    public final Creature createNPCPassenger(long guid, CreatureData data) {
        var map = getMap();

        if (map.getCreatureRespawnTime(guid) != 0) {
            return null;
        }

        var creature = CREATURE.createCreatureFromDB(guid, map, false, true);

        if (!creature) {
            return null;
        }

        var spawn = data.spawnPoint.Copy();

        creature.setTransport(this);
        creature.getMovementInfo().transport.guid = getGUID();
        creature.getMovementInfo().transport.pos.relocate(spawn);
        creature.getMovementInfo().transport.seat = -1;
        calculatePassengerPosition(spawn);
        creature.getLocation().relocate(spawn);
        creature.setHomePosition(creature.getLocation().getX(), creature.getLocation().getY(), creature.getLocation().getZ(), creature.getLocation().getO());
        creature.setTransportHomePosition(creature.getMovementInfo().transport.pos);

        // @HACK - transport models are not added to map's dynamic LoS calculations
        //         because the current GameObjectModel cannot be moved without recreating
        creature.addUnitState(UnitState.IgnorePathfinding);

        if (!creature.getLocation().isPositionValid()) {
            Log.outError(LogFilter.transport, "Creature (guidlow {0}, entry {1}) not created. Suggested coordinates aren't valid (X: {2} Y: {3})", creature.getGUID().toString(), creature.getEntry(), creature.getLocation().getX(), creature.getLocation().getY());

            return null;
        }

        PhasingHandler.initDbPhaseShift(creature.getPhaseShift(), data.phaseUseFlags, data.phaseId, data.phaseGroup);
        PhasingHandler.initDbVisibleMapId(creature.getPhaseShift(), data.terrainSwapMap);

        if (!map.addToMap(creature)) {
            return null;
        }

        synchronized (staticPassengers) {
            staticPassengers.add(creature);
        }

        global.getScriptMgr().<ITransportOnAddCreaturePassenger>RunScript(p -> p.OnAddCreaturePassenger(this, creature), getScriptId());

        return creature;
    }


    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType, SummonPropertiesRecord properties, int duration, Unit summoner, int spellId) {
        return summonPassenger(entry, pos, summonType, properties, duration, summoner, spellId, 0);
    }

    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType, SummonPropertiesRecord properties, int duration, Unit summoner) {
        return summonPassenger(entry, pos, summonType, properties, duration, summoner, 0, 0);
    }

    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType, SummonPropertiesRecord properties, int duration) {
        return summonPassenger(entry, pos, summonType, properties, duration, null, 0, 0);
    }

    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType, SummonPropertiesRecord properties) {
        return summonPassenger(entry, pos, summonType, properties, 0, null, 0, 0);
    }

    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType) {
        return summonPassenger(entry, pos, summonType, null, 0, null, 0, 0);
    }

    public final TempSummon summonPassenger(int entry, Position pos, TempSummonType summonType, SummonPropertiesRecord properties, int duration, Unit summoner, int spellId, int vehId) {
        var map = getMap();

        if (map == null) {
            return null;
        }

        var mask = UnitTypeMask.SUMMON;

        if (properties != null) {
            switch (properties.Control) {
                case Pet:
                    mask = UnitTypeMask.Guardian;

                    break;
                case Puppet:
                    mask = UnitTypeMask.Puppet;

                    break;
                case Vehicle:
                    mask = UnitTypeMask.minion;

                    break;
                case Wild:
                case Ally:
                case Unk: {
                    switch (properties.title) {
                        case Minion:
                        case Guardian:
                        case Runeblade:
                            mask = UnitTypeMask.Guardian;

                            break;
                        case Totem:
                        case LightWell:
                            mask = UnitTypeMask.totem;

                            break;
                        case Vehicle:
                        case Mount:
                            mask = UnitTypeMask.SUMMON;

                            break;
                        case Companion:
                            mask = UnitTypeMask.minion;

                            break;
                        default:
                            if (properties.getFlags().hasFlag(SummonPropertiesFlags.JoinSummonerSpawnGroup)) // Mirror Image, Summon Gargoyle
                            {
                                mask = UnitTypeMask.Guardian;
                            }

                            break;
                    }

                    break;
                }
                default:
                    return null;
            }
        }

        TempSummon summon = null;

        switch (mask) {
            case Summon:
                summon = new TempSummon(properties, summoner, false);

                break;
            case Guardian:
                summon = new Guardian(properties, summoner, false);

                break;
            case Puppet:
                summon = new Puppet(properties, summoner);

                break;
            case Totem:
                summon = new totem(properties, summoner);

                break;
            case Minion:
                summon = new minion(properties, summoner, false);

                break;
        }

        var newPos = pos.Copy();
        calculatePassengerPosition(newPos);

        if (!summon.create(map.generateLowGuid(HighGuid.Creature), map, entry, newPos, null, vehId)) {
            return null;
        }

        WorldObject phaseShiftOwner = this;

        if (summoner != null && !(properties != null && properties.getFlags().hasFlag(SummonPropertiesFlags.IgnoreSummonerPhase))) {
            phaseShiftOwner = summoner;
        }

        if (phaseShiftOwner != null) {
            PhasingHandler.inheritPhaseShift(summon, phaseShiftOwner);
        }

        summon.setCreatedBySpell(spellId);

        summon.setTransport(this);
        summon.getMovementInfo().transport.guid = getGUID();
        summon.getMovementInfo().transport.pos.relocate(pos);
        summon.getLocation().relocate(newPos);
        summon.setHomePosition(newPos);
        summon.setTransportHomePosition(pos);

        // @HACK - transport models are not added to map's dynamic LoS calculations
        //         because the current GameObjectModel cannot be moved without recreating
        summon.addUnitState(UnitState.IgnorePathfinding);

        summon.initStats(duration);

        if (!map.addToMap(summon)) {
            return null;
        }

        synchronized (staticPassengers) {
            staticPassengers.add(summon);
        }

        summon.initSummon();
        summon.setTempSummonType(summonType);

        return summon;
    }

    public final void updatePosition(float x, float y, float z, float o) {
        global.getScriptMgr().<ITransportOnRelocate>RunScript(p -> p.OnRelocate(this, getLocation().getMapId(), x, y, z), getScriptId());

        var newActive = getMap().isGridLoaded(x, y);
        Cell oldCell = new Cell(getLocation().getX(), getLocation().getY());

        getLocation().relocate(x, y, z, o);
        getStationaryPosition().setO(o);
        updateModelPosition();

        updatePassengerPositions(passengers);

		/* There are four possible scenarios that trigger loading/unloading passengers:
		1. transport moves from inactive to active grid
		2. the grid that transport is currently in becomes active
		3. transport moves from active to inactive grid
		4. the grid that transport is currently in unloads
		*/
        synchronized (staticPassengers) {
            if (staticPassengers.isEmpty() && newActive) // 1. and 2.
            {
                loadStaticPassengers();
            } else if (!staticPassengers.isEmpty() && !newActive && oldCell.diffGrid(new Cell(getLocation().getX(), getLocation().getY()))) // 3.
            {
                unloadStaticPassengers();
            } else {
                updatePassengerPositions(staticPassengers);
            }
        }
        // 4. is handed by grid unload
    }

    public final void enableMovement(boolean enabled) {
        if (getTemplate().moTransport.allowstopping == 0) {
            return;
        }

        if (!enabled) {
            nextStopTimestamp = (_pathProgress / getTransportPeriod()) * getTransportPeriod() + transportInfo.getNextPauseWaypointTimestamp(pathProgress);
        } else {
            nextStopTimestamp = null;
            setGoState(GOState.active);
            removeDynamicFlag(GameObjectDynamicLowFlag.Stopped);
        }
    }

    public final void setDelayedAddModelToMap() {
        delayedAddModel = true;
    }

    @Override
    public void buildUpdate(HashMap<Player, UpdateData> data_map) {
        var players = getMap().getPlayers();

        if (players.isEmpty()) {
            return;
        }

        for (var playerReference : players) {
            if (playerReference.inSamePhase(this)) {
                buildFieldsUpdate(playerReference, data_map);
            }
        }

        clearUpdateMask(true);
    }

    public final int getExpectedMapId() {
        return transportInfo.getPathLegs().get(currentPathLeg).getMapId();
    }

    public final HashSet<WorldObject> getPassengers() {
        return passengers;
    }

    public final int getTransportPeriod() {
        return getGameObjectFieldData().level;
    }

    public final void setPeriod(int period) {
        setLevel(period);
    }

    public final boolean isStopped() {
        return hasDynamicFlag(GameObjectDynamicLowFlag.GO_DYNFLAG_LO_STOPPED);
    }

    public final int getTimer() {
        return pathProgress;
    }

    private GameObject createGOPassenger(long guid, GameObjectData data) {
        var map = getMap();

        if (map.getGORespawnTime(guid) != 0) {
            return null;
        }

        var go = createGameObjectFromDb(guid, map, false);

        if (!go) {
            return null;
        }

        var spawn = data.spawnPoint.Copy();

        go.setTransport(this);
        go.getMovementInfo().transport.guid = getGUID();
        go.getMovementInfo().transport.pos.relocate(spawn);
        go.getMovementInfo().transport.seat = -1;
        calculatePassengerPosition(spawn);
        go.getLocation().relocate(spawn);
        go.relocateStationaryPosition(spawn);

        if (!go.getLocation().isPositionValid()) {
            Log.outError(LogFilter.transport, "GameObject (guidlow {0}, entry {1}) not created. Suggested coordinates aren't valid (X: {2} Y: {3})", go.getGUID().toString(), go.getEntry(), go.getLocation().getX(), go.getLocation().getY());

            return null;
        }

        PhasingHandler.initDbPhaseShift(go.getPhaseShift(), data.phaseUseFlags, data.phaseId, data.phaseGroup);
        PhasingHandler.initDbVisibleMapId(go.getPhaseShift(), data.terrainSwapMap);

        if (!map.addToMap(go)) {
            return null;
        }

        synchronized (staticPassengers) {
            staticPassengers.add(go);
        }

        return go;
    }

    private void loadStaticPassengers() {
        var mapId = getTemplate().moTransport.spawnMap;
        var cells = global.getObjectMgr().getMapObjectGuids(mapId, getMap().getDifficultyID());

        if (cells == null) {
            return;
        }

        for (var cell : cells.entrySet()) {
            // Creatures on transport
            for (var npc : cell.getValue().creatures) {
                createNPCPassenger(npc, global.getObjectMgr().getCreatureData(npc));
            }

            // GameObjects on transport
            for (var go : cell.getValue().gameobjects) {
                createGOPassenger(go, global.getObjectMgr().getGameObjectData(go));
            }
        }
    }

    private void unloadStaticPassengers() {
        synchronized (staticPassengers) {
            while (!staticPassengers.isEmpty()) {
                var obj = staticPassengers.first();
                obj.addObjectToRemoveList(); // also removes from _staticPassengers
            }
        }
    }

    private boolean teleportTransport(int oldMapId, int newMapId, float x, float y, float z, float o) {
        if (oldMapId != newMapId) {
            unloadStaticPassengers();
            teleportPassengersAndHideTransport(newMapId, x, y, z, o);

            return true;
        } else {
            updatePosition(x, y, z, o);

            // Teleport players, they need to know it
            for (var obj : passengers) {
                if (obj.isTypeId(TypeId.PLAYER)) {
                    // will be relocated in UpdatePosition of the vehicle
                    var veh = obj.AsUnit.VehicleBase;

                    if (veh) {
                        if (veh.transport == this) {
                            continue;
                        }
                    }

                    var pos = obj.MovementInfo.transport.pos.Copy();
                    itransport.calculatePassengerPosition(pos, x, y, z, o);

                    obj.AsUnit.nearTeleportTo(pos);
                }
            }

            return false;
        }
    }

    private void teleportPassengersAndHideTransport(int newMapid, float x, float y, float z, float o) {
        if (newMapid == getLocation().getMapId()) {
            addToWorld();

            for (var player : getMap().getPlayers()) {
                if (player.getTransport() != this && player.inSamePhase(this)) {
                    UpdateData data = new UpdateData(getMap().getId());
                    buildCreateUpdateBlockForPlayer(data, player);
                    player.getVisibleTransports().add(getGUID());
                    com.github.azeroth.game.networking.packet.UpdateObject packet;
                    tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
                    data.buildPacket(tempOut_packet);
                    packet = tempOut_packet.outArgValue;
                    player.sendPacket(packet);
                }
            }
        } else {
            UpdateData data = new UpdateData(getMap().getId());
            buildOutOfRangeUpdateBlock(data);

            com.github.azeroth.game.networking.packet.UpdateObject packet;
            tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet2 = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
            data.buildPacket(tempOut_packet2);
            packet = tempOut_packet2.outArgValue;

            for (var player : getMap().getPlayers()) {
                if (player.getTransport() != this && player.getVisibleTransports().contains(getGUID())) {
                    player.sendPacket(packet);
                    player.getVisibleTransports().remove(getGUID());
                }
            }

            removeFromWorld();
        }

        ArrayList<WorldObject> passengersToTeleport = new ArrayList<WorldObject>(passengers);

        for (var obj : passengersToTeleport) {
            var newPos = obj.getMovementInfo().transport.pos.Copy();
            itransport.calculatePassengerPosition(newPos, x, y, z, o);

            switch (obj.getObjectTypeId()) {
                case Player:
                    if (!obj.toPlayer().teleportTo(newMapid, newPos, TeleportToOptions.NotLeaveTransport)) {
                        removePassenger(obj);
                    }

                    break;
                case DynamicObject:
                case AreaTrigger:
                    obj.addObjectToRemoveList();

                    break;
                default:
                    removePassenger(obj);

                    break;
            }
        }
    }

    private void updatePassengerPositions(HashSet<WorldObject> passengers) {
        for (var passenger : passengers) {
            var pos = passenger.MovementInfo.transport.pos.Copy();
            calculatePassengerPosition(pos);
            itransport.UpdatePassengerPosition(this, getMap(), passenger, pos, true);
        }
    }
}
