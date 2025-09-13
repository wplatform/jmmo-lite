package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class DisplayToast extends ServerPacket {
    public long quantity;
    public displayToastMethod displayToastMethod = Framework.Constants.displayToastMethod.values()[0];
    public boolean mailed;
    public DisplayToasttype type = DisplayToastType.money;
    public int questID;
    public boolean isSecondaryResult;
    public itemInstance item;
    public boolean bonusRoll;
    public int lootSpec;
    public int currencyID;    public gender gender = gender.NONE;
    public DisplayToast() {
        super(ServerOpcode.DisplayToast);
    }

    @Override
    public void write() {
        this.writeInt64(quantity);
        this.writeInt8((byte) displayToastMethod.getValue());
        this.writeInt32(questID);

        this.writeBit(mailed);
        this.writeBits((byte) type.getValue(), 2);
        this.writeBit(isSecondaryResult);

        switch (type) {
            case NewItem:
                this.writeBit(bonusRoll);
                item.write(this);
                this.writeInt32(lootSpec);
                this.writeInt32(gender.getValue());

                break;
            case NewCurrency:
                this.writeInt32(currencyID);

                break;
            default:
                break;
        }

        this.flushBits();
    }


}
