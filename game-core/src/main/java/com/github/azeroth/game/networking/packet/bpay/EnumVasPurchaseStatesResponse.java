package com.github.azeroth.game.networking.packet.bpay;


public class EnumVasPurchaseStatesResponse extends ServerPacket {

    private byte result = 0;

    public EnumVasPurchaseStatesResponse() {
        super(ServerOpcode.EnumVasPurchaseStatesResponse);
    }


    public final byte getResult() {
        return result;
    }


    public final void setResult(byte value) {
        result = value;
    }


	/*WorldPacket const* WorldPackets::BattlePay::BattlePayVasPurchaseStarted::Write()
	{
		this << unkInt;
		this << VasPurchase;

		return &this;
	}*/

	/*WorldPacket const* WorldPackets::BattlePay::CharacterClassTrialCreate::Write()
	{
		this << result;
		return &this;
	}*/

	/*WorldPacket const* WorldPackets::BattlePay::BattlePayCharacterUpgradeQueued::Write()
	{
		this << Character;
		this << static_cast<uint32>(EquipmentItems.size());
		for (auto const& item : EquipmentItems)
			this << item;

		return &this;
	}*/

	/*void WorldPackets::BattlePay::BattlePayTrialBoostCharacter::Read()
	{
		this >> Character;
		this >> specializationID;
	}*/

	/*WorldPacket const* WorldPackets::BattlePay::BattlePayVasPurchaseList::Write()
	{
		this.writeBits(VasPurchase.size(), 6);
		this.flushBits();
		for (auto const& itr : VasPurchase)
			this << itr;

		return &this;
	}*/

    @Override
    public void write() {
        this.writeBits(getResult(), 2);
    }
}
