package com.github.azeroth.game.spell;


import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;


public class WorldObjectSpellConeTargetCheck extends WorldObjectSpellAreaTargetCheck {
    private final Position coneSrc;
    private final float coneAngle;
    private final float lineWidth;

    public WorldObjectSpellConeTargetCheck(Position coneSrc, float coneAngle, float lineWidth, float range, WorldObject caster, SpellInfo spellInfo, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList, SpellTargetObjectTypes objectType) {
        super(range, caster.getLocation(), caster, caster, spellInfo, selectionType, condList, objectType);
        coneSrc = coneSrc;
        coneAngle = coneAngle;
        lineWidth = lineWidth;
    }

    @Override
    public boolean invoke(WorldObject target) {
        if (spellInfo.hasAttribute(SpellCustomAttributes.ConeBack)) {
            if (coneSrc.hasInArc(-Math.abs(coneAngle), target.getLocation())) {
                return false;
            }
        } else if (spellInfo.hasAttribute(SpellCustomAttributes.ConeLine)) {
            if (!coneSrc.hasInLine(target.getLocation(), target.getCombatReach(), lineWidth)) {
                return false;
            }
        } else {
            if (!caster.isUnit() || !caster.toUnit().isWithinBoundaryRadius(target.toUnit())) {
                // coneAngle > 0 . select targets in front
                // coneAngle < 0 . select targets in back
                if (coneSrc.hasInArc(coneAngle, target.getLocation()) != MathUtil.fuzzyGe(coneAngle, 0.0f)) {
                    return false;
                }
            }
        }

        return super.invoke(target);
    }
}
