package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.WorldPacket;

public class PlayerGuidLookupData {
    public boolean isDeleted;
    public ObjectGuid accountID = ObjectGuid.EMPTY;
    public ObjectGuid bnetAccountID = ObjectGuid.EMPTY;
    public ObjectGuid guidActual = ObjectGuid.EMPTY;
    public String name = "";
    public long guildClubMemberID; // same as bgs.protocol.club.v1.MemberId.unique_id
    public int virtualRealmAddress;
    public Race raceID = race.NONE;
    public Gender sex = gender.NONE;
    public PlayerClass classID = playerClass.NONE;
    public byte level;
    public byte unused915;
    public declinedName declinedNames = new declinedName();


    public final boolean initialize(ObjectGuid guid) {
        return initialize(guid, null);
    }

    public final boolean initialize(ObjectGuid guid, Player player) {
        var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(guid);

        if (characterInfo == null) {
            return false;
        }

        if (player) {
            accountID = player.getSession().getAccountGUID();
            bnetAccountID = player.getSession().getBattlenetAccountGUID();
            name = player.getName();
            raceID = player.getRace();
            sex = player.getNativeGender();
            classID = player.getClass();
            level = (byte) player.getLevel();

            var names = player.getDeclinedNames();

            if (names != null) {
                declinedNames = names;
            }
        } else {
            var accountId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(guid);
            var bnetAccountId = global.getBNetAccountMgr().getIdByGameAccount(accountId);

            accountID = ObjectGuid.create(HighGuid.wowAccount, accountId);
            bnetAccountID = ObjectGuid.create(HighGuid.BNetAccount, bnetAccountId);
            name = characterInfo.name;
            raceID = characterInfo.raceId;
            sex = characterInfo.sex;
            classID = characterInfo.classId;
            level = characterInfo.level;
        }

        isDeleted = characterInfo.isDeleted;
        guidActual = guid;
        virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();

        return true;
    }

    public final void write(WorldPacket data) {
        data.writeBit(isDeleted);
        data.writeBits(name.getBytes().length, 6);

        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            data.writeBits(declinedNames.name.charAt(i).getBytes().length, 7);
        }

        data.flushBits();

        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            data.writeString(declinedNames.name.charAt(i));
        }

        data.writeGuid(accountID);
        data.writeGuid(bnetAccountID);
        data.writeGuid(guidActual);
        data.writeInt64(guildClubMemberID);
        data.writeInt32(virtualRealmAddress);
        data.writeInt8((byte) raceID.getValue());
        data.writeInt8((byte) sex.getValue());
        data.writeInt8((byte) classID.getValue());
        data.writeInt8(level);
        data.writeInt8(unused915);
        data.writeString(name);
    }
}
