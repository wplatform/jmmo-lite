package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.entity.player.CufProfile;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LoadCUFProfiles extends ServerPacket {
    public ArrayList<CufProfile> CUFProfiles = new ArrayList<>();

    public LoadCUFProfiles() {
        super(ServerOpcode.LoadCufProfiles);
    }

    @Override
    public void write() {
        this.writeInt32(CUFProfiles.size());

        for (var cufProfile : CUFProfiles) {
            this.writeBits(cufProfile.getProfileName().getBytes().length, 7);

            // Bool Options
            for (byte option = 0; option < CUFBoolOptions.BoolOptionsCount.getValue(); option++) {
                this.writeBit(cufProfile.getBoolOptions()[option]);
            }

            // Other Options
            this.writeInt16(cufProfile.getFrameHeight());
            this.writeInt16(cufProfile.getFrameWidth());

            this.writeInt8(cufProfile.getSortBy());
            this.writeInt8(cufProfile.getHealthText());

            this.writeInt8(cufProfile.getTopPoint());
            this.writeInt8(cufProfile.getBottomPoint());
            this.writeInt8(cufProfile.getLeftPoint());

            this.writeInt16(cufProfile.getTopOffset());
            this.writeInt16(cufProfile.getBottomOffset());
            this.writeInt16(cufProfile.getLeftOffset());

            this.writeString(cufProfile.getProfileName());
        }
    }
}
