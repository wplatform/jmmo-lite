package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.entity.player.CufProfile;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class SaveCUFProfiles extends ClientPacket {
    public ArrayList<CufProfile> CUFProfiles = new ArrayList<>();

    public SaveCUFProfiles(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var count = this.readUInt32();

        for (byte i = 0; i < count && i < PlayerConst.MaxCUFProfiles; i++) {
            CufProfile cufProfile = new CufProfile();

            var strLen = this.<Byte>readBit(7);

            // Bool Options
            for (byte option = 0; option < CUFBoolOptions.BoolOptionsCount.getValue(); option++) {
                cufProfile.getBoolOptions().set(option, this.readBit());
            }

            // Other Options
            cufProfile.setFrameHeight(this.readUInt16());
            cufProfile.setFrameWidth(this.readUInt16());

            cufProfile.setSortBy(this.readUInt8());
            cufProfile.setHealthText(this.readUInt8());

            cufProfile.setTopPoint(this.readUInt8());
            cufProfile.setBottomPoint(this.readUInt8());
            cufProfile.setLeftPoint(this.readUInt8());

            cufProfile.setTopOffset(this.readUInt16());
            cufProfile.setBottomOffset(this.readUInt16());
            cufProfile.setLeftOffset(this.readUInt16());

            cufProfile.setProfileName(this.readString(strLen));

            CUFProfiles.add(cufProfile);
        }
    }
}
