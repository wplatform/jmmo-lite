package com.github.azeroth.game.networking.packet.guild;


public class PlayerSaveGuildEmblem extends ServerPacket {
    public GuildEmblemerror error = GuildEmblemError.values()[0];

    public PlayerSaveGuildEmblem() {
        super(ServerOpcode.PlayerSaveGuildEmblem);
    }

    @Override
    public void write() {
        this.writeInt32((int) error.getValue());
    }
}
