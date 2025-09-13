package com.github.azeroth.game.pvp;


import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.listener.ScriptObjectAutoAddDBBound;
import com.github.azeroth.game.listener.interfaces.ioutdoorpvp.IOutdoorPvPGetOutdoorPvP;


class OutdoorPvP_hellfire_peninsula extends ScriptObjectAutoAddDBBound implements IOutdoorPvPGetOutdoorPvP {
    public OutdoorPvP_hellfire_peninsula() {
        super("outdoorpvp_hp");
    }

    public final OutdoorPvP getOutdoorPvP(Map map) {
        return new HellfirePeninsulaPvP(map);
    }
}
