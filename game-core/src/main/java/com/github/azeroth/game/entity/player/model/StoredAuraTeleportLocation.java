package com.github.azeroth.game.entity.player.model;

import com.github.azeroth.game.domain.object.WorldLocation;

public class StoredAuraTeleportLocation {
    private WorldLocation loc;
    private State currentState;

    public final WorldLocation getLoc() {
        return loc;
    }

    public final void setLoc(WorldLocation value) {
        loc = value;
    }

    public final State getCurrentState() {
        return currentState;
    }

    public final void setCurrentState(State value) {
        currentState = value;
    }

    public enum State {
        Unchanged,
        changed,
        Deleted
    }
}
