package com.github.azeroth.game.garrison;


public class FinalizeGarrisonPlotGOInfo {
    public FactionInfo[] factionInfo = new FactionInfo[2];

    public final static class FactionInfo {
        public int gameObjectId;
        public position pos;
        public short animKitId;

        public FactionInfo clone() {
            FactionInfo varCopy = new FactionInfo();

            varCopy.gameObjectId = this.gameObjectId;
            varCopy.pos = this.pos;
            varCopy.animKitId = this.animKitId;

            return varCopy;
        }
    }
}
