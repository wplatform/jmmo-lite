package com.github.azeroth.game.networking.packet.adventurejournal;

final class AdventureJournalEntry {
    public int adventureJournalID;
    public int priority;

    public AdventureJournalEntry clone() {
        AdventureJournalEntry varCopy = new AdventureJournalEntry();

        varCopy.adventureJournalID = this.adventureJournalID;
        varCopy.priority = this.priority;

        return varCopy;
    }
}
