package com.github.azeroth.game.spell;


public class ChargeDropEvent extends BasicEvent {
    private final Aura base;
    private final AuraRemoveMode mode;

    public ChargeDropEvent(Aura aura, AuraRemoveMode mode) {
        base = aura;
        mode = mode;
    }

    @Override
    public boolean execute(long etime, int pTime) {
        // _base is always valid (look in aura._Remove())
        base.modChargesDelayed(-1, mode);

        return true;
    }
}
