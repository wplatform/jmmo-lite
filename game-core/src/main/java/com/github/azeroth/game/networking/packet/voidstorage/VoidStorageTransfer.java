package com.github.azeroth.game.networking.packet.voidstorage;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class VoidStorageTransfer extends ClientPacket {
    public ObjectGuid[] withdrawals = new ObjectGuid[(int) SharedConst.VoidStorageMaxWithdraw];
    public ObjectGuid[] deposits = new ObjectGuid[(int) SharedConst.VoidStorageMaxDeposit];
    public ObjectGuid npc = ObjectGuid.EMPTY;

    public VoidStorageTransfer(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npc = this.readPackedGuid();
        var DepositCount = this.readInt32();
        var WithdrawalCount = this.readInt32();

        for (int i = 0; i < DepositCount; ++i) {
            Deposits[i] = this.readPackedGuid();
        }

        for (int i = 0; i < WithdrawalCount; ++i) {
            Withdrawals[i] = this.readPackedGuid();
        }
    }
}
