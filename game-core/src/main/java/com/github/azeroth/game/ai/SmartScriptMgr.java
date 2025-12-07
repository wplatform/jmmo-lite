package com.github.azeroth.game.ai;/*
 * Java translation of SmartScriptMgr.cpp
 * Manages smart scripts for creatures and gameobjects. Comments preserved.
 */

import lombok.Getter;
import lombok.Setter;
import java.util.Map;
import java.util.HashMap;

@Getter @Setter
public class SmartScriptMgr {
    private final Map<Integer, SmartScript> scripts = new HashMap<>();

    public void loadScripts() { }
    public SmartScript getScript(int id) { return scripts.get(id); }
}
