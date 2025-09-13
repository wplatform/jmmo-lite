package com.github.azeroth.game.networking.packet.scenario;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class ScenarioPOIs extends ServerPacket {
    public ArrayList<ScenarioPOIData> scenarioPOIDataStats = new ArrayList<>();

    public scenarioPOIs() {
        super(ServerOpcode.ScenarioPois);
    }

    @Override
    public void write() {
        this.writeInt32(scenarioPOIDataStats.size());

        for (var scenarioPOIData : scenarioPOIDataStats) {
            this.writeInt32(scenarioPOIData.criteriaTreeID);
            this.writeInt32(scenarioPOIData.scenarioPOIs.size());

            for (var scenarioPOI : scenarioPOIData.scenarioPOIs) {
                this.writeInt32(scenarioPOI.blobIndex);
                this.writeInt32(scenarioPOI.mapID);
                this.writeInt32(scenarioPOI.uiMapID);
                this.writeInt32(scenarioPOI.priority);
                this.writeInt32(scenarioPOI.flags);
                this.writeInt32(scenarioPOI.worldEffectID);
                this.writeInt32(scenarioPOI.playerConditionID);
                this.writeInt32(scenarioPOI.navigationPlayerConditionID);
                this.writeInt32(scenarioPOI.points.size());

                for (var scenarioPOIBlobPoint : scenarioPOI.points) {
                    this.writeInt32((int) scenarioPOIBlobPoint.X);
                    this.writeInt32((int) scenarioPOIBlobPoint.Y);
                    this.writeInt32((int) scenarioPOIBlobPoint.Z);
                }
            }
        }
    }
}
