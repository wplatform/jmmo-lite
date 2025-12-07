package game.ai;

import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class AreaTriggerAI {
    protected AreaTrigger at;

    public AreaTriggerAI(AreaTrigger a) {
        at = a;
    }

    // Called when the AreaTrigger has just been initialized, just before added to map
    public void onInitialize() {
    }

    // Called when the AreaTrigger has just been created
    public void onCreate() {
    }

    // Called on each AreaTrigger update
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void OnUpdate(uint diff)
    public void onUpdate(int diff) {
    }

    // Called when the AreaTrigger reach splineIndex
    public void onSplineIndexReached(int splineIndex) {
    }

    // Called when the AreaTrigger reach its destination
    public void onDestinationReached() {
    }

    // Called when an unit enter the AreaTrigger
    public void onUnitEnter(Unit unit) {
    }

    // Called when an unit exit the AreaTrigger, or when the AreaTrigger is removed
    public void onUnitExit(Unit unit) {
    }

    // Called when the AreaTrigger is removed
    public void onRemove() {
    }

    public void onPeriodicProc() {
    }
}