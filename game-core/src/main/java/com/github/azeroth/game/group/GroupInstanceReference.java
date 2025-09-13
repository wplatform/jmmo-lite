package com.github.azeroth.game.group;


import com.github.azeroth.game.map.InstanceMap;
import com.github.azeroth.reference.Reference;


public class GroupInstanceReference extends Reference<PlayerGroup, InstanceMap, GroupInstanceReference> {


    @Override
    protected GroupInstanceReference self() {
        return null;
    }
}
