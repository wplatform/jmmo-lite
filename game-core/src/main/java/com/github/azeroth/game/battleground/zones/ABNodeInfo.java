package com.github.azeroth.game.battleground.zones;


final class ABNodeInfo {
    public int nodeId;
    public int textAllianceAssaulted;
    public int textHordeAssaulted;
    public int textAllianceTaken;
    public int textHordeTaken;
    public int textAllianceDefended;
    public int textHordeDefended;
    public int textAllianceClaims;
    public int textHordeClaims;
    public ABNodeInfo() {
    }
    public ABNodeInfo(int nodeId, int textAllianceAssaulted, int textHordeAssaulted, int textAllianceTaken, int textHordeTaken, int textAllianceDefended, int textHordeDefended, int textAllianceClaims, int textHordeClaims) {
        nodeId = nodeId;
        textAllianceAssaulted = textAllianceAssaulted;
        textHordeAssaulted = textHordeAssaulted;
        textAllianceTaken = textAllianceTaken;
        textHordeTaken = textHordeTaken;
        textAllianceDefended = textAllianceDefended;
        textHordeDefended = textHordeDefended;
        textAllianceClaims = textAllianceClaims;
        textHordeClaims = textHordeClaims;
    }

    public ABNodeInfo clone() {
        ABNodeInfo varCopy = new ABNodeInfo();

        varCopy.nodeId = this.nodeId;
        varCopy.textAllianceAssaulted = this.textAllianceAssaulted;
        varCopy.textHordeAssaulted = this.textHordeAssaulted;
        varCopy.textAllianceTaken = this.textAllianceTaken;
        varCopy.textHordeTaken = this.textHordeTaken;
        varCopy.textAllianceDefended = this.textAllianceDefended;
        varCopy.textHordeDefended = this.textHordeDefended;
        varCopy.textAllianceClaims = this.textAllianceClaims;
        varCopy.textHordeClaims = this.textHordeClaims;

        return varCopy;
    }
}
