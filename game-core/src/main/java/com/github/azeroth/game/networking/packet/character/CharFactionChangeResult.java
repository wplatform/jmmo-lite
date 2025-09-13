package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ServerPacket;

public class CharFactionChangeResult extends ServerPacket {
    public ResponseCodes result = ResponseCodes.forValue(0);
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public CharFactionChangedisplayInfo display;

    public CharFactionChangeResult() {
        super(ServerOpcode.CharFactionChangeResult);
    }

    @Override
    public void write() {
        this.writeInt8((byte) result.getValue());
        this.writeGuid(UUID);
        this.writeBit(display != null);
        this.flushBits();

        if (display != null) {
            this.writeBits(display.name.getBytes().length, 6);
            this.writeInt8(display.sexID);
            this.writeInt8(display.raceID);
            this.writeInt32(display.customizations.size());
            this.writeString(display.name);

            for (var customization : display.customizations) {
                this.writeInt32(customization.chrCustomizationOptionID);
                this.writeInt32(customization.chrCustomizationChoiceID);
            }
        }
    }

    public static class CharFactionChangeDisplayInfo {
        public String name;

        public byte sexID;

        public byte raceID;
        public Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);
    }
}
