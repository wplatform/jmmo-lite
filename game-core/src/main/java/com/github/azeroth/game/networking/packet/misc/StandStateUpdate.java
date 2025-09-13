package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;

public class StandStateUpdate extends ServerPacket {

    private final int animKitID;
    private final UnitStandstateType state;


    public StandStateUpdate(UnitStandStateType state, int animKitId) {
        super(ServerOpcode.StandStateUpdate);
        state = state;
        animKitID = animKitId;
    }

    @Override
    public void write() {
        this.writeInt32(animKitID);
        this.writeInt8((byte) state.getValue());
    }
}
