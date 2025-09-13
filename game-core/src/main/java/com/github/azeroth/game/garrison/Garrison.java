package com.github.azeroth.game.garrison;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.garrison.GarrisonFollower;
import com.github.azeroth.game.networking.packet.garrison.GarrisonRemoteInfo;
import game.PhasingHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Garrison {
    private final Player owner;
    private final GarrisonType garrisonType;
    private final HashMap<Integer, Plot> plots = new HashMap<Integer, Plot>();
    private final ArrayList<Integer> knownBuildings = new ArrayList<>();
    private final HashMap<Long, follower> followers = new HashMap<Long, follower>();
    private final ArrayList<Integer> followerIds = new ArrayList<>();
    private GarrSiteLevelRecord siteLevel;
    private int followerActivationsRemainingToday;

    public Garrison(Player owner) {
        owner = owner;
        followerActivationsRemainingToday = 1;
    }

    public static void deleteFromDB(long ownerGuid, SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_GARRISON);
        stmt.AddValue(0, ownerGuid);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_GARRISON_BLUEPRINTS);
        stmt.AddValue(0, ownerGuid);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_GARRISON_BUILDINGS);
        stmt.AddValue(0, ownerGuid);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_GARRISON_FOLLOWERS);
        stmt.AddValue(0, ownerGuid);
        trans.append(stmt);
    }

    public final boolean loadFromDB(SQLResult garrison, SQLResult blueprints, SQLResult buildings, SQLResult followers, SQLResult abilities) {
        if (garrison.isEmpty()) {
            return false;
        }

        siteLevel = CliDB.GarrSiteLevelStorage.get(garrison.<Integer>Read(0));
        followerActivationsRemainingToday = garrison.<Integer>Read(1);

        if (siteLevel == null) {
            return false;
        }

        initializePlots();

        if (!blueprints.isEmpty()) {
            do {
                var building = CliDB.GarrBuildingStorage.get(blueprints.<Integer>Read(0));

                if (building != null) {
                    knownBuildings.add(building.id);
                }
            } while (blueprints.NextRow());
        }

        if (!buildings.isEmpty()) {
            do {
                var plotInstanceId = buildings.<Integer>Read(0);
                var buildingId = buildings.<Integer>Read(1);
                var timeBuilt = buildings.<Long>Read(2);
                var active = buildings.<Boolean>Read(3);

                var plot = getPlot(plotInstanceId);

                if (plot == null) {
                    continue;
                }

                if (!CliDB.GarrBuildingStorage.containsKey(buildingId)) {
                    continue;
                }

                plot.buildingInfo.packetInfo = new garrisonBuildingInfo();
                plot.buildingInfo.packetInfo.garrPlotInstanceID = plotInstanceId;
                plot.buildingInfo.packetInfo.garrBuildingID = buildingId;
                plot.buildingInfo.packetInfo.timeBuilt = timeBuilt;
                plot.buildingInfo.packetInfo.active = active;
            } while (buildings.NextRow());
        }

        if (!followers.isEmpty()) {
            do {
                var dbId = followers.<Long>Read(0);
                var followerId = followers.<Integer>Read(1);

                if (!CliDB.GarrFollowerStorage.containsKey(followerId)) {
                    continue;
                }

                followerIds.add(followerId);

                var follower = new Follower();
                follower.packetInfo.dbID = dbId;
                follower.packetInfo.garrFollowerID = followerId;
                follower.packetInfo.quality = followers.<Integer>Read(2);
                follower.packetInfo.followerLevel = followers.<Integer>Read(3);
                follower.packetInfo.itemLevelWeapon = followers.<Integer>Read(4);
                follower.packetInfo.itemLevelArmor = followers.<Integer>Read(5);
                follower.packetInfo.xp = followers.<Integer>Read(6);
                follower.packetInfo.currentBuildingID = followers.<Integer>Read(7);
                follower.packetInfo.currentMissionID = followers.<Integer>Read(8);
                follower.packetInfo.followerStatus = followers.<Integer>Read(9);

                if (!CliDB.GarrBuildingStorage.containsKey(follower.packetInfo.currentBuildingID)) {
                    follower.packetInfo.currentBuildingID = 0;
                }

                //if (!sGarrMissionStore.LookupEntry(follower.packetInfo.currentMissionID))
                //    follower.packetInfo.currentMissionID = 0;
                followers.put(followerId, follower);
            } while (followers.NextRow());

            if (!abilities.isEmpty()) {
                do {
                    var dbId = abilities.<Long>Read(0);
                    var ability = CliDB.GarrAbilityStorage.get(abilities.<Integer>Read(1));

                    if (ability == null) {
                        continue;
                    }

                    var garrisonFollower = followers.get(dbId);

                    if (garrisonFollower == null) {
                        continue;
                    }

                    garrisonFollower.packetInfo.abilityID.add(ability);
                } while (abilities.NextRow());
            }
        }

        return true;
    }

    public final void saveToDB(SQLTransaction trans) {
        deleteFromDB(owner.getGUID().getCounter(), trans);

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_GARRISON);
        stmt.AddValue(0, owner.getGUID().getCounter());
        stmt.AddValue(1, garrisonType.getValue());
        stmt.AddValue(2, siteLevel.id);
        stmt.AddValue(3, followerActivationsRemainingToday);
        trans.append(stmt);

        for (var building : knownBuildings) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_GARRISON_BLUEPRINTS);
            stmt.AddValue(0, owner.getGUID().getCounter());
            stmt.AddValue(1, garrisonType.getValue());
            stmt.AddValue(2, building);
            trans.append(stmt);
        }

        for (var plot : plots.values()) {
            if (plot.buildingInfo.packetInfo != null) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_GARRISON_BUILDINGS);
                stmt.AddValue(0, owner.getGUID().getCounter());
                stmt.AddValue(1, garrisonType.getValue());
                stmt.AddValue(2, plot.buildingInfo.packetInfo.garrPlotInstanceID);
                stmt.AddValue(3, plot.buildingInfo.packetInfo.garrBuildingID);
                stmt.AddValue(4, plot.buildingInfo.packetInfo.timeBuilt);
                stmt.AddValue(5, plot.buildingInfo.packetInfo.active);
                trans.append(stmt);
            }
        }

        for (var follower : followers.values()) {
            byte index = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_GARRISON_FOLLOWERS);
            stmt.AddValue(index++, follower.packetInfo.dbID);
            stmt.AddValue(index++, owner.getGUID().getCounter());
            stmt.AddValue(index++, garrisonType.getValue());
            stmt.AddValue(index++, follower.packetInfo.garrFollowerID);
            stmt.AddValue(index++, follower.packetInfo.quality);
            stmt.AddValue(index++, follower.packetInfo.followerLevel);
            stmt.AddValue(index++, follower.packetInfo.itemLevelWeapon);
            stmt.AddValue(index++, follower.packetInfo.itemLevelArmor);
            stmt.AddValue(index++, follower.packetInfo.xp);
            stmt.AddValue(index++, follower.packetInfo.currentBuildingID);
            stmt.AddValue(index++, follower.packetInfo.currentMissionID);
            stmt.AddValue(index++, follower.packetInfo.followerStatus);
            trans.append(stmt);

            byte slot = 0;

            for (var ability : follower.packetInfo.abilityID) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_GARRISON_FOLLOWER_ABILITIES);
                stmt.AddValue(0, follower.packetInfo.dbID);
                stmt.AddValue(1, ability.id);
                stmt.AddValue(2, slot++);
                trans.append(stmt);
            }
        }
    }

    public final boolean create(int garrSiteId) {
        var siteLevel = global.getGarrisonMgr().getGarrSiteLevelEntry(garrSiteId, 1);

        if (siteLevel == null) {
            return false;
        }

        siteLevel = siteLevel;

        initializePlots();

        GarrisonCreateResult garrisonCreateResult = new GarrisonCreateResult();
        garrisonCreateResult.garrSiteLevelID = siteLevel.id;
        owner.sendPacket(garrisonCreateResult);
        PhasingHandler.onConditionChange(owner);
        sendRemoteInfo();

        return true;
    }

    public final void delete() {
        SQLTransaction trans = new SQLTransaction();
        deleteFromDB(owner.getGUID().getCounter(), trans);
        DB.characters.CommitTransaction(trans);

        GarrisonDeleteResult garrisonDelete = new GarrisonDeleteResult();
        garrisonDelete.result = GarrisonError.success;
        garrisonDelete.garrSiteID = siteLevel.garrSiteID;
        owner.sendPacket(garrisonDelete);
    }

    public final int getFaction() {
        return owner.getTeam() == Team.Horde ? GarrisonFactionIndex.Horde : GarrisonFactionIndex.Alliance;
    }

    public final GarrisonType getGarrisonType() {
        return garrisonType;
    }

    public final GarrSiteLevelRecord getSiteLevel() {
        return siteLevel;
    }

    public final Collection<Plot> getPlots() {
        return plots.values();
    }

    public final Plot getPlot(int garrPlotInstanceId) {
        return plots.get(garrPlotInstanceId);
    }

    public final boolean hasBlueprint(int garrBuildingId) {
        return knownBuildings.contains(garrBuildingId);
    }

    public final void learnBlueprint(int garrBuildingId) {
        GarrisonLearnBlueprintResult learnBlueprintResult = new GarrisonLearnBlueprintResult();
        learnBlueprintResult.garrTypeID = getGarrisonType();
        learnBlueprintResult.buildingID = garrBuildingId;
        learnBlueprintResult.result = GarrisonError.success;

        if (!CliDB.GarrBuildingStorage.containsKey(garrBuildingId)) {
            learnBlueprintResult.result = GarrisonError.InvalidBuildingId;
        } else if (hasBlueprint(garrBuildingId)) {
            learnBlueprintResult.result = GarrisonError.BlueprintExists;
        } else {
            knownBuildings.add(garrBuildingId);
        }

        owner.sendPacket(learnBlueprintResult);
    }

    public final void placeBuilding(int garrPlotInstanceId, int garrBuildingId) {
        GarrisonPlaceBuildingResult placeBuildingResult = new GarrisonPlaceBuildingResult();
        placeBuildingResult.garrTypeID = getGarrisonType();
        placeBuildingResult.result = checkBuildingPlacement(garrPlotInstanceId, garrBuildingId);

        if (placeBuildingResult.result == GarrisonError.success) {
            placeBuildingResult.buildingInfo.garrPlotInstanceID = garrPlotInstanceId;
            placeBuildingResult.buildingInfo.garrBuildingID = garrBuildingId;
            placeBuildingResult.buildingInfo.timeBuilt = gameTime.GetGameTime();

            var plot = getPlot(garrPlotInstanceId);
            int oldBuildingId = 0;
            var map = findMap();
            var building = CliDB.GarrBuildingStorage.get(garrBuildingId);

            if (map) {
                plot.deleteGameObject(map);
            }

            if (plot.buildingInfo.packetInfo != null) {
                oldBuildingId = plot.buildingInfo.packetInfo.garrBuildingID;

                if (CliDB.GarrBuildingStorage.get(oldBuildingId).BuildingType != building.BuildingType) {
                    plot.clearBuildingInfo(getGarrisonType(), owner);
                }
            }

            plot.setBuildingInfo(placeBuildingResult.buildingInfo, owner);

            if (map) {
                var go = plot.createGameObject(map, getFaction());

                if (go) {
                    map.addToMap(go);
                }
            }

            owner.removeCurrency(building.CurrencyTypeID, building.currencyQty, CurrencyDestroyReason.Garrison);
            owner.modifyMoney(-building.GoldCost * MoneyConstants.gold, false);

            if (oldBuildingId != 0) {
                GarrisonBuildingRemoved buildingRemoved = new GarrisonBuildingRemoved();
                buildingRemoved.garrTypeID = getGarrisonType();
                buildingRemoved.result = GarrisonError.success;
                buildingRemoved.garrPlotInstanceID = garrPlotInstanceId;
                buildingRemoved.garrBuildingID = oldBuildingId;
                owner.sendPacket(buildingRemoved);
            }

            owner.updateCriteria(CriteriaType.PlaceGarrisonBuilding, garrBuildingId);
        }

        owner.sendPacket(placeBuildingResult);
    }

    public final void cancelBuildingConstruction(int garrPlotInstanceId) {
        GarrisonBuildingRemoved buildingRemoved = new GarrisonBuildingRemoved();
        buildingRemoved.garrTypeID = getGarrisonType();
        buildingRemoved.result = checkBuildingRemoval(garrPlotInstanceId);

        if (buildingRemoved.result == GarrisonError.success) {
            var plot = getPlot(garrPlotInstanceId);

            buildingRemoved.garrPlotInstanceID = garrPlotInstanceId;
            buildingRemoved.garrBuildingID = plot.buildingInfo.packetInfo.garrBuildingID;

            var map = findMap();

            if (map) {
                plot.deleteGameObject(map);
            }

            plot.clearBuildingInfo(getGarrisonType(), owner);
            owner.sendPacket(buildingRemoved);

            var constructing = CliDB.GarrBuildingStorage.get(buildingRemoved.garrBuildingID);
            // Refund construction/upgrade cost
            owner.addCurrency(constructing.CurrencyTypeID, (int) constructing.currencyQty, CurrencyGainSource.GarrisonBuildingRefund);
            owner.modifyMoney(constructing.GoldCost * MoneyConstants.gold, false);

            if (constructing.UpgradeLevel > 1) {
                // Restore previous level building
                var restored = global.getGarrisonMgr().getPreviousLevelBuilding((byte) constructing.BuildingType, constructing.UpgradeLevel);

                GarrisonPlaceBuildingResult placeBuildingResult = new GarrisonPlaceBuildingResult();
                placeBuildingResult.garrTypeID = getGarrisonType();
                placeBuildingResult.result = GarrisonError.success;
                placeBuildingResult.buildingInfo.garrPlotInstanceID = garrPlotInstanceId;
                placeBuildingResult.buildingInfo.garrBuildingID = restored;
                placeBuildingResult.buildingInfo.timeBuilt = gameTime.GetGameTime();
                placeBuildingResult.buildingInfo.active = true;

                plot.setBuildingInfo(placeBuildingResult.buildingInfo, owner);
                owner.sendPacket(placeBuildingResult);
            }

            if (map) {
                var go = plot.createGameObject(map, getFaction());

                if (go) {
                    map.addToMap(go);
                }
            }
        } else {
            owner.sendPacket(buildingRemoved);
        }
    }

    public final void activateBuilding(int garrPlotInstanceId) {
        var plot = getPlot(garrPlotInstanceId);

        if (plot != null) {
            if (plot.buildingInfo.canActivate() && plot.buildingInfo.packetInfo != null && !plot.buildingInfo.packetInfo.active) {
                plot.buildingInfo.packetInfo.active = true;
                var map = findMap();

                if (map) {
                    plot.deleteGameObject(map);
                    var go = plot.createGameObject(map, getFaction());

                    if (go) {
                        map.addToMap(go);
                    }
                }

                GarrisonBuildingActivated buildingActivated = new GarrisonBuildingActivated();
                buildingActivated.garrPlotInstanceID = garrPlotInstanceId;
                owner.sendPacket(buildingActivated);

                owner.updateCriteria(CriteriaType.ActivateAnyGarrisonBuilding, plot.buildingInfo.packetInfo.garrBuildingID);
            }
        }
    }

    public final void addFollower(int garrFollowerId) {
        GarrisonAddFollowerResult addFollowerResult = new GarrisonAddFollowerResult();
        addFollowerResult.garrTypeID = getGarrisonType();
        var followerEntry = CliDB.GarrFollowerStorage.get(garrFollowerId);

        if (followerIds.contains(garrFollowerId) || followerEntry == null) {
            addFollowerResult.result = GarrisonError.FollowerExists;
            owner.sendPacket(addFollowerResult);

            return;
        }

        followerIds.add(garrFollowerId);
        var dbId = global.getGarrisonMgr().generateFollowerDbId();

        Follower follower = new follower();
        follower.packetInfo.dbID = dbId;
        follower.packetInfo.garrFollowerID = garrFollowerId;
        follower.packetInfo.quality = (int) followerEntry.quality; // TODO: handle magic upgrades
        follower.packetInfo.followerLevel = followerEntry.followerLevel;
        follower.packetInfo.itemLevelWeapon = followerEntry.itemLevelWeapon;
        follower.packetInfo.itemLevelArmor = followerEntry.itemLevelArmor;
        follower.packetInfo.xp = 0;
        follower.packetInfo.currentBuildingID = 0;
        follower.packetInfo.currentMissionID = 0;
        follower.packetInfo.abilityID = global.getGarrisonMgr().rollFollowerAbilities(garrFollowerId, followerEntry, follower.packetInfo.quality, getFaction(), true);
        follower.packetInfo.followerStatus = 0;

        followers.put(dbId, follower);
        addFollowerResult.follower = follower.packetInfo;
        owner.sendPacket(addFollowerResult);

        owner.updateCriteria(CriteriaType.RecruitGarrisonFollower, follower.packetInfo.dbID);
    }

    public final Follower getFollower(long dbId) {
        return followers.get(dbId);
    }

    public final int countFollowers(java.util.function.Predicate<follower> predicate) {
        int count = 0;

        for (var pair : followers.entrySet()) {
            if (predicate(pair.getValue())) {
                ++count;
            }
        }

        return count;
    }

    public final void sendInfo() {
        GetGarrisonInfoResult garrisonInfo = new GetGarrisonInfoResult();
        garrisonInfo.factionIndex = getFaction();

        GarrisonInfo garrison = new GarrisonInfo();
        garrison.garrTypeID = getGarrisonType();
        garrison.garrSiteID = siteLevel.garrSiteID;
        garrison.garrSiteLevelID = siteLevel.id;
        garrison.numFollowerActivationsRemaining = followerActivationsRemainingToday;

        for (var plot : plots.values()) {
            garrison.plots.add(plot.packetInfo);

            if (plot.buildingInfo.packetInfo != null) {
                garrison.buildings.add(plot.buildingInfo.packetInfo);
            }
        }

        for (var follower : followers.values()) {
            garrison.followers.add(follower.packetInfo);
        }

        garrisonInfo.garrisons.add(garrison);

        owner.sendPacket(garrisonInfo);
    }

    public final void sendRemoteInfo() {
        var garrisonMap = CliDB.MapStorage.get(siteLevel.mapID);

        if (garrisonMap == null || owner.getLocation().getMapId() != garrisonMap.ParentMapID) {
            return;
        }

        GarrisonRemoteInfo remoteInfo = new GarrisonRemoteInfo();

        GarrisonRemoteSiteInfo remoteSiteInfo = new GarrisonRemoteSiteInfo();
        remoteSiteInfo.garrSiteLevelID = siteLevel.id;

        for (var p : plots.entrySet()) {
            if (p.getValue().buildingInfo.packetInfo != null) {
                remoteSiteInfo.buildings.add(new GarrisonRemoteBuildingInfo(p.getKey(), p.getValue().buildingInfo.packetInfo.garrBuildingID));
            }
        }

        remoteInfo.sites.add(remoteSiteInfo);
        owner.sendPacket(remoteInfo);
    }

    public final void sendBlueprintAndSpecializationData() {
        GarrisonRequestBlueprintAndSpecializationDataResult data = new GarrisonRequestBlueprintAndSpecializationDataResult();
        data.garrTypeID = getGarrisonType();
        data.blueprintsKnown = knownBuildings;
        owner.sendPacket(data);
    }

    public final void sendMapData(Player receiver) {
        GarrisonMapDataResponse mapData = new GarrisonMapDataResponse();

        for (var plot : plots.values()) {
            if (plot.buildingInfo.packetInfo != null) {
                var garrBuildingPlotInstId = global.getGarrisonMgr().getGarrBuildingPlotInst(plot.buildingInfo.packetInfo.garrBuildingID, plot.garrSiteLevelPlotInstId);

                if (garrBuildingPlotInstId != 0) {
                    mapData.buildings.add(new GarrisonBuildingMapData(garrBuildingPlotInstId, plot.packetInfo.plotPos));
                }
            }
        }

        receiver.sendPacket(mapData);
    }

    public final void resetFollowerActivationLimit() {
        followerActivationsRemainingToday = 1;
    }

    private void initializePlots() {
        var plots = global.getGarrisonMgr().getGarrPlotInstForSiteLevel(siteLevel.id);

        for (var i = 0; i < plots.size(); ++i) {
            int garrPlotInstanceId = plots.get(i).garrPlotInstanceID;
            var plotInstance = CliDB.GarrPlotInstanceStorage.get(garrPlotInstanceId);
            var gameObject = global.getGarrisonMgr().getPlotGameObject(siteLevel.mapID, garrPlotInstanceId);

            if (plotInstance == null || gameObject == null) {
                continue;
            }

            var plot = CliDB.GarrPlotStorage.get(plotInstance.GarrPlotID);

            if (plot == null) {
                continue;
            }

            var plotInfo = plots.get(garrPlotInstanceId);
            plotInfo.packetInfo.garrPlotInstanceID = garrPlotInstanceId;
            plotInfo.packetInfo.plotPos.relocate(gameObject.pos.X, gameObject.pos.Y, gameObject.pos.Z, 2 * (float) Math.acos(gameObject.Rot[3]));
            plotInfo.packetInfo.plotType = plot.plotType;
            plotInfo.rotation = new Quaternion(gameObject.Rot[0], gameObject.Rot[1], gameObject.Rot[2], gameObject.Rot[3]);
            plotInfo.emptyGameObjectId = gameObject.id;
            plotInfo.garrSiteLevelPlotInstId = plots.get(i).id;
        }
    }

    private void upgrade() {
    }

    private void enter() {
        WorldLocation loc = new worldLocation(siteLevel.mapID);
        loc.relocate(owner.getLocation());
        owner.teleportTo(loc, TeleportToOptions.Seamless);
    }

    private void leave() {
        var map = CliDB.MapStorage.get(siteLevel.mapID);

        if (map != null) {
            WorldLocation loc = new worldLocation((int) map.ParentMapID);
            loc.relocate(owner.getLocation());
            owner.teleportTo(loc, TeleportToOptions.Seamless);
        }
    }

    private void unlearnBlueprint(int garrBuildingId) {
        GarrisonUnlearnBlueprintResult unlearnBlueprintResult = new GarrisonUnlearnBlueprintResult();
        unlearnBlueprintResult.garrTypeID = getGarrisonType();
        unlearnBlueprintResult.buildingID = garrBuildingId;
        unlearnBlueprintResult.result = GarrisonError.success;

        if (!CliDB.GarrBuildingStorage.containsKey(garrBuildingId)) {
            unlearnBlueprintResult.result = GarrisonError.InvalidBuildingId;
        } else if (hasBlueprint(garrBuildingId)) {
            unlearnBlueprintResult.result = GarrisonError.RequiresBlueprint;
        } else {
            knownBuildings.remove((Integer) garrBuildingId);
        }

        owner.sendPacket(unlearnBlueprintResult);
    }

    private Map findMap() {
        return global.getMapMgr().findMap(siteLevel.mapID, (int) owner.getGUID().getCounter());
    }

    private GarrisonError checkBuildingPlacement(int garrPlotInstanceId, int garrBuildingId) {
        var plotInstance = CliDB.GarrPlotInstanceStorage.get(garrPlotInstanceId);
        var plot = getPlot(garrPlotInstanceId);

        if (plotInstance == null || plot == null) {
            return GarrisonError.InvalidPlotInstanceId;
        }

        var building = CliDB.GarrBuildingStorage.get(garrBuildingId);

        if (building == null) {
            return GarrisonError.InvalidBuildingId;
        }

        if (!global.getGarrisonMgr().isPlotMatchingBuilding(plotInstance.GarrPlotID, garrBuildingId)) {
            return GarrisonError.InvalidPlotBuilding;
        }

        // Cannot place buldings of higher level than garrison level
        if (building.UpgradeLevel > siteLevel.MaxBuildingLevel) {
            return GarrisonError.InvalidBuildingId;
        }

        if (building.flags.hasFlag(GarrisonBuildingFlags.NeedsPlan)) {
            if (hasBlueprint(garrBuildingId)) {
                return GarrisonError.RequiresBlueprint;
            }
        } else // Building is built as a quest reward
        {
            return GarrisonError.InvalidBuildingId;
        }

        // Check all plots to find if we already have this building
        GarrBuildingRecord existingBuilding;

        for (var p : plots.entrySet()) {
            if (p.getValue().buildingInfo.packetInfo != null) {
                existingBuilding = CliDB.GarrBuildingStorage.get(p.getValue().buildingInfo.packetInfo.garrBuildingID);

                if (existingBuilding.BuildingType == building.BuildingType) {
                    if (p.getKey() != garrPlotInstanceId || existingBuilding.UpgradeLevel + 1 != building.UpgradeLevel) // check if its an upgrade in same plot
                    {
                        return GarrisonError.BuildingExists;
                    }
                }
            }
        }

        if (!owner.hasCurrency(building.CurrencyTypeID, (int) building.currencyQty)) {
            return GarrisonError.NotEnoughCurrency;
        }

        if (!owner.hasEnoughMoney(building.GoldCost * MoneyConstants.gold)) {
            return GarrisonError.NotEnoughGold;
        }

        // New building cannot replace another building currently under construction
        if (plot.buildingInfo.packetInfo != null) {
            if (!plot.buildingInfo.packetInfo.active) {
                return GarrisonError.NoBuilding;
            }
        }

        return GarrisonError.success;
    }

    private GarrisonError checkBuildingRemoval(int garrPlotInstanceId) {
        var plot = getPlot(garrPlotInstanceId);

        if (plot == null) {
            return GarrisonError.InvalidPlotInstanceId;
        }

        if (plot.buildingInfo.packetInfo == null) {
            return GarrisonError.NoBuilding;
        }

        if (plot.buildingInfo.canActivate()) {
            return GarrisonError.BuildingExists;
        }

        return GarrisonError.success;
    }

    public static class Building {
        public ObjectGuid UUID = ObjectGuid.EMPTY;
        public ArrayList<ObjectGuid> spawns = new ArrayList<>();
        public GarrisonBuildingInfo packetInfo;

        public final boolean canActivate() {
            if (packetInfo != null) {
                var building = CliDB.GarrBuildingStorage.get(packetInfo.garrBuildingID);

                if (packetInfo.timeBuilt + building.BuildSeconds <= gameTime.GetGameTime()) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class Plot {
        public garrisonPlotInfo packetInfo = new garrisonPlotInfo();
        public Quaternion rotation;
        public int emptyGameObjectId;
        public int garrSiteLevelPlotInstId;
        public Building buildingInfo;

        public final GameObject createGameObject(Map map, int faction) {
            var entry = emptyGameObjectId;

            if (buildingInfo.packetInfo != null) {
                var plotInstance = CliDB.GarrPlotInstanceStorage.get(packetInfo.garrPlotInstanceID);
                var plot = CliDB.GarrPlotStorage.get(plotInstance.GarrPlotID);
                var building = CliDB.GarrBuildingStorage.get(buildingInfo.packetInfo.garrBuildingID);

                entry = faction == GarrisonFactionIndex.Horde ? plot.HordeConstructObjID : plot.AllianceConstructObjID;

                if (buildingInfo.packetInfo.active || entry == 0) {
                    entry = faction == GarrisonFactionIndex.Horde ? building.HordeGameObjectID : building.AllianceGameObjectID;
                }
            }

            if (global.getObjectMgr().getGameObjectTemplate(entry) == null) {
                Log.outError(LogFilter.Garrison, "Garrison attempted to spawn gameobject whose template doesn't exist ({0})", entry);

                return null;
            }

            var go = gameObject.createGameObject(entry, map, packetInfo.plotPos, rotation, 255, GOState.Ready);

            if (!go) {
                return null;
            }

            if (buildingInfo.canActivate() && buildingInfo.packetInfo != null && !buildingInfo.packetInfo.active) {
                var finalizeInfo = global.getGarrisonMgr().getPlotFinalizeGOInfo(packetInfo.garrPlotInstanceID);

                if (finalizeInfo != null) {
                    var pos2 = finalizeInfo.factionInfo[faction].pos;
                    var finalizer = gameObject.createGameObject(finalizeInfo.factionInfo[faction].gameObjectId, map, pos2, Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos2.getO(), 0.0f, 0.0f)), 255, GOState.Ready);

                    if (finalizer) {
                        // set some spell id to make the object delete itself after use
                        finalizer.setSpellId(finalizer.getTemplate().goober.spell);

                        finalizer.setRespawnTime(0);

                        var animKit = finalizeInfo.factionInfo[faction].animKitId;

                        if (animKit != 0) {
                            finalizer.setAnimKitId(animKit, false);
                        }

                        map.addToMap(finalizer);
                    }
                }
            }

            if (go.getGoType() == GameObjectTypes.garrisonBuilding && go.getTemplate().garrisonBuilding.spawnMap != 0) {
                for (var cellGuids : global.getObjectMgr().getMapObjectGuids(new integer(go.getTemplate().garrisonBuilding.spawnMap, map.getDifficultyID())).entrySet()) {
                    for (var spawnId : cellGuids.getValue().creatures) {
                        var spawn = this.<Creature>BuildingSpawnHelper(go, spawnId, map);

                        if (spawn) {
                            buildingInfo.spawns.add(spawn.getGUID());
                        }
                    }

                    for (var spawnId : cellGuids.getValue().gameobjects) {
                        var spawn = this.<GameObject>BuildingSpawnHelper(go, spawnId, map);

                        if (spawn) {
                            buildingInfo.spawns.add(spawn.getGUID());
                        }
                    }
                }
            }

            buildingInfo.guid = go.getGUID();

            return go;
        }

        public final void deleteGameObject(Map map) {
            if (buildingInfo.guid.isEmpty()) {
                return;
            }

            for (var guid : buildingInfo.spawns) {
                WorldObject obj;

                switch (guid.getHigh()) {
                    case Creature:
                        obj = map.getCreature(guid);

                        break;
                    case GameObject:
                        obj = map.getGameObject(guid);

                        break;
                    default:
                        continue;
                }

                if (obj) {
                    obj.addObjectToRemoveList();
                }
            }

            buildingInfo.spawns.clear();

            var oldBuilding = map.getGameObject(buildingInfo.guid);

            if (oldBuilding) {
                oldBuilding.delete();
            }

            buildingInfo.guid.clear();
        }

        public final void clearBuildingInfo(GarrisonType garrisonType, Player owner) {
            GarrisonPlotPlaced plotPlaced = new GarrisonPlotPlaced();
            plotPlaced.garrTypeID = garrisonType;
            plotPlaced.plotInfo = packetInfo;
            owner.sendPacket(plotPlaced);

            buildingInfo.packetInfo = null;
        }

        public final void setBuildingInfo(GarrisonBuildingInfo buildingInfo, Player owner) {
            if (buildingInfo.packetInfo == null) {
                GarrisonPlotRemoved plotRemoved = new GarrisonPlotRemoved();
                plotRemoved.garrPlotInstanceID = packetInfo.garrPlotInstanceID;
                owner.sendPacket(plotRemoved);
            }

            buildingInfo.packetInfo = buildingInfo;
        }

        
        private <T extends WorldObject> T buildingSpawnHelper(GameObject building, long spawnId, Map map) {
            T spawn = new T();

            if (!spawn.loadFromDB(spawnId, map, false, false)) {
                return null;
            }

            var pos = spawn.getLocation().Copy();
            itransport.calculatePassengerPosition(pos, building.getLocation().getX(), building.getLocation().getY(), building.getLocation().getZ(), building.getLocation().getO());

            spawn.getLocation().relocate(pos);

            switch (spawn.getObjectTypeId()) {
                case Unit:
                    spawn.toCreature().setHomePosition(pos);

                    break;
                case GameObject:
                    spawn.toGameObject().relocateStationaryPosition(pos);

                    break;
            }

            if (!spawn.getLocation().isPositionValid()) {
                return null;
            }

            if (!map.addToMap(spawn)) {
                return null;
            }

            return spawn;
        }
    }

    public static class Follower {
        public GarrisonFollower packetInfo = new GarrisonFollower();

        public final int getItemLevel() {
            return (packetInfo.itemLevelWeapon + packetInfo.itemLevelArmor) / 2;
        }

        public final boolean hasAbility(int garrAbilityId) {
            return packetInfo.abilityID.Any(garrAbility ->
            {
                return garrAbility.id == garrAbilityId;
            });
        }
    }
}
