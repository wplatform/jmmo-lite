package com.github.azeroth.game.networking.packet.combatlog;

final class SpellDispellData {
    public int spellID;
    public boolean harmful;
    public Integer rolled = null;
    public Integer needed = null;

    public SpellDispellData clone() {
        SpellDispellData varCopy = new SpellDispellData();

        varCopy.spellID = this.spellID;
        varCopy.harmful = this.harmful;
        varCopy.rolled = this.rolled;
        varCopy.needed = this.needed;

        return varCopy;
    }
}
