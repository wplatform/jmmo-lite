package com.github.azeroth.game.script;

public interface ScriptFactory {

    AuraScript getAuraScript(int spellId);

    SpellScript getSpellScript(int spellId);
}
