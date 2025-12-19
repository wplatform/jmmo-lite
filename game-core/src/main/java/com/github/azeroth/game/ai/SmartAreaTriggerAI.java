package com.github.azeroth.game.ai;










public class SmartAreaTriggerAI extends AreaTriggerAI {
    private final SmartScript script = new SmartScript();

    public SmartAreaTriggerAI(AreaTrigger areaTrigger) {
        super(areaTrigger);
    }

    @Override
    public void onInitialize() {
        getScript().onInitialize(at);
    }


//ORIGINAL LINE: public override void OnUpdate(uint diff)
    @Override
    public void onUpdate(int diff) {
        getScript().onUpdate(diff);
    }

    @Override
    public void onUnitEnter(Unit unit) {
        getScript().processEventsFor(SmartEvents.AreatriggerOntrigger, unit);
    }


//ORIGINAL LINE: public void SetTimedActionList(SmartScriptHolder e, uint entry, Unit invoker)
    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        getScript().setTimedActionList(e, entry, invoker);
    }

    public final SmartScript getScript() {
        return script;
    }
}