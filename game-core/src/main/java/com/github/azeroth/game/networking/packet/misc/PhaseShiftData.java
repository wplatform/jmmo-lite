package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class PhaseShiftData {

    public int PhaseShiftFlag;
    public ArrayList<PhaseShiftDataPhase> phases = new ArrayList<>();
    public ObjectGuid personalGUID = ObjectGuid.EMPTY;

    public final void write(WorldPacket data) {
        data.writeInt32(PhaseShiftFlag);
        data.writeInt32(phases.size());
        data.writeGuid(personalGUID);

        for (var phaseShiftDataPhase : phases) {
            phaseShiftDataPhase.write(data);
        }
    }
}
