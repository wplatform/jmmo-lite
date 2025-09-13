package com.github.azeroth.game.entity.item;


import java.util.HashMap;


class ItemAdditionalLoadInfo {
    public artifactData artifact;
    public AzeriteData azeriteItem;
    public AzeriteEmpoweredData azeriteEmpoweredItem;

    public static void init(HashMap<Long, ItemAdditionalLoadInfo> loadInfo, SQLResult artifactResult, SQLResult azeriteItemResult, SQLResult azeriteItemMilestonePowersResult, SQLResult azeriteItemUnlockedEssencesResult, SQLResult azeriteEmpoweredItemResult) {

//		ItemAdditionalLoadInfo GetOrCreateLoadInfo(ulong guid)
//			{
//				if (!loadInfo.ContainsKey(guid))
//					loadInfo[guid] = new ItemAdditionalLoadInfo();
//
//				return loadInfo[guid];
//			}

        if (!artifactResult.isEmpty()) {
            do {
                var info = GetOrCreateLoadInfo(artifactResult.<Long>Read(0));

                if (info.artifact == null) {
                    info.artifact = new ArtifactData();
                }

                info.artifact.xp = artifactResult.<Long>Read(1);
                info.artifact.artifactAppearanceId = artifactResult.<Integer>Read(2);
                info.artifact.artifactTierId = artifactResult.<Integer>Read(3);

                ArtifactPowerData artifactPowerData = new ArtifactPowerData();
                artifactPowerData.artifactPowerId = artifactResult.<Integer>Read(4);
                artifactPowerData.purchasedRank = artifactResult.<Byte>Read(5);

                var artifactPower = CliDB.ArtifactPowerStorage.get(artifactPowerData.artifactPowerId);

                if (artifactPower != null) {
                    int maxRank = artifactPower.MaxPurchasableRank;

                    // allow ARTIFACT_POWER_FLAG_FINAL to overflow maxrank here - needs to be handled in Item::CheckArtifactUnlock (will refund artifact power)
                    if (artifactPower.flags.hasFlag(ArtifactPowerFlag.MaxRankWithTier) && artifactPower.tier < info.artifact.artifactTierId) {
                        maxRank += info.artifact.ArtifactTierId - artifactPower.tier;
                    }

                    if (artifactPowerData.purchasedRank > maxRank) {
                        artifactPowerData.purchasedRank = (byte) maxRank;
                    }

                    artifactPowerData.currentRankWithBonus = (byte) ((artifactPower.flags.getValue() & ArtifactPowerFlag.first.getValue()) == ArtifactPowerFlag.first.getValue() ? 1 : 0);

                    info.artifact.artifactPowers.add(artifactPowerData);
                }
            } while (artifactResult.NextRow());
        }

        if (!azeriteItemResult.isEmpty()) {
            do {
                var info = GetOrCreateLoadInfo(azeriteItemResult.<Long>Read(0));

                if (info.azeriteItem == null) {
                    info.azeriteItem = new AzeriteData();
                }

                info.azeriteItem.xp = azeriteItemResult.<Long>Read(1);
                info.azeriteItem.level = azeriteItemResult.<Integer>Read(2);
                info.azeriteItem.KnowledgeLevel = azeriteItemResult.<Integer>Read(3);

                for (var i = 0; i < info.azeriteItem.SelectedAzeriteEssences.length; ++i) {
                    info.azeriteItem.SelectedAzeriteEssences[i] = new AzeriteItemSelectedEssencesData();

                    var specializationId = azeriteItemResult.<Integer>Read(4 + i * 4);

                    if (!CliDB.ChrSpecializationStorage.containsKey(specializationId)) {
                        continue;
                    }

                    info.azeriteItem.SelectedAzeriteEssences[i].specializationId = specializationId;

                    for (var j = 0; j < SharedConst.MaxAzeriteEssenceSlot; ++j) {
                        var azeriteEssence = CliDB.AzeriteEssenceStorage.get(azeriteItemResult.<Integer>Read(5 + i * 5 + j));

                        if (azeriteEssence == null || !global.getDB2Mgr().IsSpecSetMember(azeriteEssence.SpecSetID, specializationId)) {
                            continue;
                        }

                        info.azeriteItem.SelectedAzeriteEssences[i].AzeriteEssenceId[j] = azeriteEssence.id;
                    }
                }
            } while (azeriteItemResult.NextRow());
        }

        if (!azeriteItemMilestonePowersResult.isEmpty()) {
            do {
                var info = GetOrCreateLoadInfo(azeriteItemMilestonePowersResult.<Long>Read(0));

                if (info.azeriteItem == null) {
                    info.azeriteItem = new AzeriteData();
                }

                info.azeriteItem.AzeriteItemMilestonePowers.add(azeriteItemMilestonePowersResult.<Integer>Read(1));
            } while (azeriteItemMilestonePowersResult.NextRow());
        }

        if (!azeriteItemUnlockedEssencesResult.isEmpty()) {
            do {
                var azeriteEssencePower = global.getDB2Mgr().GetAzeriteEssencePower(azeriteItemUnlockedEssencesResult.<Integer>Read(1), azeriteItemUnlockedEssencesResult.<Integer>Read(2));

                if (azeriteEssencePower != null) {
                    var info = GetOrCreateLoadInfo(azeriteItemUnlockedEssencesResult.<Long>Read(0));

                    if (info.azeriteItem == null) {
                        info.azeriteItem = new AzeriteData();
                    }

                    info.azeriteItem.UnlockedAzeriteEssences.add(azeriteEssencePower);
                }
            } while (azeriteItemUnlockedEssencesResult.NextRow());
        }

        if (!azeriteEmpoweredItemResult.isEmpty()) {
            do {
                var info = GetOrCreateLoadInfo(azeriteEmpoweredItemResult.<Long>Read(0));

                if (info.azeriteEmpoweredItem == null) {
                    info.azeriteEmpoweredItem = new AzeriteEmpoweredData();
                }

                for (var i = 0; i < SharedConst.MaxAzeriteEmpoweredTier; ++i) {
                    if (CliDB.AzeritePowerStorage.containsKey(azeriteEmpoweredItemResult.<Integer>Read(1 + i))) {
                        info.azeriteEmpoweredItem.SelectedAzeritePowers[i] = azeriteEmpoweredItemResult.<Integer>Read(1 + i);
                    }
                }
            } while (azeriteEmpoweredItemResult.NextRow());
        }
    }
}
