package game.ai;

import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class EntryCheckPredicate implements ICheck<ObjectGuid> {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: readonly uint _entry;
    private final int entry;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public EntryCheckPredicate(uint entry)
    public EntryCheckPredicate(int entry) {
        this.entry = entry;
    }

    public final boolean invoke(ObjectGuid guid) {
        return guid.getEntry() == entry;
    }
}