package com.github.azeroth.game.networking.packet.spell;


public class UpdateActionButtons extends ServerPacket {
    public long[] actionButtons = new long[PlayerConst.MaxActionButtons];
    public byte reason;

    public UpdateActionButtons() {
        super(ServerOpcode.UpdateActionButtons);
    }

    @Override
    public void write() {
        for (var i = 0; i < PlayerConst.MaxActionButtons; ++i) {
            this.writeInt64(ActionButtons[i]);
        }

        this.writeInt8(reason);
    }
}
