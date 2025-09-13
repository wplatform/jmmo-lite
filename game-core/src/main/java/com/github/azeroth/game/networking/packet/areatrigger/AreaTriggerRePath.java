package com.github.azeroth.game.networking.packet.areatrigger;


import com.github.azeroth.game.networking.ServerPacket;

class AreaTriggerRePath extends ServerPacket {
    public areaTriggerSplineInfo areaTriggerSpline;
    public areaTriggerOrbitInfo areaTriggerOrbit;
    public areaTriggerMovementScriptInfo areaTriggerMovementScript = null;
    public ObjectGuid triggerGUID = ObjectGuid.EMPTY;

    public AreaTriggerRePath() {
        super(ServerOpcode.AreaTriggerRePath);
    }

    @Override
    public void write() {
        this.writeGuid(triggerGUID);

        this.writeBit(areaTriggerSpline != null);
        this.writeBit(areaTriggerOrbit != null);
        this.writeBit(areaTriggerMovementScript != null);
        this.flushBits();

        if (areaTriggerSpline != null) {
            areaTriggerSpline.write(this);
        }

        if (areaTriggerMovementScript != null) {
            areaTriggerMovementScript.getValue().write(this);
        }

        if (areaTriggerOrbit != null) {
            areaTriggerOrbit.write(this);
        }
    }
}
