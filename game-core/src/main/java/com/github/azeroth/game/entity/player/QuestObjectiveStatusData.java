package com.github.azeroth.game.entity.player;

final class QuestObjectiveStatusData {

    public (
    public Questobjective objective;
    status)questStatusPair;
    int questID, QuestStatusData

    public QuestObjectiveStatusData clone() {
        QuestObjectiveStatusData varCopy = new QuestObjectiveStatusData();

        varCopy.objective = this.objective;

        return varCopy;
    }
}
