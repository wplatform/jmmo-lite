package com.github.azeroth.game.script;

import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.spell.SpellEffectInfo;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.world.WorldContext;

public abstract class SpellValidator {


    protected WorldContext worldContext;

    protected String scriptName;

    boolean _Validate(SpellInfo entry) {
        if (!Validate(entry)) {
            Logs.SCRIPTS.error("Spell `{}` did not pass Validate() function of script `{}` - script will be not added to the spell", entry.getId(), getClass().getSimpleName());
            return false;
        }
        return true;
    }


    boolean ValidateSpellInfoImpl(int spellId) {

        SpellInfo spellInfo = worldContext.getSpellManager().getSpellInfo(spellId, Difficulty.NONE);
        if (spellInfo == null) {
            Logs.SCRIPTS_SPELLS.error("SpellValidator::ValidateSpellInfo: Spell {} does not exist.", spellId);
            return false;
        }

        return true;
    }




    protected abstract boolean Validate(SpellInfo entry);
}
