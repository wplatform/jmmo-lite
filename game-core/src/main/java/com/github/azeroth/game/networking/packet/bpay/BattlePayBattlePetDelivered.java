package com.github.azeroth.game.networking.packet.bpay;


public class BattlePayBattlePetDelivered extends ServerPacket {
    private ObjectGuid battlePetGuid = ObjectGuid.EMPTY;
    private int displayID = 0;

    public BattlePayBattlePetDelivered() {
        super(ServerOpcode.BattlePayBattlePetDelivered);
    }

    public final ObjectGuid getBattlePetGuid() {
        return battlePetGuid;
    }

    public final void setBattlePetGuid(ObjectGuid value) {
        battlePetGuid = value;
    }

    public final int getDisplayID() {
        return displayID;
    }

    public final void setDisplayID(int value) {
        displayID = value;
    }


	/*WorldPacket const* WorldPackets::BattlePay::PurchaseDetails::Write()
	{
		this << unkInt;
		this << VasPurchaseProgress;
		this << unkLong;

		this.writeBits(key.length(), 6);
		this.writeBits(Key2.length(), 6);
		this.writeString(key);
		this.writeString(Key2);

		return &this;
	}*/

	/*WorldPacket const* WorldPackets::BattlePay::PurchaseUnk::Write()
	{
		this << unkByte;
		this << unkInt;

		this.writeBits(key.length(), 7);
		this.writeString(key);

		return &this;
	}*/

    @Override
    public void write() {
        this.write(getDisplayID());
        this.write(getBattlePetGuid());
    }
}
