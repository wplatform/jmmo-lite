package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class NullCreatureAI extends CreatureAI {
    public NullCreatureAI(Creature creature) {
        super(creature);
        creature.reactState = ReactStates.Passive;
    }

    @Override
    public void moveInLineOfSight(Unit unit) {
    }
    @Override
    public void attackStart(Unit unit) {
    }
    @Override
    public void justStartedThreateningMe(Unit unit) {
    }
    @Override
    public void justEnteredCombat(Unit who) {
    }
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
    }
    @Override
    public void justAppeared() {
    }
    @Override
    public void enterEvadeMode(EvadeReason why) {
    }
    @Override
    public void onCharmed(boolean isNew) {
    }
}