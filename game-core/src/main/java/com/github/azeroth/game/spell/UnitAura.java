package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.UnitListSearcher;
import com.github.azeroth.game.map.grid.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;


public class UnitAura extends Aura {
    private final HashMap<ObjectGuid, HashSet<Integer>> staticApplications = new HashMap<ObjectGuid, HashSet<Integer>>(); // non-area auras

    private DiminishingGroup mAuraDrGroup = DiminishingGroup.values()[0]; // Diminishing

    public UnitAura(AuraCreateInfo createInfo) {
        super(createInfo);
        mAuraDrGroup = DiminishingGroup.NONE;
        loadScripts();
        _InitEffects(createInfo.auraEffectMask, createInfo.caster, createInfo.baseAmount);
        getOwnerAsUnit()._AddAura(this, createInfo.caster);
    }

    @Override
    public void _ApplyForTarget(Unit target, Unit caster, AuraApplication aurApp) {
        super._ApplyForTarget(target, caster, aurApp);

        // register aura diminishing on apply
        if (mAuraDrGroup != DiminishingGroup.NONE) {
            target.applyDiminishingAura(mAuraDrGroup, true);
        }
    }

    @Override
    public void _UnapplyForTarget(Unit target, Unit caster, AuraApplication aurApp) {
        super._UnapplyForTarget(target, caster, aurApp);

        // unregister aura diminishing (and store last time)
        if (mAuraDrGroup != DiminishingGroup.NONE) {
            target.applyDiminishingAura(mAuraDrGroup, false);
        }
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

        getOwnerAsUnit().removeOwnedAura(this, removeMode);
        super.remove(removeMode);
    }

    @Override
    public HashMap<unit, HashSet<Integer>> fillTargetMap(Unit caster) {
        var targets = new HashMap<unit, HashSet<Integer>>();
        var refe = caster;

        if (refe == null) {
            refe = getOwnerAsUnit();
        }

        // add non area aura targets
        // static applications go through spell system first, so we assume they meet conditions
        for (var targetPair : staticApplications.entrySet()) {
            var target = global.getObjAccessor().GetUnit(getOwnerAsUnit(), targetPair.getKey());

            if (target == null && Objects.equals(targetPair.getKey(), getOwnerAsUnit().getGUID())) {
                target = getOwnerAsUnit();
            }

            if (target) {
                targets.put(target, targetPair.getValue());
            }
        }

        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (!hasEffect(spellEffectInfo.effectIndex)) {
                continue;
            }

            // area auras only
            if (spellEffectInfo.effect == SpellEffectName.ApplyAura) {
                continue;
            }

            // skip area update if owner is not in world!
            if (!getOwnerAsUnit().isInWorld()) {
                continue;
            }

            if (getOwnerAsUnit().hasUnitState(UnitState.Isolated)) {
                continue;
            }

            ArrayList<Unit> units = new ArrayList<>();
            var condList = spellEffectInfo.implicitTargetConditions;

            var radius = spellEffectInfo.calcRadius(refe);
            var extraSearchRadius = 0.0f;

            var selectionType = SpellTargetCheckTypes.Default;

            switch (spellEffectInfo.effect) {
                case ApplyAreaAuraParty:
                case ApplyAreaAuraPartyNonrandom:
                    selectionType = SpellTargetCheckTypes.Party;

                    break;
                case ApplyAreaAuraRaid:
                    selectionType = SpellTargetCheckTypes.raid;

                    break;
                case ApplyAreaAuraFriend:
                    selectionType = SpellTargetCheckTypes.Ally;

                    break;
                case ApplyAreaAuraEnemy:
                    selectionType = SpellTargetCheckTypes.Enemy;
                    extraSearchRadius = radius > 0.0f ? SharedConst.ExtraCellSearchRadius : 0.0f;

                    break;
                case ApplyAreaAuraPet:
                    if (condList == null || global.getConditionMgr().isObjectMeetToConditions(getOwnerAsUnit(), refe, condList)) {
                        units.add(getOwnerAsUnit());
                    }

                    /* fallthrough */
                case ApplyAreaAuraOwner: {
                    var owner = getOwnerAsUnit().getCharmerOrOwner();

                    if (owner != null) {
                        if (getOwnerAsUnit().isWithinDistInMap(owner, radius)) {
                            if (condList == null || global.getConditionMgr().isObjectMeetToConditions(owner, refe, condList)) {
                                units.add(owner);
                            }
                        }
                    }

                    break;
                }
                case ApplyAuraOnPet: {
                    var pet = global.getObjAccessor().GetUnit(getOwnerAsUnit(), getOwnerAsUnit().getPetGUID());

                    if (pet != null) {
                        if (condList == null || global.getConditionMgr().isObjectMeetToConditions(pet, refe, condList)) {
                            units.add(pet);
                        }
                    }

                    break;
                }
                case ApplyAreaAuraSummons: {
                    if (condList == null || global.getConditionMgr().isObjectMeetToConditions(getOwnerAsUnit(), refe, condList)) {
                        units.add(getOwnerAsUnit());
                    }

                    selectionType = SpellTargetCheckTypes.Summoned;

                    break;
                }
            }

            if (selectionType != SpellTargetCheckTypes.Default) {
                WorldObjectSpellAreaTargetCheck check = new WorldObjectSpellAreaTargetCheck(radius, getOwnerAsUnit().getLocation(), refe, getOwnerAsUnit(), getSpellInfo(), selectionType, condList, SpellTargetObjectTypes.unit);
                UnitListSearcher searcher = new UnitListSearcher(getOwnerAsUnit(), units, check, gridType.All);
                Cell.visitGrid(getOwnerAsUnit(), searcher, radius + extraSearchRadius);

                // by design WorldObjectSpellAreaTargetCheck allows not-in-world units (for spells) but for auras it is not acceptable
                tangible.ListHelper.removeAll(units, unit -> !unit.isSelfOrInSameMap(getOwnerAsUnit()));
            }

            for (var unit : units) {
                if (!targets.containsKey(unit)) {
                    targets.put(unit, new HashSet<Integer>());
                }

                targets.get(unit).add(spellEffectInfo.effectIndex);
            }
        }

        return targets;
    }

    public final void addStaticApplication(Unit target, HashSet<Integer> effectMask) {
        var effMask = effectMask.ToHashSet();

        // only valid for non-area auras
        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (effMask.contains(spellEffectInfo.effectIndex) && !spellEffectInfo.isEffect(SpellEffectName.ApplyAura)) {
                effMask.remove(spellEffectInfo.effectIndex);
            }
        }

        if (effMask.count == 0) {
            return;
        }

        if (!staticApplications.containsKey(target.getGUID())) {
            staticApplications.put(target.getGUID(), new HashSet<Integer>());
        }

        staticApplications.get(target.getGUID()).UnionWith(effMask);
    }

    public final DiminishingGroup getDiminishGroup() {
        return mAuraDrGroup;
    }

    // Allow Apply Aura Handler to modify and access m_AuraDRGroup
    public final void setDiminishGroup(DiminishingGroup group) {
        mAuraDrGroup = group;
    }
}
