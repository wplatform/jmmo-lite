package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.movement.movementForce;

import java.util.ArrayList;


public class MoveUpdateTeleport extends ServerPacket {
    public MovementInfo status;
    public ArrayList<movementForce> movementForces;
    public Float swimBackSpeed = null;
    public Float flightSpeed = null;
    public Float swimSpeed = null;
    public Float walkSpeed = null;
    public Float turnRate = null;
    public Float runSpeed = null;
    public Float flightBackSpeed = null;
    public Float runBackSpeed = null;
    public Float pitchRate = null;

    public MoveUpdateTeleport() {
        super(ServerOpcode.MoveUpdateTeleport);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);

        this.writeInt32(movementForces != null ? movementForces.size() : 0);
        this.writeBit(walkSpeed != null);
        this.writeBit(runSpeed != null);
        this.writeBit(runBackSpeed != null);
        this.writeBit(swimSpeed != null);
        this.writeBit(swimBackSpeed != null);
        this.writeBit(flightSpeed != null);
        this.writeBit(flightBackSpeed != null);
        this.writeBit(turnRate != null);
        this.writeBit(pitchRate != null);
        this.flushBits();

        if (movementForces != null) {
            for (var force : movementForces) {
                force.write(this);
            }
        }

        if (walkSpeed != null) {
            this.writeFloat(walkSpeed.floatValue());
        }

        if (runSpeed != null) {
            this.writeFloat(runSpeed.floatValue());
        }

        if (runBackSpeed != null) {
            this.writeFloat(runBackSpeed.floatValue());
        }

        if (swimSpeed != null) {
            this.writeFloat(swimSpeed.floatValue());
        }

        if (swimBackSpeed != null) {
            this.writeFloat(swimBackSpeed.floatValue());
        }

        if (flightSpeed != null) {
            this.writeFloat(flightSpeed.floatValue());
        }

        if (flightBackSpeed != null) {
            this.writeFloat(flightBackSpeed.floatValue());
        }

        if (turnRate != null) {
            this.writeFloat(turnRate.floatValue());
        }

        if (pitchRate != null) {
            this.writeFloat(pitchRate.floatValue());
        }
    }
}
