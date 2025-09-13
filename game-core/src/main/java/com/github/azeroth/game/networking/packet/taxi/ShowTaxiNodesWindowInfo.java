package com.github.azeroth.game.networking.packet.taxi;

public final class ShowTaxiNodesWindowInfo {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public int currentNode;

    public ShowTaxiNodesWindowInfo clone() {
        ShowTaxiNodesWindowInfo varCopy = new ShowTaxiNodesWindowInfo();

        varCopy.unitGUID = this.unitGUID;
        varCopy.currentNode = this.currentNode;

        return varCopy;
    }
}
