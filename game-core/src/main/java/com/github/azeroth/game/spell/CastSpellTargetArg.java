package com.github.azeroth.game.spell;

import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;

public class CastSpellTargetArg {
    public SpellCastTargets targets;

    public CastSpellTargetArg() {
        targets = new SpellCastTargets();
    }

    public CastSpellTargetArg(WorldObject target) {
        if (target != null) {
            var unitTarget = target.toUnit();

            if (unitTarget != null) {
                targets = new SpellCastTargets();
                targets.setUnitTarget(unitTarget);
            } else {
                var goTarget = target.toGameObject();

                if (goTarget != null) {
                    targets = new SpellCastTargets();
                    targets.setGOTarget(goTarget);
                } else {
                    var itemTarget = target.toItem();

                    if (itemTarget != null) {
                        targets = new SpellCastTargets();
                        targets.setItemTarget(itemTarget);
                    }
                }
                // error when targeting anything other than units and gameobjects
            }
        } else {
            targets = new SpellCastTargets(); // nullptr is allowed
        }
    }

    public CastSpellTargetArg(Item itemTarget) {
        targets = new SpellCastTargets();
        targets.setItemTarget(itemTarget);
    }

    public CastSpellTargetArg(Position dest) {
        targets = new SpellCastTargets();
        targets.setDst(dest);
    }

    public CastSpellTargetArg(SpellCastTargets targets) {
        this.targets = targets;
    }
}
