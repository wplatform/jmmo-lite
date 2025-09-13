package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.creature.Creature;

import java.util.ArrayList;


public class SummonList extends ArrayList<ObjectGuid> {
    private final Creature me;

    public SummonList(Creature creature) {
        me = creature;
    }

    public final void summon(Creature summon) {
        this.add(summon.getGUID());
    }


    public final void doZoneInCombat() {
        doZoneInCombat(0);
    }

    public final void doZoneInCombat(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id);

            if (summon && summon.isAIEnabled() && (entry == 0 || summon.getEntry() == entry)) {
                summon.getAI().doZoneInCombat(null);
            }
        }
    }

    public final void despawnEntry(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id);

            if (!summon) {
                this.remove(id);
            } else if (summon.getEntry() == entry) {
                this.remove(id);
                summon.despawnOrUnsummon();
            }
        }
    }

    public final void despawnAll() {
        while (!this.isEmpty()) {
            var summon = ObjectAccessor.getCreature(me, this.FirstOrDefault());
            this.remove(0);

            if (summon) {
                summon.despawnOrUnsummon();
            }
        }
    }

    public final void despawn(Creature summon) {
        this.remove(summon.getGUID());
    }

    public final void despawnIf(ICheck<ObjectGuid> predicate) {
        tangible.ListHelper.removeAll(this, predicate);
    }

    public final void despawnIf(java.util.function.Predicate<ObjectGuid> predicate) {
        tangible.ListHelper.removeAll(this, predicate);
    }

    public final void removeNotExisting() {
        for (var id : this) {
            if (!ObjectAccessor.getCreature(me, id)) {
                this.remove(id);
            }
        }
    }


    public final void doAction(int info, ICheck<ObjectGuid> predicate) {
        doAction(info, predicate, 0);
    }

    public final void doAction(int info, ICheck<ObjectGuid> predicate, short max) {
        // We need to use a copy of SummonList here, otherwise original SummonList would be modified
        ArrayList<ObjectGuid> listCopy = new ArrayList<ObjectGuid>(this);
        listCopy.RandomResize(predicate.Invoke, max);
        doActionImpl(info, listCopy);
    }


    public final void doAction(int info, java.util.function.Predicate<ObjectGuid> predicate) {
        doAction(info, predicate, 0);
    }

    public final void doAction(int info, java.util.function.Predicate<ObjectGuid> predicate, short max) {
        // We need to use a copy of SummonList here, otherwise original SummonList would be modified
        ArrayList<ObjectGuid> listCopy = new ArrayList<ObjectGuid>(this);
        listCopy.RandomResize(predicate, max);
        doActionImpl(info, listCopy);
    }

    public final boolean hasEntry(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id);

            if (summon && summon.getEntry() == entry) {
                return true;
            }
        }

        return false;
    }

    private void doActionImpl(int action, ArrayList<ObjectGuid> summons) {
        for (var guid : summons) {
            var summon = ObjectAccessor.getCreature(me, guid);

            if (summon && summon.isAIEnabled()) {
                summon.getAI().doAction(action);
            }
        }
    }
}
