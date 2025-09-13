package com.github.azeroth.game.networking.packet.guild;


public class GuildCommandResult extends ServerPacket {
    public String name;
    public GuildCommandError result = GuildCommandError.values()[0];
    public GuildcommandType command = GuildCommandType.values()[0];

    public GuildCommandResult() {
        super(ServerOpcode.GuildCommandResult);
    }

    @Override
    public void write() {
        this.writeInt32((int) result.getValue());
        this.writeInt32((int) command.getValue());

        this.writeBits(name.getBytes().length, 8);
        this.writeString(name);
    }
}
