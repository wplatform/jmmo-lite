package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SmartAreaTriggerAI extends AreaTriggerAI {
    private final SmartScript script = new SmartScript();

    public SmartAreaTriggerAI(AreaTrigger areaTrigger) {
        super(areaTrigger);
    }

    @Override
    public void onInitialize() {
        getScript().onInitialize(at);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void OnUpdate(uint diff)
    @Override
    public void onUpdate(int diff) {
        getScript().onUpdate(diff);
    }

    @Override
    public void onUnitEnter(Unit unit) {
        getScript().processEventsFor(SmartEvents.AreatriggerOntrigger, unit);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetTimedActionList(SmartScriptHolder e, uint entry, Unit invoker)
    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        getScript().setTimedActionList(e, entry, invoker);
    }

    public final SmartScript getScript() {
        return script;
    }
}