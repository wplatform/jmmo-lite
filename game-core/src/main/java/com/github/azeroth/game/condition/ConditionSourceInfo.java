package com.github.azeroth.game.condition;


import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.Map;

public class ConditionSourceInfo {

    public static final byte MAX_CONDITION_TARGETS = 3;
    public WorldObject[] conditionTargets = new WorldObject[MAX_CONDITION_TARGETS]; // an array of targets available for conditions
    public Map mConditionMap;
    public Condition mLastFailedCondition;


    public ConditionSourceInfo(WorldObject target0, WorldObject target1) {
        this(target0, target1, null);
    }

    public ConditionSourceInfo(WorldObject target0) {
        this(target0, null, null);
    }

    public ConditionSourceInfo(WorldObject target0, WorldObject target1, WorldObject target2) {
        conditionTargets[0] = target0;
        conditionTargets[1] = target1;
        conditionTargets[2] = target2;
        mConditionMap = target0 != null ? target0.getMap() : null;
        mLastFailedCondition = null;
    }

    public ConditionSourceInfo(Map map) {
        mConditionMap = map;
        mLastFailedCondition = null;
    }
}
