package com.github.azeroth.game.spell.events;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.CastSpellExtraArgs;

public class DelayedCastEvent extends BasicEvent {
    private Unit trigger;
    private Unit target;

    private int spellId;
    private CastSpellExtraArgs castFlags;


    public DelayedCastEvent(Unit trigger, Unit target, int spellId, CastSpellExtraArgs args) {
        setTrigger(trigger);
        setTarget(target);
        setSpellId(spellId);
        setCastFlags(args);
    }

    public final Unit getTrigger() {
        return trigger;
    }

    public final void setTrigger(Unit value) {
        trigger = value;
    }

    public final Unit getTarget() {
        return target;
    }

    public final void setTarget(Unit value) {
        target = value;
    }


    public final int getSpellId() {
        return spellId;
    }


    public final void setSpellId(int value) {
        spellId = value;
    }

    public final CastSpellExtraArgs getCastFlags() {
        return castFlags;
    }

    public final void setCastFlags(CastSpellExtraArgs value) {
        castFlags = value;
    }


    @Override
    public boolean execute(long etime, int pTime) {
        getTrigger().castSpell(getTarget(), getSpellId(), getCastFlags());

        return true;
    }
}
