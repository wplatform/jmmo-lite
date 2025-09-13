package com.github.azeroth.game.networking.packet.vignette;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;
import java.util.List;

public final class VignetteUpdate extends ServerPacket {

    public VignetteDataSet added = new VignetteDataSet();
    public VignetteDataSet updated = new VignetteDataSet();
    public List<ObjectGuid> removed = new ArrayList<>();
    public boolean forceUpdate = false;
    public boolean inFogOfWar = false;

    public VignetteUpdate() {
        super(ServerOpCode.SMSG_VIGNETTE_UPDATE,200);
    }

    @Override
    public void write() {
        this.writeBit(forceUpdate);
        this.writeBit(inFogOfWar);
        this.writeInt32(removed.size());
        this.writeVignetteDataSet(added);
        this.writeVignetteDataSet(updated);

        for (ObjectGuid removed : removed)
            this.writeGuid(removed);
    }

    private void writeVignetteDataSet(VignetteDataSet vignetteDataSet) {

        this.writeInt32(vignetteDataSet.IDs.size());
        this.writeInt32(vignetteDataSet.Data.size());

        for (ObjectGuid id : vignetteDataSet.IDs)
            this.writeGuid(id);


        for (VignetteData vignetteData : vignetteDataSet.Data)
            this.writeVignetteData(vignetteData);

    }


    private void writeVignetteData(VignetteData vignetteData) {
        this.writeVector3(vignetteData.position);
        this.writeGuid(vignetteData.ObjGUID);
        this.writeInt32(vignetteData.vignetteID);
        this.writeInt32(vignetteData.ZoneID);
        this.writeInt32(vignetteData.WMOGroupID);
        this.writeInt32(vignetteData.WMODoodadPlacementID);
        this.writeFloat(vignetteData.HealthPercent);
        this.writeInt16(vignetteData.RecommendedGroupSizeMin);
        this.writeInt16(vignetteData.RecommendedGroupSizeMax);
    }
}
