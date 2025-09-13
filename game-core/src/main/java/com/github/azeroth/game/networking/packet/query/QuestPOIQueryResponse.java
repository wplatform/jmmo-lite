package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.QuestPOIData;
import game.WorldConfig;

import java.util.ArrayList;


public class QuestPOIQueryResponse extends ServerPacket {
    public ArrayList<QuestPOIData> questPOIDataStats = new ArrayList<>();

    public QuestPOIQueryResponse() {
        super(ServerOpcode.QuestPoiQueryResponse);
    }

    @Override
    public void write() {
        this.writeInt32(questPOIDataStats.size());
        this.writeInt32(questPOIDataStats.size());

        var useCache = WorldConfig.getBoolValue(WorldCfg.CacheDataQueries);

        for (var questPOIData : questPOIDataStats) {
            if (useCache) {
                this.writeBytes(questPOIData.queryDataBuffer);
            } else {
                questPOIData.write(this);
            }
        }
    }
}
