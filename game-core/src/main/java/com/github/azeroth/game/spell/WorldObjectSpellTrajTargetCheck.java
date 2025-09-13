package com.github.azeroth.game.spell;


import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;


public class WorldObjectSpellTrajTargetCheck extends WorldObjectSpellTargetCheck {
    private final float range;
    private final Position position;

    public WorldObjectSpellTrajTargetCheck(float range, Position position, WorldObject caster, SpellInfo spellInfo, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList, SpellTargetObjectTypes objectType) {
        super(caster, caster, spellInfo, selectionType, condList, objectType);
        range = range;
        position = position;
    }

    @Override
    public boolean invoke(WorldObject target) {
        // return all targets on missile trajectory (0 - size of a missile)
        if (!caster.getLocation().hasInLine(target.getLocation(), target.getCombatReach(), SpellConst.TrajectoryMissileSize)) {
            return false;
        }

        if (target.getLocation().getExactdist2D(position) > range) {
            return false;
        }

        return super.invoke(target);
    }
}
