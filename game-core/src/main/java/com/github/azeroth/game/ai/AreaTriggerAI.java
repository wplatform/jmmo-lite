package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.unit.Unit;

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

    // Called when an unit exit the areaTrigger, or when the AreaTrigger is removed
    public void onUnitExit(Unit unit) {
    }

    // Called when the AreaTrigger is removed
    public void onRemove() {
    }

    public void onPeriodicProc() {
    }
}
