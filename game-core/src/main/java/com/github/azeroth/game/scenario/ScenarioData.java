package com.github.azeroth.game.scenario;


import java.util.HashMap;


public class ScenarioData {
    public ScenarioRecord entry;
    public HashMap<Byte, ScenarioStepRecord> steps = new HashMap<Byte, ScenarioStepRecord>();
}
