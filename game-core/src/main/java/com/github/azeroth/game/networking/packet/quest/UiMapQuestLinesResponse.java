package com.github.azeroth.game.networking.packet.quest;


import java.util.ArrayList;


public class UiMapQuestLinesResponse extends ServerPacket {
    public int uiMapID;
    public ArrayList<Integer> questLineXQuestIDs = new ArrayList<>();

    public UiMapQuestLinesResponse() {
        super(ServerOpcode.UiMapQuestLinesResponse);
    }

    @Override
    public void write() {
        this.write(uiMapID);
        this.writeInt32((int) questLineXQuestIDs.size());

        for (var item : questLineXQuestIDs) {
            this.write(item);
        }
    }
}
