package com.github.azeroth.game.ai;









public class EntryCheckPredicate implements ICheck<ObjectGuid> {

//ORIGINAL LINE: readonly uint _entry;
    private final int entry;


//ORIGINAL LINE: public EntryCheckPredicate(uint entry)
    public EntryCheckPredicate(int entry) {
        this.entry = entry;
    }

    public final boolean invoke(ObjectGuid guid) {
        return guid.getEntry() == entry;
    }
}