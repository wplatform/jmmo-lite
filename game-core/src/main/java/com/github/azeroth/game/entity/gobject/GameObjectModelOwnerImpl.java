package com.github.azeroth.game.entity.gobject;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.map.model.GameObjectModelOwnerBase;
import com.github.azeroth.game.domain.phasing.PhaseShift;

class GameObjectModelOwnerImpl extends GameObjectModelOwnerBase {
    private final GameObject owner;

    public GameObjectModelOwnerImpl(GameObject owner) {
        this.owner = owner;
    }

    @Override
    public boolean isSpawned() {
        return owner.isSpawned();
    }

    @Override
    public int getDisplayId() {
        return owner.getDisplayId();
    }

    @Override
    public byte getNameSetId() {
        return owner.getNameSetId();
    }


    @Override
    public boolean isInPhase(PhaseShift phaseShift) {
        return owner.getPhaseShift().canSee(phaseShift);
    }

    @Override
    public Vector3 getPosition() {
        return new Vector3(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ());
    }

    @Override
    public Quaternion getRotation() {
        return new Quaternion(owner.getLocalRotation().x, owner.getLocalRotation().y, owner.getLocalRotation().z, owner.getLocalRotation().w);
    }

    @Override
    public float getScale() {
        return owner.getObjectScale();
    }
}
