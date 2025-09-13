package com.github.azeroth.game.server;// C# TO JAVA CONVERTER WARNING: Java does not allow user-defined second types. The behavior of this class may differ from the original:

public final class AccountData {
    public long time;
    public String data;

    public AccountData clone() {
        AccountData varCopy = new AccountData();

        varCopy.time = this.time;
        varCopy.data = this.data;

        return varCopy;
    }
}
