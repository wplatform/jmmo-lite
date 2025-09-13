package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.UnitListSearcher;
import com.github.azeroth.game.map.grid.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class DynObjAura extends Aura {
    public dynObjAura(AuraCreateInfo createInfo) {
        super(createInfo);
        loadScripts();
        _InitEffects(createInfo.auraEffectMask, createInfo.caster, createInfo.baseAmount);
        getDynobjOwner().setAura(this);
    }


    @Override
    public void remove() {
        remove(AuraRemoveMode.Default);
    }

    @Override
    public void remove(AuraRemoveMode removeMode) {
        if (isRemoved()) {
            return;
        }

        _Remove(removeMode);
        super.remove(removeMode);
    }

    @Override
    public HashMap<unit, HashSet<Integer>> fillTargetMap(Unit caster) {
        var targets = new HashMap<unit, HashSet<Integer>>();
        var dynObjOwnerCaster = getDynobjOwner().getCaster();
        var radius = getDynobjOwner().getRadius();

        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (!hasEffect(spellEffectInfo.effectIndex)) {
                continue;
            }

            // we can't use effect type like area auras to determine check type, check targets
            var selectionType = spellEffectInfo.targetA.getCheckType();

            if (spellEffectInfo.targetB.getReferenceType() == SpellTargetReferenceTypes.dest) {
                selectionType = spellEffectInfo.targetB.getCheckType();
            }

            ArrayList<Unit> targetList = new ArrayList<>();
            var condList = spellEffectInfo.implicitTargetConditions;

            WorldObjectSpellAreaTargetCheck check = new WorldObjectSpellAreaTargetCheck(radius, getDynobjOwner().getLocation(), dynObjOwnerCaster, dynObjOwnerCaster, getSpellInfo(), selectionType, condList, SpellTargetObjectTypes.unit);
            UnitListSearcher searcher = new UnitListSearcher(getDynobjOwner(), targetList, check, gridType.All);
            Cell.visitGrid(getDynobjOwner(), searcher, radius);

            // by design WorldObjectSpellAreaTargetCheck allows not-in-world units (for spells) but for auras it is not acceptable
            tangible.ListHelper.removeAll(targetList, unit -> !unit.isSelfOrInSameMap(getDynobjOwner()));

            for (var unit : targetList) {
                if (!targets.containsKey(unit)) {
                    targets.put(unit, new HashSet<Integer>());
                }

                targets.get(unit).add(spellEffectInfo.effectIndex);
            }
        }

        return targets;
    }
}
