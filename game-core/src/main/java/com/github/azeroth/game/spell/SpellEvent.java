package com.github.azeroth.game.spell;


public class SpellEvent extends BasicEvent {
    private final Spell spell;

    public SpellEvent(Spell spell) {
        spell = spell;
    }

    @Override
    public boolean isDeletable() {
        return spell.isDeletable();
    }

    public final Spell getSpell() {
        return spell;
    }


    @Override
    public boolean execute(long etime, int pTime) {
        // update spell if it is not finished
        if (spell.getState() != SpellState.Finished) {
            spell.update(pTime);
        }

        // check spell state to process
        switch (spell.getState()) {
            case Finished: {
                // spell was finished, check deletable state
                if (spell.isDeletable()) {
                    // check, if we do have unfinished triggered spells
                    return true; // spell is deletable, finish event
                }

                // event will be re-added automatically at the end of routine)
                break;
            }
            case Delayed: {
                // first, check, if we have just started
                if (spell.getDelayStart() != 0) {
                    // run the spell handler and think about what we can do next
                    var tOffset = etime - spell.getDelayStart();
                    var nOffset = spell.handleDelayed(tOffset);

                    if (nOffset != 0) {
                        // re-add us to the queue
                        spell.getCaster().getEvents().addEvent(this, duration.ofSeconds(spell.getDelayStart() + nOffset), false);

                        return false; // event not complete
                    }
                    // event complete
                    // finish update event will be re-added automatically at the end of routine)
                } else {
                    // delaying had just started, record the moment
                    spell.setDelayStart(etime);
                    // handle effects on caster if the spell has travel time but also affects the caster in some way
                    var nOffset = spell.handleDelayed(0);

                    // re-plan the event for the delay moment
                    spell.getCaster().getEvents().addEvent(this, duration.ofSeconds(etime + nOffset), false);

                    return false; // event not complete
                }

                break;
            }
        }

        // spell processing not complete, plan event on the next update interval
        spell.getCaster().getEvents().addEvent(this, duration.ofSeconds(etime + 1), false);

        return false; // event not complete
    }


    @Override
    public void abort(long eTime) {
        // oops, the spell we try to do is aborted
        if (spell.getState() != SpellState.Finished) {
            spell.cancel();
        }
    }
}
