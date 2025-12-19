package com.github.azeroth.game.ai;



import java.util.*;






public class SummonList extends ArrayList<ObjectGuid> {
    private final Creature me;

    public SummonList(Creature creature) {
        me = creature;
    }

    public final void summon(Creature summon) {
        this.add(summon.getGUID().clone());
    }


    public final void doZoneInCombat() {
        doZoneInCombat(0);
    }


//ORIGINAL LINE: public void DoZoneInCombat(uint entry = 0)

    public final void doZoneInCombat(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id.clone());

            if (summon && summon.isAIEnabled() && (entry == 0 || summon.getEntry() == entry)) {
                summon.getAI().doZoneInCombat(null);
            }
        }
    }


//ORIGINAL LINE: public void DespawnEntry(uint entry)
    public final void despawnEntry(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id.clone());

            if (!summon) {
                this.remove(id.clone());
            } else if (summon.getEntry() == entry) {
                this.remove(id.clone());
                summon.despawnOrUnsummon();
            }
        }
    }
    public final void despawnAll() {
        while (!this.Empty()) {
            var summon = ObjectAccessor.getCreature(me, this.FirstOrDefault());
            this.remove(0);

            if (summon) {
                summon.despawnOrUnsummon();
            }
        }
    }

    public final void despawn(Creature summon) {
        this.remove(summon.getGUID().clone());
    }

    public final void despawnIf(ICheck<ObjectGuid> predicate) {
        tangible.ListHelper.removeAll(this, predicate);
    }

    public final void despawnIf(java.util.function.Predicate<ObjectGuid> predicate) {
        tangible.ListHelper.removeAll(this, predicate);
    }

    public final void removeNotExisting() {
        for (var id : this) {
            if (!ObjectAccessor.getCreature(me, id.clone())) {
                this.remove(id.clone());
            }
        }
    }


    public final void doAction(int info, ICheck<ObjectGuid> predicate) {
        doAction(info, predicate, 0);
    }


//ORIGINAL LINE: public void DoAction(int info, ICheck<ObjectGuid> predicate, ushort max = 0)

    public final void doAction(int info, ICheck<ObjectGuid> predicate, short max) {
        // We need to use a copy of SummonList here, otherwise original SummonList would be modified
        ArrayList<ObjectGuid> listCopy = new ArrayList<ObjectGuid>(this);
        listCopy.RandomResize(predicate.Invoke, max);
        doActionImpl(info, listCopy);
    }


    public final void doAction(int info, java.util.function.Predicate<ObjectGuid> predicate) {
        doAction(info, predicate, 0);
    }


//ORIGINAL LINE: public void DoAction(int info, Predicate<ObjectGuid> predicate, ushort max = 0)

    public final void doAction(int info, java.util.function.Predicate<ObjectGuid> predicate, short max) {
        // We need to use a copy of SummonList here, otherwise original SummonList would be modified
        ArrayList<ObjectGuid> listCopy = new ArrayList<ObjectGuid>(this);
        listCopy.RandomResize(predicate, max);
        doActionImpl(info, listCopy);
    }


//ORIGINAL LINE: public bool HasEntry(uint entry)
    public final boolean hasEntry(int entry) {
        for (var id : this) {
            var summon = ObjectAccessor.getCreature(me, id.clone());

            if (summon && summon.getEntry() == entry) {
                return true;
            }
        }

        return false;
    }

    private void doActionImpl(int action, ArrayList<ObjectGuid> summons) {
        for (var guid : summons) {
            var summon = ObjectAccessor.getCreature(me, guid.clone());

            if (summon && summon.isAIEnabled()) {
                summon.getAI().doAction(action);
            }
        }
    }
}