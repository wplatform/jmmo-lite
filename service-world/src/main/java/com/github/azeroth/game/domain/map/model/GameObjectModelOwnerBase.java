package com.github.azeroth.game.domain.map.model;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.phasing.PhaseShift;

public abstract class GameObjectModelOwnerBase {
    public abstract boolean isSpawned();


    public abstract int getDisplayId();


    public abstract byte getNameSetId();

    public abstract boolean isInPhase(PhaseShift phaseShift);

    public abstract Vector3 getPosition();

    public abstract Quaternion getRotation();

    public abstract float getScale();
}
