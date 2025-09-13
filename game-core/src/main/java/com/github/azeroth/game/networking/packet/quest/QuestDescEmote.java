package com.github.azeroth.game.networking.packet.quest;

public final class QuestDescEmote {

    public int type;
    public int delay;

    public QuestDescEmote(int type) {
        this(type, 0);
    }

    public QuestDescEmote() {
    }
    public QuestDescEmote(int type, int delay) {
        this.type = type;
        this.delay = delay;
    }

}
