package com.github.azeroth.game.garrison;


import com.github.azeroth.game.domain.object.Position;

public class FinalizeGarrisonPlotGOInfo {
    public FactionInfo[] factionInfo = new FactionInfo[2];

    public final static class FactionInfo {
        public int gameObjectId;
        public Position pos;
        public short animKitId;
    }
}
