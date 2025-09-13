package com.github.azeroth.game.entity.unit;

final class PositionUpdateInfo {
    public boolean relocated;
    public boolean turned;

    public void reset() {
        relocated = false;
        turned = false;
    }

    public PositionUpdateInfo clone() {
        PositionUpdateInfo varCopy = new PositionUpdateInfo();

        varCopy.relocated = this.relocated;
        varCopy.turned = this.turned;

        return varCopy;
    }
}
