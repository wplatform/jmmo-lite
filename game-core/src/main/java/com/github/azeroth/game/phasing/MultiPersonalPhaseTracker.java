package com.github.azeroth.game.phasing;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.map.PersonalPhaseGridLoader;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.grid.NGrid;

import java.util.HashMap;

public class MultiPersonalPhaseTracker {
    private final HashMap<ObjectGuid, PlayerPersonalPhasesTracker> playerData = new HashMap<ObjectGuid, PlayerPersonalPhasesTracker>();

    public final void loadGrid(PhaseShift phaseShift, NGrid grid, Map map, Cell cell) {
        if (!phaseShift.getHasPersonalPhase()) {
            return;
        }

        PersonalPhaseGridLoader loader = new PersonalPhaseGridLoader(grid, map, cell, phaseShift.personalGuid, Framework.Constants.gridType.Grid);
        var playerTracker = playerData.get(phaseShift.personalGuid);

        for (var phaseRef : phaseShift.phases.entrySet()) {
            if (!phaseRef.getValue().isPersonal()) {
                continue;
            }

            if (!global.getObjectMgr().hasPersonalSpawns(map.getId(), map.getDifficultyID(), phaseRef.getKey())) {
                continue;
            }

            if (playerTracker.isGridLoadedForPhase(grid.getGridId(), phaseRef.getKey())) {
                continue;
            }

            Logs.MAPS.debug(String.format("Loading personal phase objects (phase %1$s) in %2$s for map %3$s instance %4$s", phaseRef.getKey(), cell, map.getId(), map.getInstanceId()));

            loader.load(phaseRef.getKey());

            playerTracker.setGridLoadedForPhase(grid.getGridId(), phaseRef.getKey());
        }

        if (loader.getLoadedGameObjects() != 0) {
            map.balance();
        }
    }

    public final void unloadGrid(NGrid grid) {
        for (var itr : playerData.ToList()) {
            itr.value.setGridUnloaded(grid.getGridId());

            if (itr.value.IsEmpty) {
                playerData.remove(itr.key);
            }
        }
    }

    public final void registerTrackedObject(int phaseId, ObjectGuid phaseOwner, WorldObject obj) {
        playerData.get(phaseOwner).registerTrackedObject(phaseId, obj);
    }

    public final void unregisterTrackedObject(WorldObject obj) {
        var playerTracker = playerData.get(obj.getPhaseShift().personalGuid);

        if (playerTracker != null) {
            playerTracker.unregisterTrackedObject(obj);
        }
    }

    public final void onOwnerPhaseChanged(WorldObject phaseOwner, NGrid grid, Map map, Cell cell) {
        var playerTracker = playerData.get(phaseOwner.getGUID());

        if (playerTracker != null) {
            playerTracker.onOwnerPhasesChanged(phaseOwner);
        }

        if (grid != null) {
            loadGrid(phaseOwner.getPhaseShift(), grid, map, cell);
        }
    }

    public final void markAllPhasesForDeletion(ObjectGuid phaseOwner) {
        var playerTracker = playerData.get(phaseOwner);

        if (playerTracker != null) {
            playerTracker.markAllPhasesForDeletion();
        }
    }

    public final void update(Map map, int diff) {
        for (var itr : playerData.ToList()) {
            itr.value.update(map, diff);

            if (itr.value.IsEmpty) {
                playerData.remove(itr.key);
            }
        }
    }
}
