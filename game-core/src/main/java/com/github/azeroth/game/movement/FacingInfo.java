package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.enums.MonsterMoveType;

public class FacingInfo {
    public Vector3 f;
    public ObjectGuid target;
    public float angle;
    public MonsterMoveType type;
}
