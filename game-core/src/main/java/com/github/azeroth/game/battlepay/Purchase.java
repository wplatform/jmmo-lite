package com.github.azeroth.game.battlepay;

public class Purchase {
    public ObjectGuid targetCharacter = ObjectGuid.EMPTY;
    public long distributionId;
    public long purchaseID;
    public long currentPrice;
    public int clientToken;
    public int serverToken;
    public int productID;
    public short status;
    public boolean lock;
}
