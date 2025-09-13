package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.NearestAttackableUnitInObjectRangeCheck;
import com.github.azeroth.game.map.UnitLastSearcher;
import com.github.azeroth.game.map.grid.Cell;

public class TotemAI extends NullCreatureAI {
    private ObjectGuid victimGuid;

    public TotemAI(Creature creature) {
        super(creature);
        victimGuid = ObjectGuid.EMPTY;
    }

    @Override
    public void updateAI(int diff) {
        if (me.toTotem().getTotemType() != TotemType.active) {
            return;
        }

        if (!me.isAlive() || me.isNonMeleeSpellCast(false)) {
            return;
        }

        // Search spell
        var spellInfo = global.getSpellMgr().getSpellInfo(me.toTotem().getSpell(), me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        // Get spell range
        var max_range = spellInfo.getMaxRange(false);

        // SpellModOp.Range not applied in this place just because not existence range mods for attacking totems

        var victim = !victimGuid.isEmpty() ? global.getObjAccessor().GetUnit(me, victimGuid) : null;

        // Search victim if no, not attackable, or out of range, or friendly (possible in case duel end)
        if (victim == null || !victim.isTargetableForAttack() || !me.isWithinDistInMap(victim, max_range) || me.isFriendlyTo(victim) || !me.canSeeOrDetect(victim)) {
            var extraSearchRadius = max_range > 0.0f ? SharedConst.ExtraCellSearchRadius : 0.0f;
            var u_check = new NearestAttackableUnitInObjectRangeCheck(me, me.getCharmerOrOwnerOrSelf(), max_range);
            var checker = new UnitLastSearcher(me, u_check, gridType.All);
            Cell.visitGrid(me, checker, max_range + extraSearchRadius);
            victim = checker.getTarget();
        }

        // If have target
        if (victim != null) {
            // remember
            victimGuid = victim.GUID;

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
