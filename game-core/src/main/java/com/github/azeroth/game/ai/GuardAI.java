package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class GuardAI extends ScriptedAI {
    public GuardAI(Creature creature) {
        super(creature);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    @Override
    public boolean canSeeAlways(WorldObject obj) {
        var unit = obj.getAsUnit();

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

        Log.outTrace(LogFilter.ScriptsAi, String.format("GuardAI::EnterEvadeMode: %1$s enters evade mode.", me.getGUID().clone()));

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