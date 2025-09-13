package com.github.azeroth.game;


public class GameEventFinishCondition {
    public float reqNum; // required number // use float, since some events use percent
    public float done; // done number
    public int max_world_state; // max resource count world state update id
    public int done_world_state; // done resource count world state update id
}
