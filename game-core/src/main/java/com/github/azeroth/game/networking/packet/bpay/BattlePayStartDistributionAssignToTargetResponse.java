package com.github.azeroth.game.networking.packet.bpay;


public class BattlePayStartDistributionAssignToTargetResponse extends ServerPacket {

    private long distributionID = 0;

    private int unkint1 = 0;

    private int unkint2 = 0;

    public BattlePayStartDistributionAssignToTargetResponse() {
        super(ServerOpcode.BattlePayStartDistributionAssignToTargetResponse);
    }


    public final long getDistributionID() {
        return distributionID;
    }


    public final void setDistributionID(long value) {
        distributionID = value;
    }


    public final int getUnkint1() {
        return unkint1;
    }


    public final void setUnkint1(int value) {
        unkint1 = value;
    }


    public final int getUnkint2() {
        return unkint2;
    }


    public final void setUnkint2(int value) {
        unkint2 = value;
    }


	/*WorldPacket const* WorldPackets::BattlePay::BattlepayUnk::Write()
	{
		this << unkInt;

		return &this;
	}*/

    @Override
    public void write() {
        this.write(getDistributionID());
        this.write(getUnkint1());
        this.write(getUnkint2());
    }
}
