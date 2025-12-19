package com.github.azeroth.game.domain.instance;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.DungeonEncounter;
import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BossInfo {
    public static final byte MAX_DUNGEON_ENCOUNTERS_PER_BOSS = 4;

    private EncounterState state = EncounterState.TO_BE_DECIDED;
    private EnumMap<EncounterDoorBehavior, Set<ObjectGuid>> door;
    private Set<ObjectGuid> minion;
    private ArrayList<AreaBoundary> boundary;
    private DungeonEncounter[] dungeonEncounters;

    public final DungeonEncounter getDungeonEncounterForDifficulty(Difficulty difficulty) {
        return Arrays.stream(dungeonEncounters)
                .filter(Objects::nonNull)
                .filter(dungeonEncounter -> dungeonEncounter.getDifficultyID() == difficulty.ordinal())
                .findFirst()
                .orElse(null);
    }
}
