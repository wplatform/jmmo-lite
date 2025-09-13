package com.github.azeroth.game.phasing;

import com.github.azeroth.game.entity.object.WorldObject;

import java.util.HashMap;
import java.util.Map;

class PlayerPersonalPhasesTracker {
    private final HashMap<Integer, PersonalPhaseSpawns> spawns = new HashMap<Integer, PersonalPhaseSpawns>();

    public final boolean isEmpty() {
        return spawns.isEmpty();
    }

    public final void registerTrackedObject(int phaseId, WorldObject obj) {
        spawns.get(phaseId).objects.add(obj);
    }

    public final void unregisterTrackedObject(WorldObject obj) {
        for (var spawns : spawns.values()) {
            spawns.objects.remove(obj);
        }
    }

    public final void onOwnerPhasesChanged(WorldObject owner) {
        var phaseShift = owner.getPhaseShift();

        // Loop over all our tracked phases. If any don't exist - delete them

        for (var(phaseId, spawns) : spawns) {
            if (!spawns.durationRemaining.HasValue && !phaseShift.hasPhase(phaseId)) {
                spawns.durationRemaining = PersonalPhaseSpawns.DELETE_TIME_DEFAULT;
            }
        }

        // loop over all owner phases. If any exist and marked for deletion - reset delete
        for (var phaseRef : phaseShift.phases.entrySet()) {
            var spawns = spawns.get(phaseRef.getKey());

            if (spawns != null) {
                spawns.durationRemaining = null;
            }
        }
    }

    public final void markAllPhasesForDeletion() {
        for (var spawns : spawns.values()) {
            spawns.durationRemaining = PersonalPhaseSpawns.DELETE_TIME_DEFAULT;
        }
    }

    public final void update(Map map, int diff) {
        for (var itr : spawns.ToList()) {
            if (itr.value.durationRemaining.HasValue) {
                itr.value.durationRemaining = itr.value.durationRemaining.Value - duration.ofSeconds(diff);

                if (itr.value.durationRemaining.value <= duration.Zero) {
                    despawnPhase(map, itr.value);
                    spawns.remove(itr.key);
                }
            }
        }
    }

    public final boolean isGridLoadedForPhase(int gridId, int phaseId) {
        var spawns = spawns.get(phaseId);

        if (spawns != null) {
            return spawns.grids.contains((short) gridId);
        }

        return false;
    }

    public final void setGridLoadedForPhase(int gridId, int phaseId) {
        if (!spawns.containsKey(phaseId)) {
            spawns.put(phaseId, new PersonalPhaseSpawns());
        }

        var group = spawns.get(phaseId);
        group.grids.add((short) gridId);
    }

    public final void setGridUnloaded(int gridId) {
        for (var itr : spawns.ToList()) {
            itr.value.grids.remove((short) gridId);

            if (itr.value.isEmpty()) {
                spawns.remove(itr.key);
            }
        }
    }

    private void despawnPhase(Map map, PersonalPhaseSpawns spawns) {
        for (var obj : spawns.objects) {
            map.addObjectToRemoveList(obj);
        }

        spawns.Objects.clear();
        spawns.grids.clear();
    }
}
