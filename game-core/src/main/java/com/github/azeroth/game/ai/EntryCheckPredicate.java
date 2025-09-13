package com.github.azeroth.game.ai;

public class EntryCheckPredicate implements ICheck<ObjectGuid> {

    private final int entry;


    public EntryCheckPredicate(int entry) {
        entry = entry;
    }

    public final boolean invoke(ObjectGuid guid) {
        return guid.getEntry() == entry;
    }
}
