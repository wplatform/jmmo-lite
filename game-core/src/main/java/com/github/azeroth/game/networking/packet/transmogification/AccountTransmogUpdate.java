package com.github.azeroth.game.networking.packet.transmogification;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class AccountTransmogUpdate extends ServerPacket {
    public boolean isFullUpdate;
    public boolean isSetFavorite;
    public ArrayList<Integer> favoriteAppearances = new ArrayList<>();
    public ArrayList<Integer> newAppearances = new ArrayList<>();

    public AccountTransmogUpdate() {
        super(ServerOpcode.AccountTransmogUpdate);
    }

    @Override
    public void write() {
        this.writeBit(isFullUpdate);
        this.writeBit(isSetFavorite);
        this.writeInt32(favoriteAppearances.size());
        this.writeInt32(newAppearances.size());

        for (var itemModifiedAppearanceId : favoriteAppearances) {
            this.writeInt32(itemModifiedAppearanceId);
        }

        for (var newAppearance : newAppearances) {
            this.writeInt32(newAppearance);
        }
    }
}
