package com.github.azeroth.game.spell;


final class ServersideSpellName {
    public SpellnameRecord name;

    public ServersideSpellName() {
    }

    public ServersideSpellName(int id, String name) {
        name = new SpellNameRecord();
        name.name = new LocalizedString();

        name.id = id;

        for (Locale i = 0; i.getValue() < locale.Total.getValue(); ++i) {
            name.name.charAt(i) = name;
        }
    }

    public ServersideSpellName clone() {
        ServersideSpellName varCopy = new ServersideSpellName();

        varCopy.name = this.name;

        return varCopy;
    }
}
