package com.github.azeroth.game.entity.object.update;

import lombok.Getter;

@Getter
public final class ZonePlayerForcedReaction extends UpdateMaskObject {

    private int factionId;
    private int reaction;

    public ZonePlayerForcedReaction() {
        super(3);
    }

    public void setFactionId(int factionId) {
        if (factionId != this.factionId) {
            this.factionId = factionId;
            fireMarkChanged(0, 1);
        }
    }

    public void setReaction(int reaction) {
        if (reaction != this.reaction) {
            this.reaction = reaction;
            fireMarkChanged(0, 2);
        }
    }

    @Override
    public void clearChangesMask() {

    }
}
