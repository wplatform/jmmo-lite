package com.github.azeroth.game.ai;



import game.maps.*;







public class TotemAI extends NullCreatureAI {
    private ObjectGuid victimGuid = new ObjectGuid();

    public TotemAI(Creature creature) {
        super(creature);
        victimGuid = ObjectGuid.empty;
    }


//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (me.toTotem().getTotemType() != TotemType.Active) {
            return;
        }

        if (!me.isAlive() || me.isNonMeleeSpellCast(false)) {
            return;
        }

        // Search spell
        var spellInfo = Global.getSpellMgr().getSpellInfo(me.toTotem().getSpell(), me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        // Get spell range
        var maxRange = spellInfo.getMaxRange(false);

        // SpellModOp.Range not applied in this place just because not existence range mods for attacking totems

        var victim = !victimGuid.isEmpty() ? Global.getObjAccessor().getUnit(me, victimGuid.clone()) : null;

        // Search victim if no, not attackable, or out of range, or friendly (possible in case duel end)
        if (victim == null || !victim.isTargetableForAttack() || !me.isWithinDistInMap(victim, maxRange) || me.isFriendlyTo(victim) || !me.canSeeOrDetect(victim)) {
            var extraSearchRadius = maxRange > 0.0f ? SharedConst.ExtraCellSearchRadius : 0.0f;
            var uCheck = new NearestAttackableUnitInObjectRangeCheck(me, me.getCharmerOrOwnerOrSelf(), maxRange);
            var checker = new UnitLastSearcher(me, uCheck, GridType.All);
            Cell.visitGrid(me, checker, maxRange + extraSearchRadius);
            victim = checker.getTarget();
        }

        // If have target
        if (victim != null) {
            // remember
            victimGuid = victim.getGUID().clone();

            // attack
            me.castSpell(victim, me.toTotem().getSpell());
        } else {
            victimGuid.clear();
        }
    }
    @Override
    public void attackStart(Unit victim) {
    }
}