package com.github.azeroth.game.dungeonfinding;


import java.util.HashMap;

public class LfgCompatibilityData {
    public LfgCompatibility compatibility = LfgCompatibility.values()[0];
    public HashMap<ObjectGuid, LfgRoles> roles;

    public LfgCompatibilityData() {
        compatibility = LfgCompatibility.Pending;
    }

    public LfgCompatibilityData(LfgCompatibility _compatibility) {
        compatibility = _compatibility;
    }

    public LfgCompatibilityData(LfgCompatibility _compatibility, HashMap<ObjectGuid, LfgRoles> _roles) {
        compatibility = _compatibility;
        roles = _roles;
    }
}
