package com.github.azeroth.game.phasing;


import com.github.azeroth.dbc.defines.PhaseEntryFlags;
import com.github.azeroth.dbc.defines.PhaseUseFlag;
import com.github.azeroth.game.chat.CommandHandler;
import com.github.azeroth.game.condition.ConditionSourceInfo;
import com.github.azeroth.game.domain.condition.ConditionSourceType;
import com.github.azeroth.game.domain.phasing.PhaseFlag;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.domain.phasing.PhaseShiftFlag;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.map.TerrainInfo;
import com.github.azeroth.game.networking.packet.misc.PhaseShiftChange;
import com.github.azeroth.game.networking.packet.misc.PhaseShiftDataPhase;
import com.github.azeroth.game.networking.packet.party.PartyMemberPhase;
import com.github.azeroth.game.networking.packet.party.PartyMemberPhaseStates;

import java.util.ArrayList;


public class PhasingHandler {
    public static PhaseShift EMPTY_PHASE_SHIFT = new PhaseShift();
    public static PhaseShift ALWAYS_VISIBLE;

    static {
        ALWAYS_VISIBLE = new PhaseShift();
        initDbPhaseShift(ALWAYS_VISIBLE, PhaseUseFlag.ALWAYS_VISIBLE, 0, 0);
    }


    public static PhaseFlag getPhaseFlags(int phaseId) {
        var phase = CliDB.PhaseStorage.get(phaseId);

        if (phase != null) {
            if (phase.flags.hasFlag(PhaseEntryFlags.COSMETIC)) {
                return phaseFlags.COSMETIC;
            }

            if (phase.flags.hasFlag(PhaseEntryFlags.PERSONAL)) {
                return phaseFlags.PERSONAL;
            }
        }

        return PhaseFlag.NONE;
    }

    public static void forAllControlled(Unit unit, tangible.Action1Param<unit> func) {
        for (var i = 0; i < unit.getControlled().size(); ++i) {
            var controlled = unit.getControlled().get(i);

            if (controlled.getObjectTypeId() != TypeId.PLAYER && controlled.getVehicle1() == null) // Player inside nested vehicle should not phase the root vehicle and its accessories (only direct root vehicle control does)
            {
                func.invoke(controlled);
            }
        }

        for (byte i = 0; i < SharedConst.MaxSummonSlot; ++i) {
            if (!unit.getSummonSlot()[i].isEmpty()) {
                var summon = unit.getMap().getCreature(unit.getSummonSlot()[i]);

                if (summon) {
                    func.invoke(summon);
                }
            }
        }

        var vehicle = unit.getVehicleKit();

        if (vehicle != null) {
            for (var seat : vehicle.Seats.entrySet()) {
                var passenger = global.getObjAccessor().GetUnit(unit, seat.getValue().passenger.guid);

                if (passenger != null) {
                    func.invoke(passenger);
                }
            }
        }
    }


    public static void addPhase(WorldObject obj, int phaseId, boolean updateVisibility) {
        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        addPhase(obj, phaseId, obj.getGUID(), updateVisibility, visitor);
    }


    public static void removePhase(WorldObject obj, int phaseId, boolean updateVisibility) {
        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        removePhase(obj, phaseId, updateVisibility, visitor);
    }


    public static void addPhaseGroup(WorldObject obj, int phaseGroupId, boolean updateVisibility) {
        var phasesInGroup = global.getDB2Mgr().GetPhasesForGroup(phaseGroupId);

        if (phasesInGroup.isEmpty()) {
            return;
        }

        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        addPhaseGroup(obj, phasesInGroup, obj.getGUID(), updateVisibility, visitor);
    }


    public static void removePhaseGroup(WorldObject obj, int phaseGroupId, boolean updateVisibility) {
        var phasesInGroup = global.getDB2Mgr().GetPhasesForGroup(phaseGroupId);

        if (phasesInGroup.isEmpty()) {
            return;
        }

        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        removePhaseGroup(obj, phasesInGroup, updateVisibility, visitor);
    }


    public static void addVisibleMapId(WorldObject obj, int visibleMapId) {
        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        addVisibleMapId(obj, visibleMapId, visitor);
    }


    public static void removeVisibleMapId(WorldObject obj, int visibleMapId) {
        ControlledUnitVisitor visitor = new ControlledUnitVisitor(obj);
        removeVisibleMapId(obj, visibleMapId, visitor);
    }

    public static void resetPhaseShift(WorldObject obj) {
        obj.getPhaseShift().clear();
        obj.getSuppressedPhaseShift().clear();
    }

    public static void inheritPhaseShift(WorldObject target, WorldObject source) {
        target.setPhaseShift(source.getPhaseShift());
        target.setSuppressedPhaseShift(source.getSuppressedPhaseShift());
    }

    public static void onMapChange(WorldObject obj) {
        var phaseShift = obj.getPhaseShift();
        var suppressedPhaseShift = obj.getSuppressedPhaseShift();
        ConditionSourceInfo srcInfo = new ConditionSourceInfo(obj);

        obj.getPhaseShift().visibleMapIds.clear();
        obj.getPhaseShift().uiMapPhaseIds.clear();
        obj.getSuppressedPhaseShift().visibleMapIds.clear();


        for (var(mapId, visibleMapInfo) : global.getObjectMgr().getTerrainSwaps().KeyValueList) {
            if (global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.TerrainSwap, visibleMapInfo.id, srcInfo)) {
                if (mapId == obj.getLocation().getMapId()) {
                    phaseShift.addVisibleMapId(visibleMapInfo.id, visibleMapInfo);
                }

                // ui map is visible on all maps
                for (var uiMapPhaseId : visibleMapInfo.uiMapPhaseIDs) {
                    phaseShift.addUiMapPhaseId(uiMapPhaseId);
                }
            } else if (mapId == obj.getLocation().getMapId()) {
                suppressedPhaseShift.addVisibleMapId(visibleMapInfo.id, visibleMapInfo);
            }
        }

        updateVisibilityIfNeeded(obj, false, true);
    }

    public static void onAreaChange(WorldObject obj) {
        var phaseShift = obj.getPhaseShift();
        var suppressedPhaseShift = obj.getSuppressedPhaseShift();
        var oldPhases = phaseShift.phases; // for comparison
        ConditionSourceInfo srcInfo = new ConditionSourceInfo(obj);

        obj.getPhaseShift().clearPhases();
        obj.getSuppressedPhaseShift().clearPhases();

        var areaId = obj.getAreaId();
        var areaEntry = CliDB.AreaTableStorage.get(areaId);

        while (areaEntry != null) {
            var newAreaPhases = global.getObjectMgr().getPhasesForArea(areaEntry.id);

            if (!newAreaPhases.isEmpty()) {
                for (var phaseArea : newAreaPhases) {
                    if (phaseArea.subAreaExclusions.contains(areaId)) {
                        continue;
                    }

                    var phaseId = phaseArea.phaseInfo.id;

                    if (global.getConditionMgr().isObjectMeetToConditions(srcInfo, phaseArea.conditions)) {
                        phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), phaseArea.conditions);
                    } else {
                        suppressedPhaseShift.addPhase(phaseId, getPhaseFlags(phaseId), phaseArea.conditions);
                    }
                }
            }

            areaEntry = CliDB.AreaTableStorage.get(areaEntry.ParentAreaID);
        }

        var changed = phaseShift.phases != oldPhases;
        var unit = obj.toUnit();

        if (unit) {
            for (var aurEff : unit.getAuraEffectsByType(AuraType.Phase)) {
                var phaseId = (int) aurEff.getMiscValueB();
                changed = phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), null) || changed;
            }

            for (var aurEff : unit.getAuraEffectsByType(AuraType.phaseGroup)) {
                var phasesInGroup = global.getDB2Mgr().GetPhasesForGroup((int) aurEff.getMiscValueB());

                for (var phaseId : phasesInGroup) {
                    changed = phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), null) || changed;
                }
            }

            if (phaseShift.personalReferences != 0) {
                phaseShift.personalGuid = unit.getGUID();
            }

            if (changed) {
                unit.onPhaseChange();
            }

            ControlledUnitVisitor visitor = new ControlledUnitVisitor(unit);
            visitor.visitControlledOf(unit, controlled ->
            {
                inheritPhaseShift(controlled, unit);
            });

            if (changed) {
                unit.removeNotOwnSingleTargetAuras(true);
            }
        } else {
            if (phaseShift.personalReferences != 0) {
                phaseShift.personalGuid = obj.getGUID();
            }
        }

        updateVisibilityIfNeeded(obj, true, changed);
    }


    public static boolean onConditionChange(WorldObject obj) {
        return onConditionChange(obj, true);
    }

    public static boolean onConditionChange(WorldObject obj, boolean updateVisibility) {
        var phaseShift = obj.getPhaseShift();
        var suppressedPhaseShift = obj.getSuppressedPhaseShift();
        PhaseShift newSuppressions = new PhaseShift();
        ConditionSourceInfo srcInfo = new ConditionSourceInfo(obj);
        var changed = false;

        for (var pair : phaseShift.phases.ToList()) {
            if (pair.value.areaConditions != null && !global.getConditionMgr().isObjectMeetToConditions(srcInfo, pair.value.areaConditions)) {
                newSuppressions.addPhase(pair.key, pair.value.flags, pair.value.areaConditions, pair.value.references);
                phaseShift.modifyPhasesReferences(pair.key, pair.value, -pair.value.references);
                phaseShift.phases.remove(pair.key);
            }
        }

        for (var pair : suppressedPhaseShift.phases.ToList()) {
            if (global.getConditionMgr().isObjectMeetToConditions(srcInfo, pair.value.areaConditions)) {
                changed = phaseShift.addPhase(pair.key, pair.value.flags, pair.value.areaConditions, pair.value.references) || changed;
                suppressedPhaseShift.modifyPhasesReferences(pair.key, pair.value, -pair.value.references);
                suppressedPhaseShift.phases.remove(pair.key);
            }
        }

        for (var pair : phaseShift.visibleMapIds.ToList()) {
            if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.TerrainSwap, pair.key, srcInfo)) {
                newSuppressions.addVisibleMapId(pair.key, pair.value.visibleMapInfo, pair.value.references);

                for (var uiMapPhaseId : pair.value.visibleMapInfo.uiMapPhaseIDs) {
                    changed = phaseShift.removeUiMapPhaseId(uiMapPhaseId) || changed;
                }

                phaseShift.visibleMapIds.remove(pair.key);
            }
        }

        for (var pair : suppressedPhaseShift.visibleMapIds.ToList()) {
            if (global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.TerrainSwap, pair.key, srcInfo)) {
                changed = phaseShift.addVisibleMapId(pair.key, pair.value.visibleMapInfo, pair.value.references) || changed;

                for (var uiMapPhaseId : pair.value.visibleMapInfo.uiMapPhaseIDs) {
                    changed = phaseShift.addUiMapPhaseId(uiMapPhaseId) || changed;
                }

                suppressedPhaseShift.visibleMapIds.remove(pair.key);
            }
        }

        var unit = obj.toUnit();

        if (unit) {
            for (var aurEff : unit.getAuraEffectsByType(AuraType.Phase)) {
                var phaseId = (int) aurEff.getMiscValueB();

                // if condition was met previously there is nothing to erase
                if (newSuppressions.removePhase(phaseId)) {
                    phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), null); //todo needs checked
                }
            }

            for (var aurEff : unit.getAuraEffectsByType(AuraType.phaseGroup)) {
                var phasesInGroup = global.getDB2Mgr().GetPhasesForGroup((int) aurEff.getMiscValueB());

                if (!phasesInGroup.isEmpty()) {
                    for (var phaseId : phasesInGroup) {
                        var eraseResult = newSuppressions.removePhase(phaseId);

                        // if condition was met previously there is nothing to erase
                        if (eraseResult) {
                            phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), null);
                        }
                    }
                }
            }
        }

        if (phaseShift.personalReferences != 0) {
            phaseShift.personalGuid = obj.getGUID();
        }

        changed = changed || !newSuppressions.phases.isEmpty() || !newSuppressions.visibleMapIds.isEmpty();

        for (var pair : newSuppressions.phases.entrySet()) {
            suppressedPhaseShift.addPhase(pair.getKey(), pair.getValue().flags, pair.getValue().areaConditions, pair.getValue().references);
        }

        for (var pair : newSuppressions.visibleMapIds.entrySet()) {
            suppressedPhaseShift.addVisibleMapId(pair.getKey(), pair.getValue().visibleMapInfo, pair.getValue().references);
        }

        if (unit) {
            if (changed) {
                unit.onPhaseChange();
            }

            ControlledUnitVisitor visitor = new ControlledUnitVisitor(unit);
            visitor.visitControlledOf(unit, controlled ->
            {
                inheritPhaseShift(controlled, unit);
            });

            if (changed) {
                unit.removeNotOwnSingleTargetAuras(true);
            }
        }

        updateVisibilityIfNeeded(obj, updateVisibility, changed);

        return changed;
    }

    public static void sendToPlayer(Player player, PhaseShift phaseShift) {
        PhaseShiftChange phaseShiftChange = new PhaseShiftChange();
        phaseShiftChange.client = player.getGUID();
        phaseShiftChange.phaseshift.PhaseShiftFlag = phaseShift.flags.value;
        phaseShiftChange.phaseshift.personalGUID = phaseShift.personalGuid;

        for (var pair : phaseShift.phases) {
            phaseShiftChange.phaseshift.phases.add(new PhaseShiftDataPhase(pair.value.flags.value, pair.key));
        }

        for (var visibleMapId : phaseShift.visibleMapIds) {
            phaseShiftChange.visibleMapIDs.add((short) visibleMapId.key);
        }

        for (var uiWorldMapAreaIdSwap : phaseShift.uiMapPhaseIds) {
            phaseShiftChange.uiMapPhaseIDs.add((short) uiWorldMapAreaIdSwap.key);
        }

        player.sendPacket(phaseShiftChange);
    }

    public static void sendToPlayer(Player player) {
        sendToPlayer(player, player.getPhaseShift());
    }

    public static void fillPartyMemberPhase(PartyMemberPhaseStates partyMemberPhases, PhaseShift phaseShift) {
        partyMemberPhases.PhaseShiftFlag = phaseShift.flags.value;
        partyMemberPhases.personalGUID = phaseShift.personalGuid;

        for (var pair : phaseShift.phases) {
            partyMemberPhases.list.add(new PartyMemberPhase(pair.value.flags.value, pair.key));
        }
    }

    public static PhaseShift getAlwaysVisiblePhaseShift() {
        return ALWAYS_VISIBLE;
    }


    public static void initDbPhaseShift(PhaseShift phaseShift, PhaseUseFlag phaseUseFlags, int phaseId, int phaseGroupId) {
        phaseShift.clearPhases();
        phaseShift.isDbPhaseShift = true;

        var flags = PhaseShiftFlag.NONE;

        if (phaseUseFlags.hasFlag(PhaseUseFlag.ALWAYSVISIBLE)) {
            flags = PhaseShiftFlag.forValue(flags.getValue() | PhaseShiftFlag.ALWAYSVISIBLE.getValue().getValue() | PhaseShiftFlag.UNPHASED.getValue().getValue());
        }

        if (phaseUseFlags.hasFlag(PhaseUseFlag.INVERSE)) {
            flags = PhaseShiftFlag.forValue(flags.getValue() | PhaseShiftFlag.INVERSE.getValue());
        }

        if (phaseId != 0) {
            phaseShift.addPhase(phaseId, getPhaseFlags(phaseId), null);
        } else {
            var phasesInGroup = global.getDB2Mgr().GetPhasesForGroup(phaseGroupId);

            for (var phaseInGroup : phasesInGroup) {
                phaseShift.addPhase(phaseInGroup, getPhaseFlags(phaseInGroup), null);
            }
        }

        if (phaseShift.phases.isEmpty() || phaseShift.hasPhase(169)) {
            if (flags.hasFlag(PhaseShiftFlag.INVERSE)) {
                flags = PhaseShiftFlag.forValue(flags.getValue() | PhaseShiftFlag.INVERSEUNPHASED.getValue());
            } else {
                flags = PhaseShiftFlag.forValue(flags.getValue() | PhaseShiftFlag.UNPHASED.getValue());
            }
        }

        phaseShift.flags = flags;
    }

    public static void initDbPersonalOwnership(PhaseShift phaseShift, ObjectGuid personalGuid) {
        phaseShift.personalGuid = personalGuid;
    }

    public static void initDbVisibleMapId(PhaseShift phaseShift, int visibleMapId) {
        phaseShift.visibleMapIds.clear();

        if (visibleMapId != -1) {
            phaseShift.addVisibleMapId((int) visibleMapId, global.getObjectMgr().getTerrainSwapInfo((int) visibleMapId));
        }
    }


    public static boolean inDbPhaseShift(WorldObject obj, PhaseUseFlag phaseUseFlags, short phaseId, int phaseGroupId) {
        PhaseShift phaseShift = new PhaseShift();
        initDbPhaseShift(phaseShift, phaseUseFlags, phaseId, phaseGroupId);

        return obj.getPhaseShift().canSee(phaseShift);
    }


    public static int getTerrainMapId(PhaseShift phaseShift, int mapId, TerrainInfo terrain, float x, float y) {
        if (phaseShift.visibleMapIds.isEmpty()) {
            return mapId;
        }

        if (phaseShift.visibleMapIds.size == 1) {
            return phaseShift.visibleMapIds.keys().next();
        }

        var gridCoordinate = MapDefine.computeGridCoordinate(x, y);
        var gx = MapDefine.MAX_NUMBER_OF_GRIDS - 1 - gridCoordinate.axisX();
        var gy = MapDefine.MAX_NUMBER_OF_GRIDS - 1 - gridCoordinate.axisY();

        for (var visibleMap : phaseShift.visibleMapIds.entries()) {
            if (terrain.hasChildTerrainGridFile(visibleMap.key, gx, gy)) {
                return visibleMap.key;
            }
        }

        return mapId;
    }

    public static void setAlwaysVisible(WorldObject obj, boolean apply, boolean updateVisibility) {
        if (apply) {
            obj.getPhaseShift().flags = PhaseShiftFlag.forValue(obj.getPhaseShift().flags.getValue() | PhaseShiftFlag.ALWAYSVISIBLE.getValue());
        } else {
            obj.getPhaseShift().flags = PhaseShiftFlag.forValue(obj.getPhaseShift().flags.getValue() & ~PhaseShiftFlag.ALWAYSVISIBLE.getValue());
        }

        updateVisibilityIfNeeded(obj, updateVisibility, true);
    }

    public static void setInversed(WorldObject obj, boolean apply, boolean updateVisibility) {
        if (apply) {
            obj.getPhaseShift().flags = PhaseShiftFlag.forValue(obj.getPhaseShift().flags.getValue() | PhaseShiftFlag.INVERSE.getValue());
        } else {
            obj.getPhaseShift().flags = PhaseShiftFlag.forValue(obj.getPhaseShift().flags.getValue() & ~PhaseShiftFlag.INVERSE.getValue());
        }

        obj.getPhaseShift().updateUnphasedFlag();

        updateVisibilityIfNeeded(obj, updateVisibility, true);
    }

    public static void printToChat(CommandHandler chat, WorldObject target) {
        var phaseShift = target.getPhaseShift();

        var phaseOwnerName = "N/A";

        if (phaseShift.getHasPersonalPhase()) {
            var personalGuid = global.getObjAccessor().GetWorldObject(target, phaseShift.personalGuid);

            if (personalGuid != null) {
                phaseOwnerName = personalGuid.getName();
            }
        }

        chat.sendSysMessage(SysMessage.PhaseshiftStatus, phaseShift.flags, phaseShift.personalGuid.toString(), phaseOwnerName);

        if (!phaseShift.phases.isEmpty()) {
            StringBuilder phases = new StringBuilder();
            var cosmetic = global.getObjectMgr().getSysMessage(SysMessage.PhaseFlagCosmetic, chat.getSessionDbcLocale());
            var personal = global.getObjectMgr().getSysMessage(SysMessage.PhaseFlagPersonal, chat.getSessionDbcLocale());

            for (var pair : phaseShift.phases.entrySet()) {
                phases.append("\r\n");
                phases.append("   ");
                phases.append(String.format("%1$s (%2$s)'", pair.getKey(), global.getObjectMgr().getPhaseName(pair.getKey())));

                if (pair.getValue().flags.hasFlag(phaseFlags.COSMETIC)) {
                    phases.append(String.format(" (%1$s)", cosmetic));
                }

                if (pair.getValue().flags.hasFlag(phaseFlags.PERSONAL)) {
                    phases.append(String.format(" (%1$s)", personal));
                }
            }

            chat.sendSysMessage(SysMessage.PhaseshiftPhases, phases.toString());
        }

        if (!phaseShift.visibleMapIds.isEmpty()) {
            StringBuilder visibleMapIds = new StringBuilder();

            for (var visibleMapId : phaseShift.visibleMapIds.entrySet()) {
                visibleMapIds.append(visibleMapId.getKey() + ',' + ' ');
            }

            chat.sendSysMessage(SysMessage.PhaseshiftVisibleMapIds, visibleMapIds.toString());
        }

        if (!phaseShift.uiMapPhaseIds.isEmpty()) {
            StringBuilder uiWorldMapAreaIdSwaps = new StringBuilder();

            for (var uiWorldMapAreaIdSwap : phaseShift.uiMapPhaseIds.entrySet()) {
                uiWorldMapAreaIdSwaps.append(String.format("%1$s, ", uiWorldMapAreaIdSwap.getKey()));
            }

            chat.sendSysMessage(SysMessage.PhaseshiftUiWorldMapAreaSwaps, uiWorldMapAreaIdSwaps.toString());
        }
    }

    public static String formatPhases(PhaseShift phaseShift) {
        StringBuilder phases = new StringBuilder();

        for (var phaseId : phaseShift.phases.keySet()) {
            phases.append(phaseId + ',');
        }

        return phases.toString();
    }


    public static boolean isPersonalPhase(int phaseId) {
        var phase = CliDB.PhaseStorage.get(phaseId);

        if (phase != null) {
            return phase.flags.hasFlag(PhaseEntryFlags.PERSONAL);
        }

        return false;
    }


    private static void addPhase(WorldObject obj, int phaseId, ObjectGuid personalGuid, boolean updateVisibility, ControlledUnitVisitor visitor) {
        var changed = obj.getPhaseShift().addPhase(phaseId, getPhaseFlags(phaseId), null);

        if (obj.getPhaseShift().personalReferences != 0) {
            obj.getPhaseShift().personalGuid = personalGuid;
        }

        var unit = obj.toUnit();

        if (unit) {
            unit.onPhaseChange();
            visitor.visitControlledOf(unit, controlled ->
            {
                addPhase(controlled, phaseId, personalGuid, updateVisibility, visitor);
            });
            unit.removeNotOwnSingleTargetAuras(true);
        }

        updateVisibilityIfNeeded(obj, updateVisibility, changed);
    }


    private static void removePhase(WorldObject obj, int phaseId, boolean updateVisibility, ControlledUnitVisitor visitor) {
        var changed = obj.getPhaseShift().removePhase(phaseId);

        var unit = obj.toUnit();

        if (unit) {
            unit.onPhaseChange();
            visitor.visitControlledOf(unit, controlled ->
            {
                removePhase(controlled, phaseId, updateVisibility, visitor);
            });
            unit.removeNotOwnSingleTargetAuras(true);
        }

        updateVisibilityIfNeeded(obj, updateVisibility, changed);
    }


    private static void addPhaseGroup(WorldObject obj, ArrayList<Integer> phasesInGroup, ObjectGuid personalGuid, boolean updateVisibility, ControlledUnitVisitor visitor) {
        var changed = false;

        for (var phaseId : phasesInGroup) {
            changed = obj.getPhaseShift().addPhase(phaseId, getPhaseFlags(phaseId), null) || changed;
        }

        if (obj.getPhaseShift().personalReferences != 0) {
            obj.getPhaseShift().personalGuid = personalGuid;
        }

        var unit = obj.toUnit();

        if (unit) {
            unit.onPhaseChange();
            visitor.visitControlledOf(unit, controlled ->
            {
                addPhaseGroup(controlled, phasesInGroup, personalGuid, updateVisibility, visitor);
            });
            unit.removeNotOwnSingleTargetAuras(true);
        }

        updateVisibilityIfNeeded(obj, updateVisibility, changed);
    }


    private static void removePhaseGroup(WorldObject obj, ArrayList<Integer> phasesInGroup, boolean updateVisibility, ControlledUnitVisitor visitor) {
        var changed = false;

        for (var phaseId : phasesInGroup) {
            changed = obj.getPhaseShift().removePhase(phaseId) || changed;
        }

        var unit = obj.toUnit();

        if (unit) {
            unit.onPhaseChange();
            visitor.visitControlledOf(unit, controlled ->
            {
                removePhaseGroup(controlled, phasesInGroup, updateVisibility, visitor);
            });
            unit.removeNotOwnSingleTargetAuras(true);
        }

        updateVisibilityIfNeeded(obj, updateVisibility, changed);
    }


    private static void addVisibleMapId(WorldObject obj, int visibleMapId, ControlledUnitVisitor visitor) {
        var terrainSwapInfo = global.getObjectMgr().getTerrainSwapInfo(visibleMapId);
        var changed = obj.getPhaseShift().addVisibleMapId(visibleMapId, terrainSwapInfo);

        for (var uiMapPhaseId : terrainSwapInfo.uiMapPhaseIDs) {
            changed = obj.getPhaseShift().addUiMapPhaseId(uiMapPhaseId) || changed;
        }

        var unit = obj.toUnit();

        if (unit) {
            visitor.visitControlledOf(unit, controlled ->
            {
                addVisibleMapId(controlled, visibleMapId, visitor);
            });
        }

        updateVisibilityIfNeeded(obj, false, changed);
    }


    private static void removeVisibleMapId(WorldObject obj, int visibleMapId, ControlledUnitVisitor visitor) {
        var terrainSwapInfo = global.getObjectMgr().getTerrainSwapInfo(visibleMapId);
        var changed = obj.getPhaseShift().removeVisibleMapId(visibleMapId);

        for (var uiWorldMapAreaIDSwap : terrainSwapInfo.uiMapPhaseIDs) {
            changed = obj.getPhaseShift().removeUiMapPhaseId(uiWorldMapAreaIDSwap) || changed;
        }

        var unit = obj.toUnit();

        if (unit) {
            visitor.visitControlledOf(unit, controlled ->
            {
                removeVisibleMapId(controlled, visibleMapId, visitor);
            });
        }

        updateVisibilityIfNeeded(obj, false, changed);
    }

    private static void updateVisibilityIfNeeded(WorldObject obj, boolean updateVisibility, boolean changed) {
        if (changed && obj.isInWorld()) {
            var player = obj.toPlayer();

            if (player) {
                sendToPlayer(player);
            }

            if (updateVisibility) {
                if (player) {
                    player.getMap().sendUpdateTransportVisibility(player);
                }

                obj.updateObjectVisibility();
            }
        }
    }
}
