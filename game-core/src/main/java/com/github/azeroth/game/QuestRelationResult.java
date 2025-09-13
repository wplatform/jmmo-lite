package com.github.azeroth.game;


import com.github.azeroth.game.quest.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestRelationResult extends ArrayList<Integer> {
    private final boolean onlyActive;

    public QuestRelationResult(List<Integer> range, boolean onlyActive) {
        super(range);
        this.onlyActive = onlyActive;
    }

    public final boolean hasQuest(int questId) {
        return this.contains(questId) && (!onlyActive || Quest.isTakingQuestEnabled(questId));
    }
}
