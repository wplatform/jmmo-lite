package com.github.azeroth.game.networking.packet.bpay;


public class DisplayPromotion extends ServerPacket {

    private int promotionID = 0;


    public DisplayPromotion(int ID) {
        super(ServerOpcode.DisplayPromotion);
        setPromotionID(ID);
    }


    public final int getPromotionID() {
        return promotionID;
    }


    public final void setPromotionID(int value) {
        promotionID = value;
    }


	/*void WorldPackets::BattlePay::PurchaseDetailsResponse::Read()
	{
		this >> unkByte;
	}*/

	/*/
	void WorldPackets::BattlePay::PurchaseUnkResponse::Read()
	{
		auto keyLen = this.readBit(6);
		auto key2Len = this.readBit(7);
		key = this.readString(keyLen);
		Key2 = this.readString(key2Len);
	}*/

    @Override
    public void write() {
        this.write(getPromotionID());
    }
}
