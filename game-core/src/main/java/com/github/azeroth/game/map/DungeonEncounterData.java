package com.github.azeroth.game.map;

import com.github.azeroth.game.domain.instance.BossInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DungeonEncounterData {
    private int bossId;
    private int[] dungeonEncounterId = new int[];
}
