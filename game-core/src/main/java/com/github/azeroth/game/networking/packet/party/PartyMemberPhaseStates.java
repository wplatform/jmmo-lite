package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class PartyMemberPhaseStates {
    public int PhaseShiftFlag;
    public ObjectGuid personalGUID = ObjectGuid.EMPTY;
    public ArrayList<PartyMemberPhase> list = new ArrayList<>(5);

    public final void write(WorldPacket data) {
        data.writeInt32(PhaseShiftFlag);
        data.writeInt32(list.size());
        data.writeGuid(personalGUID);

        for (var phase : list) {
            phase.write(data);
        }
    }
}
