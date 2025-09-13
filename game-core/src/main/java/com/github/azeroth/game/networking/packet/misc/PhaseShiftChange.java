package com.github.azeroth.game.networking.packet.misc;


import com.badlogic.gdx.utils.ShortArray;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;


public class PhaseShiftChange extends ServerPacket {
    public ObjectGuid client = ObjectGuid.EMPTY;
    public PhaseShiftData phaseshift = new PhaseShiftData();
    public ShortArray preloadMapIDs = new ShortArray();
    public ShortArray uiMapPhaseIDs = new ShortArray();
    public ShortArray visibleMapIDs = new ShortArray();

    public PhaseShiftChange() {
        super(ServerOpCode.SMSG_PHASE_SHIFT_CHANGE);
    }

    @Override
    public void write() {
        this.writeGuid(client);
        phaseshift.write(this);
        this.writeInt32(visibleMapIDs.size * 2); // size in bytes

        for (var visibleMapId : visibleMapIDs.items) {
            this.writeInt16(visibleMapId); // Active terrain swap map id
        }

        this.writeInt32(preloadMapIDs.size * 2); // size in bytes

        for (var preloadMapId : preloadMapIDs.items) {
            this.writeInt16(preloadMapId); // Inactive terrain swap map id
        }

        this.writeInt32(uiMapPhaseIDs.size * 2); // size in bytes

        for (var uiMapPhaseId : uiMapPhaseIDs.items) {
            this.writeInt16(uiMapPhaseId); // UI map id, WorldMapArea.db2, controls map display
        }
    }
}
