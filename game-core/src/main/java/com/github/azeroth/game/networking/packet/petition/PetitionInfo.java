package com.github.azeroth.game.networking.packet.petition;


import com.github.azeroth.game.networking.WorldPacket;

public class PetitionInfo {
    public int petitionID;
    public ObjectGuid petitioner = ObjectGuid.EMPTY;
    public String title;
    public String bodyText;
    public int minSignatures;
    public int maxSignatures;
    public int deadLine;
    public int issueDate;
    public int allowedGuildID;
    public int allowedClasses;
    public int allowedRaces;
    public short allowedGender;
    public int allowedMinLevel;
    public int allowedMaxLevel;
    public int numChoices;
    public int staticType;
    public int muid = 0;
    public LocalizedString choicetext = new LocalizedString();

    public final void write(WorldPacket data) {
        data.writeInt32(petitionID);
        data.writeGuid(petitioner);

        data.writeInt32(minSignatures);
        data.writeInt32(maxSignatures);
        data.writeInt32(deadLine);
        data.writeInt32(issueDate);
        data.writeInt32(allowedGuildID);
        data.writeInt32(allowedClasses);
        data.writeInt32(allowedRaces);
        data.writeInt16(allowedGender);
        data.writeInt32(allowedMinLevel);
        data.writeInt32(allowedMaxLevel);
        data.writeInt32(numChoices);
        data.writeInt32(staticType);
        data.writeInt32(muid);

        data.writeBits(title.getBytes().length, 7);
        data.writeBits(bodyText.getBytes().length, 12);

        for (byte i = 0; i < choicetext.length; i++) {
            data.writeBits(choicetext.get(i).getBytes().length, 6);
        }

        data.flushBits();

        for (byte i = 0; i < choicetext.length; i++) {
            data.writeString(choicetext.get(i));
        }

        data.writeString(title);
        data.writeString(bodyText);
    }
}
