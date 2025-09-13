package com.github.azeroth.game.garrison;


import java.util.ArrayList;
import java.util.HashMap;


public class GarrisonManager {
    // counters, Traits
    private final int[][] abilitiesForQuality =
            {
                    {0, 0},
                    {1, 0},
                    {1, 1},
                    {1, 2},
                    {2, 3},
                    {2, 3}
            };

    private final MultiMap<Integer, GarrSiteLevelPlotInstRecord> garrisonPlotInstBySiteLevel = new MultiMap<Integer, GarrSiteLevelPlotInstRecord>();
    private final HashMap<Integer, HashMap<Integer, GameObjectsRecord>> garrisonPlots = new HashMap<Integer, HashMap<Integer, GameObjectsRecord>>();
    private final MultiMap<Integer, Integer> garrisonBuildingsByPlot = new MultiMap<Integer, Integer>();
    private final HashMap<Long, Integer> garrisonBuildingPlotInstances = new HashMap<Long, Integer>();
    private final MultiMap<Integer, Integer> garrisonBuildingsByType = new MultiMap<Integer, Integer>();
    private final HashMap<Integer, FinalizeGarrisonPlotGOInfo> finalizePlotGOInfo = new HashMap<Integer, FinalizeGarrisonPlotGOInfo>();
    private final HashMap<Integer, GarrAbilities>[] garrisonFollowerAbilities = new HashMap<Integer, GarrAbilities>[2];
    private final MultiMap<Integer, GarrAbilityRecord> garrisonFollowerClassSpecAbilities = new MultiMap<Integer, GarrAbilityRecord>();
    private final ArrayList<GarrAbilityRecord> garrisonFollowerRandomTraits = new ArrayList<>();

    private long followerDbIdGenerator = 1;

    private GarrisonManager() {
    }

    public final void initialize() {
        for (var siteLevelPlotInst : CliDB.GarrSiteLevelPlotInstStorage.values()) {
            garrisonPlotInstBySiteLevel.add(siteLevelPlotInst.garrSiteLevelID, siteLevelPlotInst);
        }

        for (var gameObject : CliDB.GameObjectsStorage.values()) {
            if (gameObject.typeID == GameObjectTypes.garrisonPlot) {
                if (!garrisonPlots.containsKey(gameObject.OwnerID)) {
                    garrisonPlots.put(gameObject.OwnerID, new HashMap<Integer, GameObjectsRecord>());
                }

                garrisonPlots.get(gameObject.OwnerID).put((int) gameObject.PropValue[0], gameObject);
            }
        }

        for (var plotBuilding : CliDB.GarrPlotBuildingStorage.values()) {
            garrisonBuildingsByPlot.add(plotBuilding.GarrPlotID, plotBuilding.garrBuildingID);
        }

        for (var buildingPlotInst : CliDB.GarrBuildingPlotInstStorage.values()) {
            garrisonBuildingPlotInstances.put(MathUtil.MakePair64(buildingPlotInst.garrBuildingID, buildingPlotInst.GarrSiteLevelPlotInstID), buildingPlotInst.id);
        }

        for (var building : CliDB.GarrBuildingStorage.values()) {
            garrisonBuildingsByType.add((byte) building.BuildingType, building.id);
        }

        for (var i = 0; i < 2; ++i) {
            _garrisonFollowerAbilities[i] = new HashMap<Integer, GarrAbilities>();
        }

        for (var followerAbility : CliDB.GarrFollowerXAbilityStorage.values()) {
            var ability = CliDB.GarrAbilityStorage.get(followerAbility.GarrAbilityID);

            if (ability != null) {
                if (ability.garrFollowerTypeID != (int) GarrisonFollowerType.Garrison.getValue()) {
                    continue;
                }

                if (!ability.flags.hasFlag(GarrisonAbilityFlags.CannotRoll) && ability.flags.hasFlag(GarrisonAbilityFlags.Trait)) {
                    garrisonFollowerRandomTraits.add(ability);
                }

                if (followerAbility.factionIndex < 2) {
                    var dic = _garrisonFollowerAbilities[followerAbility.FactionIndex];

                    if (!dic.containsKey(followerAbility.garrFollowerID)) {
                        dic.put(followerAbility.garrFollowerID, new GarrAbilities());
                    }

                    if (ability.flags.hasFlag(GarrisonAbilityFlags.Trait)) {
                        dic.get(followerAbility.garrFollowerID).traits.add(ability);
                    } else {
                        dic.get(followerAbility.garrFollowerID).counters.add(ability);
                    }
                }
            }
        }

        initializeDbIdSequences();
        loadPlotFinalizeGOInfo();
        loadFollowerClassSpecAbilities();
    }

    public final GarrSiteLevelRecord getGarrSiteLevelEntry(int garrSiteId, int level) {
        for (var siteLevel : CliDB.GarrSiteLevelStorage.values()) {
            if (siteLevel.garrSiteID == garrSiteId && siteLevel.GarrLevel == level) {
                return siteLevel;
            }
        }

        return null;
    }

    public final ArrayList<GarrSiteLevelPlotInstRecord> getGarrPlotInstForSiteLevel(int garrSiteLevelId) {
        return garrisonPlotInstBySiteLevel.get(garrSiteLevelId);
    }

    public final GameObjectsRecord getPlotGameObject(int mapId, int garrPlotInstanceId) {
        var pair = garrisonPlots.get(mapId);

        if (pair != null) {
            var gameobjectsRecord = pair.get(garrPlotInstanceId);

            if (gameobjectsRecord != null) {
                return gameobjectsRecord;
            }
        }

        return null;
    }

    public final boolean isPlotMatchingBuilding(int garrPlotId, int garrBuildingId) {
        var plotList = garrisonBuildingsByPlot.get(garrPlotId);

        if (!plotList.isEmpty()) {
            return plotList.contains(garrBuildingId);
        }

        return false;
    }

    public final int getGarrBuildingPlotInst(int garrBuildingId, int garrSiteLevelPlotInstId) {
        return garrisonBuildingPlotInstances.get(MathUtil.MakePair64(garrBuildingId, garrSiteLevelPlotInstId));
    }

    public final int getPreviousLevelBuilding(int buildingType, int currentLevel) {
        var list = garrisonBuildingsByType.get(buildingType);

        if (!list.isEmpty()) {
            for (var buildingId : list) {
                if (CliDB.GarrBuildingStorage.get(buildingId).UpgradeLevel == currentLevel - 1) {
                    return buildingId;
                }
            }
        }

        return 0;
    }

    public final FinalizeGarrisonPlotGOInfo getPlotFinalizeGOInfo(int garrPlotInstanceID) {
        return finalizePlotGOInfo.get(garrPlotInstanceID);
    }

    public final long generateFollowerDbId() {
        if (followerDbIdGenerator >= Long.MAX_VALUE) {
            Log.outFatal(LogFilter.Server, "Garrison follower db id overflow! Can't continue, shutting down server. ");
            global.getWorldMgr().stopNow();
        }

        return followerDbIdGenerator++;
    }

    //todo check this method, might be slow.....
    public final ArrayList<GarrAbilityRecord> rollFollowerAbilities(int garrFollowerId, GarrFollowerRecord follower, int quality, int faction, boolean initial) {
        var hasForcedExclusiveTrait = false;
        ArrayList<GarrAbilityRecord> result = new ArrayList<>();

        int[] slots = {AbilitiesForQuality[quality][0], AbilitiesForQuality[quality][1]};

        GarrAbilities garrAbilities = null;
        var abilities = _garrisonFollowerAbilities[faction].get(garrFollowerId);

        if (abilities != null) {
            garrAbilities = abilities;
        }

        ArrayList<GarrAbilityRecord> abilityList = new ArrayList<>();
        ArrayList<GarrAbilityRecord> forcedAbilities = new ArrayList<>();
        ArrayList<GarrAbilityRecord> traitList = new ArrayList<>();
        ArrayList<GarrAbilityRecord> forcedTraits = new ArrayList<>();

        if (garrAbilities != null) {
            for (var ability : garrAbilities.counters) {
                if (ability.flags.hasFlag(GarrisonAbilityFlags.HordeOnly) && faction != GarrisonFactionIndex.Horde) {
                    continue;
                } else if (ability.flags.hasFlag(GarrisonAbilityFlags.AllianceOnly) && faction != GarrisonFactionIndex.Alliance) {
                    continue;
                }

                if (ability.flags.hasFlag(GarrisonAbilityFlags.CannotRemove)) {
                    forcedAbilities.add(ability);
                } else {
                    abilityList.add(ability);
                }
            }

            for (var ability : garrAbilities.traits) {
                if (ability.flags.hasFlag(GarrisonAbilityFlags.HordeOnly) && faction != GarrisonFactionIndex.Horde) {
                    continue;
                } else if (ability.flags.hasFlag(GarrisonAbilityFlags.AllianceOnly) && faction != GarrisonFactionIndex.Alliance) {
                    continue;
                }

                if (ability.flags.hasFlag(GarrisonAbilityFlags.CannotRemove)) {
                    forcedTraits.add(ability);
                } else {
                    traitList.add(ability);
                }
            }
        }

        abilityList.RandomResize((int) Math.max(0, slots[0] - forcedAbilities.size()));
        traitList.RandomResize((int) Math.max(0, slots[1] - forcedTraits.size()));

        // Add abilities specified in GarrFollowerXAbility.db2 before generic classspec ones on follower creation
        if (initial) {
            forcedAbilities.addAll(abilityList);
            forcedTraits.addAll(traitList);
        }

        collections.sort(forcedAbilities);
        collections.sort(abilityList);
        collections.sort(forcedTraits);
        collections.sort(traitList);

        // check if we have a trait from exclusive category
        for (var ability : forcedTraits) {
            if (ability.flags.hasFlag(GarrisonAbilityFlags.Exclusive)) {
                hasForcedExclusiveTrait = true;

                break;
            }
        }

        if (slots[0] > forcedAbilities.size() + abilityList.size()) {
            var classSpecAbilities = getClassSpecAbilities(follower, faction);
            var classSpecAbilitiesTemp = classSpecAbilities.Except(forcedAbilities);

            abilityList = classSpecAbilitiesTemp.Union(abilityList).ToList();
            abilityList.RandomResize((int) Math.max(0, slots[0] - forcedAbilities.size()));
        }

        if (slots[1] > forcedTraits.size() + traitList.size()) {
            ArrayList<GarrAbilityRecord> genericTraitsTemp = new ArrayList<>();

            for (var ability : garrisonFollowerRandomTraits) {
                if (ability.flags.hasFlag(GarrisonAbilityFlags.HordeOnly) && faction != GarrisonFactionIndex.Horde) {
                    continue;
                } else if (ability.flags.hasFlag(GarrisonAbilityFlags.AllianceOnly) && faction != GarrisonFactionIndex.Alliance) {
                    continue;
                }

                // forced exclusive trait exists, skip other ones entirely
                if (hasForcedExclusiveTrait && ability.flags.hasFlag(GarrisonAbilityFlags.Exclusive)) {
                    continue;
                }

                genericTraitsTemp.add(ability);
            }

            var genericTraits = genericTraitsTemp.Except(forcedTraits).ToList();
            genericTraits.addAll(traitList);

            collections.sort(genericTraits, (GarrAbilityRecord a1, GarrAbilityRecord a2) ->
            {
                var e1 = GarrisonAbilityFlags.forValue(a1.flags.getValue() & GarrisonAbilityFlags.Exclusive.getValue().getValue());
                var e2 = GarrisonAbilityFlags.forValue(a2.flags.getValue() & GarrisonAbilityFlags.Exclusive.getValue().getValue());

                if (e1 != e2) {
                    return (new integer(e1)).compareTo(e2);
                }

                return (new integer(a1.id)).compareTo(a2.id);
            });

            genericTraits = genericTraits.Distinct().ToList();

            var firstExclusive = 0;
            var total = genericTraits.size();

            for (var i = 0; i < total; ++i, ++firstExclusive) {
                if (genericTraits.get(i).flags.hasFlag(GarrisonAbilityFlags.Exclusive)) {
                    break;
                }
            }

            while (traitList.size() < Math.max(0, slots[1] - forcedTraits.size()) && total != 0) {
                var garrAbility = genericTraits.get(RandomUtil.IRand(0, total-- - 1));

                if (garrAbility.flags.hasFlag(GarrisonAbilityFlags.Exclusive)) {
                    total = firstExclusive; // selected exclusive trait - no other can be selected now
                } else {
                    --firstExclusive;
                }

                traitList.add(garrAbility);
                genericTraits.remove(garrAbility);
            }
        }

        result.addAll(forcedAbilities);
        result.addAll(abilityList);
        result.addAll(forcedTraits);
        result.addAll(traitList);

        return result;
    }

    private ArrayList<GarrAbilityRecord> getClassSpecAbilities(GarrFollowerRecord follower, int faction) {
        ArrayList<GarrAbilityRecord> abilities = new ArrayList<>();
        int classSpecId;

        switch (faction) {
            case GarrisonFactionIndex.Horde:
                classSpecId = follower.HordeGarrClassSpecID;

                break;
            case GarrisonFactionIndex.Alliance:
                classSpecId = follower.AllianceGarrClassSpecID;

                break;
            default:
                return abilities;
        }

        if (!CliDB.GarrClassSpecStorage.containsKey(classSpecId)) {
            return abilities;
        }

        var garrAbility = garrisonFollowerClassSpecAbilities.get(classSpecId);

        if (!garrAbility.isEmpty()) {
            abilities = garrAbility;
        }

        return abilities;
    }

    private void initializeDbIdSequences() {
        var result = DB.characters.query("SELECT MAX(dbId) FROM character_garrison_followers");

        if (!result.isEmpty()) {
            followerDbIdGenerator = result.<Long>Read(0) + 1;
        }
    }

    private void loadPlotFinalizeGOInfo() {
        //                                                                0                  1       2       3       4       5               6
        var result = DB.World.query("SELECT garrPlotInstanceId, hordeGameObjectId, hordeX, hordeY, hordeZ, hordeO, hordeAnimKitId, " + "allianceGameObjectId, allianceX, allianceY, allianceZ, allianceO, allianceAnimKitId FROM garrison_plot_finalize_info");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 garrison follower class spec abilities. DB table `garrison_plot_finalize_info` is empty.");

            return;
        }

        var msTime = System.currentTimeMillis();

        do {
            var garrPlotInstanceId = result.<Integer>Read(0);
            var hordeGameObjectId = result.<Integer>Read(1);
            var allianceGameObjectId = result.<Integer>Read(7);
            var hordeAnimKitId = result.<SHORT>Read(6);
            var allianceAnimKitId = result.<SHORT>Read(12);

            if (!CliDB.GarrPlotInstanceStorage.containsKey(garrPlotInstanceId)) {
                Logs.SQL.error("Non-existing GarrPlotInstance.db2 entry {0} was referenced in `garrison_plot_finalize_info`.", garrPlotInstanceId);

                continue;
            }

            var goTemplate = global.getObjectMgr().getGameObjectTemplate(hordeGameObjectId);

            if (goTemplate == null) {
                Logs.SQL.error("Non-existing gameobject_template entry {0} was referenced in `garrison_plot_finalize_info`.`hordeGameObjectId` for garrPlotInstanceId {1}.", hordeGameObjectId, garrPlotInstanceId);

                continue;
            }

            if (goTemplate.type != GameObjectTypes.goober) {
                Logs.SQL.error("Invalid gameobject type {0} (entry {1}) was referenced in `garrison_plot_finalize_info`.`hordeGameObjectId` for garrPlotInstanceId {2}.", goTemplate.type, hordeGameObjectId, garrPlotInstanceId);

                continue;
            }

            goTemplate = global.getObjectMgr().getGameObjectTemplate(allianceGameObjectId);

            if (goTemplate == null) {
                Logs.SQL.error("Non-existing gameobject_template entry {0} was referenced in `garrison_plot_finalize_info`.`allianceGameObjectId` for garrPlotInstanceId {1}.", allianceGameObjectId, garrPlotInstanceId);

                continue;
            }

            if (goTemplate.type != GameObjectTypes.goober) {
                Logs.SQL.error("Invalid gameobject type {0} (entry {1}) was referenced in `garrison_plot_finalize_info`.`allianceGameObjectId` for garrPlotInstanceId {2}.", goTemplate.type, allianceGameObjectId, garrPlotInstanceId);

                continue;
            }

            if (hordeAnimKitId != 0 && !CliDB.AnimKitStorage.containsKey(hordeAnimKitId)) {
                Logs.SQL.error("Non-existing animKit.dbc entry {0} was referenced in `garrison_plot_finalize_info`.`hordeAnimKitId` for garrPlotInstanceId {1}.", hordeAnimKitId, garrPlotInstanceId);

                continue;
            }

            if (allianceAnimKitId != 0 && !CliDB.AnimKitStorage.containsKey(allianceAnimKitId)) {
                Logs.SQL.error("Non-existing animKit.dbc entry {0} was referenced in `garrison_plot_finalize_info`.`allianceAnimKitId` for garrPlotInstanceId {1}.", allianceAnimKitId, garrPlotInstanceId);

                continue;
            }

            FinalizeGarrisonPlotGOInfo info = new FinalizeGarrisonPlotGOInfo();
            info.factionInfo[GarrisonFactionIndex.Horde].gameObjectId = hordeGameObjectId;
            info.factionInfo[GarrisonFactionIndex.Horde].pos = new Position(result.<Float>Read(2), result.<Float>Read(3), result.<Float>Read(4), result.<Float>Read(5));
            info.factionInfo[GarrisonFactionIndex.Horde].animKitId = hordeAnimKitId;

            info.factionInfo[GarrisonFactionIndex.Alliance].gameObjectId = allianceGameObjectId;
            info.factionInfo[GarrisonFactionIndex.Alliance].pos = new Position(result.<Float>Read(8), result.<Float>Read(9), result.<Float>Read(10), result.<Float>Read(11));
            info.factionInfo[GarrisonFactionIndex.Alliance].animKitId = allianceAnimKitId;

            finalizePlotGOInfo.put(garrPlotInstanceId, info);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} garrison plot finalize entries in {1}.", finalizePlotGOInfo.size(), time.GetMSTimeDiffToNow(msTime));
    }

    private void loadFollowerClassSpecAbilities() {
        var result = DB.World.query("SELECT classSpecId, abilityId FROM garrison_follower_class_spec_abilities");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 garrison follower class spec abilities. DB table `garrison_follower_class_spec_abilities` is empty.");

            return;
        }

        var msTime = System.currentTimeMillis();
        int count = 0;

        do {
            var classSpecId = result.<Integer>Read(0);
            var abilityId = result.<Integer>Read(1);

            if (!CliDB.GarrClassSpecStorage.containsKey(classSpecId)) {
                Logs.SQL.error("Non-existing GarrClassSpec.db2 entry {0} was referenced in `garrison_follower_class_spec_abilities` by row ({1}, {2}).", classSpecId, classSpecId, abilityId);

                continue;
            }

            var ability = CliDB.GarrAbilityStorage.get(abilityId);

            if (ability == null) {
                Logs.SQL.error("Non-existing GarrAbility.db2 entry {0} was referenced in `garrison_follower_class_spec_abilities` by row ({1}, {2}).", abilityId, classSpecId, abilityId);

                continue;
            }

            garrisonFollowerClassSpecAbilities.add(classSpecId, ability);
            ++count;
        } while (result.NextRow());

        //foreach (var key in garrisonFollowerClassSpecAbilities.Keys)
        //_garrisonFollowerClassSpecAbilities[key].Sort();

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} garrison follower class spec abilities in {1}.", count, time.GetMSTimeDiffToNow(msTime));
    }
}
