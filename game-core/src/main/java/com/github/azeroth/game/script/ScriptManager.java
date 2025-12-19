package com.github.azeroth.game.script;

public interface ScriptManager {

    AuraScript getAuraScript(int spellId);

    SpellScript getSpellScript(int spellId);
}
