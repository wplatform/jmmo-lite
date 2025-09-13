package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class GuardAI extends ScriptedAI {
    public GuardAI(Creature creature) {
        super(creature);
    }

    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    @Override
    public boolean canSeeAlways(WorldObject obj) {
        var unit = obj.toUnit();

        if (unit != null) {
            if (unit.isControlledByPlayer() && me.isEngagedBy(unit)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
        if (!me.isAlive()) {
            me.getMotionMaster().moveIdle();
            me.combatStop(true);
            engagementOver();

            return;
        }

        Log.outTrace(LogFilter.ScriptsAi, String.format("GuardAI::EnterEvadeMode: %1$s enters evade mode.", me.getGUID()));

        me.removeAllAuras();
        me.combatStop(true);
        engagementOver();

        me.getMotionMaster().moveTargetedHome();
    }

    @Override
    public void justDied(Unit killer) {
        if (killer != null) {
            var player = killer.getCharmerOrOwnerPlayerOrPlayerItself();

            if (player != null) {
                me.sendZoneUnderAttackMessage(player);
            }
        }
    }
}
