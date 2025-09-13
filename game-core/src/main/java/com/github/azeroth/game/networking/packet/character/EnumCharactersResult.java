package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class EnumCharactersResult extends ServerPacket {
    public boolean success;
    public boolean isDeletedCharacters; // used for character undelete list
    public boolean isNewPlayerRestrictionSkipped; // allows client to skip new player restrictions
    public boolean isNewPlayerRestricted; // forbids using level boost and class trials
    public boolean isNewPlayer; // forbids hero classes and allied races
    public boolean isTrialAccountRestricted;
    public boolean isAlliedRacesCreationAllowed;

    public int maxCharacterLevel = 1;
    public integer disabledClassesMask = new integer();

    public ArrayList<CharacterInfo> characters = new ArrayList<>(); // all character on the list
    public ArrayList<RaceUnlock> raceUnlockData = new ArrayList<>();
    public ArrayList<UnlockedConditionalAppearance> unlockedConditionalAppearances = new ArrayList<>();
    public ArrayList<RaceLimitDisableInfo> raceLimitDisables = new ArrayList<>();

    public EnumCharactersResult() {
        super(ServerOpcode.EnumCharactersResult);
    }

    @Override
    public void write() {
        this.writeBit(success);
        this.writeBit(isDeletedCharacters);
        this.writeBit(isNewPlayerRestrictionSkipped);
        this.writeBit(isNewPlayerRestricted);
        this.writeBit(isNewPlayer);
        this.writeBit(isTrialAccountRestricted);
        this.writeBit(disabledClassesMask != null);
        this.writeBit(isAlliedRacesCreationAllowed);
        this.writeInt32(characters.size());
        this.writeInt32(maxCharacterLevel);
        this.writeInt32(raceUnlockData.size());
        this.writeInt32(unlockedConditionalAppearances.size());
        this.writeInt32(raceLimitDisables.size());

        if (disabledClassesMask != null) {
            this.writeInt32(disabledClassesMask.intValue());
        }

        for (var unlockedConditionalAppearance : unlockedConditionalAppearances) {
            unlockedConditionalAppearance.write(this);
        }

        for (var raceLimitDisableInfo : raceLimitDisables) {
            raceLimitDisableInfo.write(this);
        }

        for (var charInfo : characters) {
            charInfo.write(this);
        }

        for (var raceUnlock : raceUnlockData) {
            raceUnlock.write(this);
        }
    }

    public static class CharacterInfo {
        public ObjectGuid UUID = ObjectGuid.EMPTY;
        public long guildClubMemberID; // same as bgs.protocol.club.v1.MemberId.unique_id, guessed basing on SMSG_QUERY_PLAYER_NAME_RESPONSE (that one is known)
        public String name;
        public byte listPosition; // Order of the character in list
        public byte raceId;
        public PlayerClass classId = playerClass.values()[0];
        public byte sexId;
        public Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);
        public byte experienceLevel;
        public int zoneId;
        public int mapId;
        public Vector3 preloadPos;
        public ObjectGuid guildGuid = ObjectGuid.EMPTY;
        public Characterflags flags = CharacterFlags.values()[0]; // Character flag @see enum CharacterFlags
        public CharacterCustomizeFlags flags2 = CharacterCustomizeFlags.values()[0]; // Character customization flags @see enum CharacterCustomizeFlags
        public int flags3; // Character flags 3 @todo research
        public int flags4;
        public boolean firstLogin;
        public byte unkWod61x;
        public long lastPlayedTime;
        public short specID;
        public int unknown703;
        public int lastLoginVersion;
        public int overrideSelectScreenFileDataID;
        public int petCreatureDisplayId;
        public int petExperienceLevel;
        public int petCreatureFamilyId;
        public boolean boostInProgress; // @todo
        public int[] professionIds = new int[2]; // @todo
        public VisualItemInfo[] visualItems = new VisualItemInfo[InventorySlots.ReagentBagEnd];
        public ArrayList<String> mailSenders = new ArrayList<>();
        public ArrayList<Integer> mailSenderTypes = new ArrayList<>();

        public CharacterInfo(SQLFields fields) {
            UUID = ObjectGuid.create(HighGuid.Player, fields.<Long>Read(0));
            name = fields.<String>Read(1);
            raceId = fields.<Byte>Read(2);
            classId = playerClass.forValue(fields.<Byte>Read(3));
            sexId = fields.<Byte>Read(4);
            experienceLevel = fields.<Byte>Read(5);
            zoneId = fields.<Integer>Read(6);
            mapId = fields.<Integer>Read(7);
            preloadPos = new Vector3(fields.<Float>Read(8), fields.<Float>Read(9), fields.<Float>Read(10));

            var guildId = fields.<Long>Read(11);

            if (guildId != 0) {
                guildGuid = ObjectGuid.create(HighGuid.Guild, guildId);
            }

            var playerFlags = playerFlags.forValue(fields.<Integer>Read(12));
            var atLoginFlags = AtLoginFlags.forValue(fields.<SHORT>Read(13));

            if (atLoginFlags.hasFlag(AtLoginFlags.Resurrect)) {
                playerFlags = playerFlags.forValue(playerFlags.getValue() & ~playerFlags.Ghost.getValue());
            }

            if (playerFlags.hasFlag(playerFlags.Ghost)) {
                flags = CharacterFlags.forValue(flags.getValue() | CharacterFlags.Ghost.getValue());
            }

            if (atLoginFlags.hasFlag(AtLoginFlags.Rename)) {
                flags = CharacterFlags.forValue(flags.getValue() | CharacterFlags.Rename.getValue());
            }

            if (fields.<Integer>Read(18) != 0) {
                flags = CharacterFlags.forValue(flags.getValue() | CharacterFlags.LockedByBilling.getValue());
            }

            if (WorldConfig.getBoolValue(WorldCfg.DeclinedNamesUsed) && !StringUtil.isEmpty(fields.<String>Read(23))) {
                flags = CharacterFlags.forValue(flags.getValue() | CharacterFlags.Declined.getValue());
            }

            if (atLoginFlags.hasFlag(AtLoginFlags.Customize)) {
                flags2 = CharacterCustomizeFlags.Customize;
            } else if (atLoginFlags.hasFlag(AtLoginFlags.ChangeFaction)) {
                flags2 = CharacterCustomizeFlags.faction;
            } else if (atLoginFlags.hasFlag(AtLoginFlags.ChangeRace)) {
                flags2 = CharacterCustomizeFlags.race;
            }

            flags3 = 0;
            flags4 = 0;
            firstLogin = atLoginFlags.hasFlag(AtLoginFlags.firstLogin);

            // show pet at selection character in character list only for non-ghost character
            if (!playerFlags.hasFlag(playerFlags.Ghost) && (classId == playerClass.Warlock || classId == playerClass.Hunter || classId == playerClass.Deathknight)) {
                var creatureInfo = global.getObjectMgr().getCreatureTemplate(fields.<Integer>Read(14));

                if (creatureInfo != null) {
                    petCreatureDisplayId = fields.<Integer>Read(15);
                    petExperienceLevel = fields.<SHORT>Read(16);
                    petCreatureFamilyId = (int) creatureInfo.family.getValue();
                }
            }

            boostInProgress = false;
            ProfessionIds[0] = 0;
            ProfessionIds[1] = 0;

            StringArguments equipment = new StringArguments(fields.<String>Read(17));
            listPosition = fields.<Byte>Read(19);
            lastPlayedTime = fields.<Long>Read(20);

            var spec = global.getDB2Mgr().GetChrSpecializationByIndex(classId, fields.<Byte>Read(21));

            if (spec != null) {
                specID = (short) spec.id;
            }

            lastLoginVersion = fields.<Integer>Read(22);

            for (byte slot = 0; slot < InventorySlots.ReagentBagEnd; ++slot) {
                VisualItems[slot].invType = (byte) equipment.NextUInt32(" ");
                VisualItems[slot].displayId = equipment.NextUInt32(" ");
                VisualItems[slot].displayEnchantId = equipment.NextUInt32(" ");
                VisualItems[slot].subclass = (byte) equipment.NextUInt32(" ");
                VisualItems[slot].secondaryItemModifiedAppearanceID = equipment.NextUInt32(" ");
            }
        }

        public final void write(WorldPacket data) {
            data.writeGuid(UUID);
            data.writeInt64(guildClubMemberID);
            data.writeInt8(listPosition);
            data.writeInt8(raceId);
            data.writeInt8((byte) classId.getValue());
            data.writeInt8(sexId);
            data.writeInt32(customizations.size());

            data.writeInt8(experienceLevel);
            data.writeInt32(zoneId);
            data.writeInt32(mapId);
            data.writeVector3(preloadPos);
            data.writeGuid(guildGuid);
            data.writeInt32((int) flags.getValue());
            data.writeInt32((int) flags2.getValue());
            data.writeInt32(flags3);
            data.writeInt32(petCreatureDisplayId);
            data.writeInt32(petExperienceLevel);
            data.writeInt32(petCreatureFamilyId);

            data.writeInt32(ProfessionIds[0]);
            data.writeInt32(ProfessionIds[1]);

            for (var visualItem : visualItems) {
                visualItem.write(data);
            }

            data.writeInt64(lastPlayedTime);
            data.writeInt16(specID);
            data.writeInt32(unknown703);
            data.writeInt32(lastLoginVersion);
            data.writeInt32(flags4);
            data.writeInt32(mailSenders.size());
            data.writeInt32(mailSenderTypes.size());
            data.writeInt32(overrideSelectScreenFileDataID);

            for (var customization : customizations) {
                data.writeInt32(customization.chrCustomizationOptionID);
                data.writeInt32(customization.chrCustomizationChoiceID);
            }

            for (var mailSenderType : mailSenderTypes) {
                data.writeInt32(mailSenderType);
            }

            data.writeBits(name.getBytes().length, 6);
            data.writeBit(firstLogin);
            data.writeBit(boostInProgress);
            data.writeBits(unkWod61x, 5);

            for (var str : mailSenders) {
                data.writeBits(str.getBytes().length + 1, 6);
            }

            data.flushBits();

            for (var str : mailSenders) {
                if (!str.isEmpty()) {
                    data.writeCString(str);
                }
            }

            data.writeString(name);
        }

        public final static class VisualItemInfo {
            public int displayId;
            public int displayEnchantId;
            public int secondaryItemModifiedAppearanceID; // also -1 is some special value
            public byte invType;
            public byte subclass;

            public void write(WorldPacket data) {
                data.writeInt32(displayId);
                data.writeInt32(displayEnchantId);
                data.writeInt32(secondaryItemModifiedAppearanceID);
                data.writeInt8(invType);
                data.writeInt8(subclass);
            }

            public VisualItemInfo clone() {
                VisualItemInfo varCopy = new VisualItemInfo();

                varCopy.displayId = this.displayId;
                varCopy.displayEnchantId = this.displayEnchantId;
                varCopy.secondaryItemModifiedAppearanceID = this.secondaryItemModifiedAppearanceID;
                varCopy.invType = this.invType;
                varCopy.subclass = this.subclass;

                return varCopy;
            }
        }

        public final static class PetInfo {
            public int creatureDisplayId; // PetCreatureDisplayID
            public int level; // PetExperienceLevel
            public int creatureFamily; // PetCreatureFamilyID

            public PetInfo clone() {
                PetInfo varCopy = new petInfo();

                varCopy.creatureDisplayId = this.creatureDisplayId;
                varCopy.level = this.level;
                varCopy.creatureFamily = this.creatureFamily;

                return varCopy;
            }
        }
    }

    public final static class RaceUnlock {
        public int raceID;
        public boolean hasExpansion;
        public boolean hasAchievement;
        public boolean hasHeritageArmor;

        public void write(WorldPacket data) {
            data.writeInt32(raceID);
            data.writeBit(hasExpansion);
            data.writeBit(hasAchievement);
            data.writeBit(hasHeritageArmor);
            data.flushBits();
        }

        public RaceUnlock clone() {
            RaceUnlock varCopy = new RaceUnlock();

            varCopy.raceID = this.raceID;
            varCopy.hasExpansion = this.hasExpansion;
            varCopy.hasAchievement = this.hasAchievement;
            varCopy.hasHeritageArmor = this.hasHeritageArmor;

            return varCopy;
        }
    }

    public final static class UnlockedConditionalAppearance {
        public int achievementID;
        public int unused;

        public void write(WorldPacket data) {
            data.writeInt32(achievementID);
            data.writeInt32(unused);
        }

        public UnlockedConditionalAppearance clone() {
            UnlockedConditionalAppearance varCopy = new UnlockedConditionalAppearance();

            varCopy.achievementID = this.achievementID;
            varCopy.unused = this.unused;

            return varCopy;
        }
    }

    public final static class RaceLimitDisableInfo {
        public int raceID;
        public int blockReason;

        public void write(WorldPacket data) {
            data.writeInt32(raceID);
            data.writeInt32(blockReason);
        }

        public RaceLimitDisableInfo clone() {
            RaceLimitDisableInfo varCopy = new RaceLimitDisableInfo();

            varCopy.raceID = this.raceID;
            varCopy.blockReason = this.blockReason;

            return varCopy;
        }

        private enum blah {
            Server,
            level;

            public static final int SIZE = Integer.SIZE;

            public static blah forValue(int value) {
                return values()[value];
            }

            public int getValue() {
                return this.ordinal();
            }
        }
    }
}
