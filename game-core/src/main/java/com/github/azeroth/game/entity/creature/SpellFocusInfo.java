package com.github.azeroth.game.entity.creature;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.spell.Spell;

final class SpellFocusInfo {
    public Spell spell;
    public int delay; // ms until the creature's target should snap back (0 = no snapback scheduled)
    public ObjectGuid target = ObjectGuid.EMPTY; // the creature's "real" target while casting
    public float orientation; // the creature's "real" orientation while casting

}
