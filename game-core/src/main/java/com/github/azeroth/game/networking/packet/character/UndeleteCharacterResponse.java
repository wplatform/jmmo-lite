package com.github.azeroth.game.networking.packet.character;


public class UndeleteCharacterResponse extends ServerPacket {
    public CharacterundeleteInfo undeleteInfo;
    public CharacterUndeleteresult result = CharacterUndeleteResult.values()[0];

    public UndeleteCharacterResponse() {
        super(ServerOpcode.UndeleteCharacterResponse);
    }

    @Override
    public void write() {
        this.writeInt32(undeleteInfo.clientToken);
        this.writeInt32((int) result.getValue());
        this.writeGuid(undeleteInfo.characterGuid);
    }
}
