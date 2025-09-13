package com.github.azeroth.game.networking.packet.quest;

public final class QuestObjectiveSimple {
    public int id;
    public int objectID;
    public int amount;
    public byte type;

    public QuestObjectiveSimple clone() {
        QuestObjectiveSimple varCopy = new QuestObjectiveSimple();

        varCopy.id = this.id;
        varCopy.objectID = this.objectID;
        varCopy.amount = this.amount;
        varCopy.type = this.type;

        return varCopy;
    }
}
