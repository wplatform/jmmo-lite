package com.github.azeroth.game.networking.packet.guild;


public class GuildMemberUpdateNote extends ServerPacket {
    public ObjectGuid member = ObjectGuid.EMPTY;
    public boolean isPublic; // 0 == officer, 1 == Public
    public String note;

    public GuildMemberUpdateNote() {
        super(ServerOpcode.GuildMemberUpdateNote);
    }

    @Override
    public void write() {
        this.writeGuid(member);

        this.writeBits(note.getBytes().length, 8);
        this.writeBit(isPublic);
        this.flushBits();

        this.writeString(note);
    }
}
