package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.unit.Unit;

public class SmartAreaTriggerAI extends AreaTriggerAI {
    private final smartScript script = new smartScript();

    public SmartAreaTriggerAI(AreaTrigger areaTrigger) {
        super(areaTrigger);
    }

    @Override
    public void onInitialize() {
        getScript().onInitialize(at);
    }

    @Override
    public void onUpdate(int diff) {
        getScript().onUpdate(diff);
    }

    @Override
    public void onUnitEnter(Unit unit) {
        getScript().processEventsFor(SmartEvents.AreatriggerOntrigger, unit);
    }

    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        getScript().setTimedActionList(e, entry, invoker);
    }

    public final SmartScript getScript() {
        return script;
    }
}
