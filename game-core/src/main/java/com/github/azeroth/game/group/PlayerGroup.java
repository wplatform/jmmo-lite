package com.github.azeroth.game.group;


import com.github.azeroth.common.Pair;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.MapEntry;
import com.github.azeroth.defines.ItemQuality;
import com.github.azeroth.defines.LootMethod;
import com.github.azeroth.defines.RemoveMethod;
import com.github.azeroth.game.battlefield.BattleField;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.packet.party.PartyLFGInfo;
import com.github.azeroth.game.networking.packet.party.PartyUpdate;
import com.github.azeroth.game.listener.interfaces.igroup.*;
import com.github.azeroth.reference.RefManager;
import com.github.azeroth.game.world.WorldSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static java.util.logging.Logger.global;


public class PlayerGroup extends RefManager<PlayerGroup, Player, GroupReference> {
    private final ArrayList<MemberSlot> memberSlots = new ArrayList<>();
    private final GroupRefManager memberMgr = new GroupRefManager();
    private final ArrayList<Player> invitees = new ArrayList<>();
    private final ObjectGuid[] targetIcons = new ObjectGuid[ObjectDefine.TargetIconsCount];
    private final HashMap<Integer, Pair<ObjectGuid, Integer>> recentInstances = new HashMap<>();
    private final GroupInstanceRefManager instanceRefManager = new GroupInstanceRefManager();
    private final timeTracker leaderOfflineTimer = new timeTracker();

    // Raid markers
    private final RaidMarker[] markers = new RaidMarker[MapDefine.RaidMarkersCount];
    private ObjectGuid leaderGuid = ObjectGuid.EMPTY;
    private byte leaderFactionGroup;
    private String leaderName;
    private Difficulty dungeonDifficulty = Difficulty.values()[0];    private GroupFlags groupFlags = getGroupFlags().values()[0];
    private Difficulty raidDifficulty = Difficulty.values()[0];    private GroupCategory groupCategory = getGroupCategory().values()[0];
    private Difficulty legacyRaidDifficulty = Difficulty.values()[0];
    private Battleground bgGroup;
    private BattleField bfGroup;
    private ItemQuality lootThreshold = itemQuality.values()[0];
    private ObjectGuid looterGuid = ObjectGuid.EMPTY;
    private ObjectGuid masterLooterGuid = ObjectGuid.EMPTY;    private LootMethod lootMethod = getLootMethod().values()[0];
    private byte[] subGroupsCounts;
    private ObjectGuid guid = ObjectGuid.EMPTY;
    private int dbStoreId;
    private boolean isLeaderOffline;
    // Ready Check
    private boolean readyCheckStarted;
    private duration readyCheckTimer = new duration();
    private int activeMarkers;

    public PlayerGroup() {
        leaderName = "";
        groupFlags = GroupFlags.NONE;
        dungeonDifficulty = Difficulty.NORMAL;
        raidDifficulty = Difficulty.NormalRaid;
        legacyRaidDifficulty = Difficulty.Raid10N;
        lootMethod = lootMethod.personalLoot;
        lootThreshold = itemQuality.Uncommon;
    }

    public final Difficulty getDungeonDifficultyID() {
        return dungeonDifficulty;
    }

    public final void setDungeonDifficultyID(Difficulty difficulty) {
        dungeonDifficulty = difficulty;

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_DIFFICULTY);

            stmt.AddValue(0, (byte) dungeonDifficulty.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (player.getSession() == null) {
                continue;
            }

            player.setDungeonDifficultyId(difficulty);
            player.sendDungeonDifficulty();
        }
    }

    public final Difficulty getRaidDifficultyID() {
        return raidDifficulty;
    }

    public final void setRaidDifficultyID(Difficulty difficulty) {
        raidDifficulty = difficulty;

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_RAID_DIFFICULTY);

            stmt.AddValue(0, (byte) raidDifficulty.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (player.getSession() == null) {
                continue;
            }

            player.setRaidDifficultyId(difficulty);
            player.sendRaidDifficulty(false);
        }
    }

    public final Difficulty getLegacyRaidDifficultyID() {
        return legacyRaidDifficulty;
    }

    public final void setLegacyRaidDifficultyID(Difficulty difficulty) {
        legacyRaidDifficulty = difficulty;

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_LEGACY_RAID_DIFFICULTY);

            stmt.AddValue(0, (byte) legacyRaidDifficulty.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (player.getSession() == null) {
                continue;
            }

            player.setLegacyRaidDifficultyId(difficulty);
            player.sendRaidDifficulty(true);
        }
    }

    private boolean isReadyCheckCompleted() {
        for (var member : memberSlots) {
            if (!member.readyChecked) {
                return false;
            }
        }

        return true;
    }

    public final boolean isFull() {
        return isRaidGroup() ? (memberSlots.size() >= MapDefine.MaxRaidSize) : (memberSlots.size() >= MapDefine.MaxGroupSize);
    }

    public final boolean isLFGGroup() {
        return groupFlags.hasFlag(GroupFlags.Lfg);
    }

    public final boolean isRaidGroup() {
        return groupFlags.hasFlag(GroupFlags.raid);
    }

    public final boolean isBGGroup() {
        return bgGroup != null;
    }

    public final boolean isBFGroup() {
        return bfGroup != null;
    }

    public final boolean isCreated() {
        return getMembersCount() > 0;
    }

    public final ObjectGuid getLeaderGUID() {
        return leaderGuid;
    }

    public final ObjectGuid getGUID() {
        return guid;
    }

    public final long getLowGUID() {
        return guid.getCounter();
    }

    public final String getLeaderName() {
        return leaderName;
    }

    public final LootMethod getLootMethod() {
        return lootMethod;
    }

    public final void setLootMethod(LootMethod method) {
        lootMethod = method;
    }

    public final ObjectGuid getLooterGuid() {
        if (getLootMethod() == LootMethod.FREE_FOR_ALL) {
            return ObjectGuid.EMPTY;
        }
        return looterGuid;
    }

    public final void setLooterGuid(ObjectGuid guid) {
        looterGuid = guid;
    }

    public final ObjectGuid getMasterLooterGuid() {
        return masterLooterGuid;
    }

    public final void setMasterLooterGuid(ObjectGuid guid) {
        masterLooterGuid = guid;
    }

    public final ItemQuality getLootThreshold() {
        return lootThreshold;
    }

    public final void setLootThreshold(ItemQuality threshold) {
        lootThreshold = threshold;
    }

    public final GroupCategory getGroupCategory() {
        return groupCategory;
    }

    public final int getDbStoreId() {
        return dbStoreId;
    }

    public final ArrayList<MemberSlot> getMemberSlots() {
        return memberSlots;
    }

    public final GroupReference getFirstMember() {
        return memberMgr.getFirst();
    }

    public final int getMembersCount() {
        return (int) memberSlots.size();
    }

    public final int getInviteeCount() {
        return (int) invitees.size();
    }

    public final GroupFlags getGroupFlags() {
        return groupFlags;
    }

    public final boolean isReadyCheckStarted() {
        return readyCheckStarted;
    }

    public final void update(int diff) {
        if (isLeaderOffline) {
            leaderOfflineTimer.update(diff);

            if (leaderOfflineTimer.Passed) {
                selectNewPartyOrRaidLeader();
                isLeaderOffline = false;
            }
        }

        updateReadyCheck(diff);
    }

    public final boolean create(Player leader) {
        var leaderGuid = leader.getGUID();

        guid = ObjectGuid.create(HighGuid.Party, global.getGroupMgr().generateGroupId());
        leaderGuid = leaderGuid;
        leaderFactionGroup = player.getFactionGroupForRace(leader.getRace());
        leaderName = leader.getName();
        leader.setPlayerFlag(PlayerFlag.GroupLeader);

        if (isBGGroup() || isBFGroup()) {
            groupFlags = GroupFlags.MaskBgRaid;
            groupCategory = GroupCategory.instance;
        }

        if (groupFlags.hasFlag(GroupFlags.raid)) {
            initRaidSubGroupsCounter();
        }

        lootThreshold = itemQuality.Uncommon;
        looterGuid = leaderGuid;

        dungeonDifficulty = Difficulty.NORMAL;
        raidDifficulty = Difficulty.NormalRaid;
        legacyRaidDifficulty = Difficulty.Raid10N;

        if (!isBGGroup() && !isBFGroup()) {
            dungeonDifficulty = leader.getDungeonDifficultyId();
            raidDifficulty = leader.getRaidDifficultyId();
            legacyRaidDifficulty = leader.getLegacyRaidDifficultyId();

            dbStoreId = global.getGroupMgr().generateNewGroupDbStoreId();

            global.getGroupMgr().registerGroupDbStoreId(dbStoreId, this);

            // Store group in database
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GROUP);

            byte index = 0;

            stmt.AddValue(index++, dbStoreId);
            stmt.AddValue(index++, leaderGuid.getCounter());
            stmt.AddValue(index++, (byte) lootMethod.getValue());
            stmt.AddValue(index++, looterGuid.getCounter());
            stmt.AddValue(index++, (byte) lootThreshold.getValue());
            stmt.AddValue(index++, _targetIcons[0].GetRawValue());
            stmt.AddValue(index++, _targetIcons[1].GetRawValue());
            stmt.AddValue(index++, _targetIcons[2].GetRawValue());
            stmt.AddValue(index++, _targetIcons[3].GetRawValue());
            stmt.AddValue(index++, _targetIcons[4].GetRawValue());
            stmt.AddValue(index++, _targetIcons[5].GetRawValue());
            stmt.AddValue(index++, _targetIcons[6].GetRawValue());
            stmt.AddValue(index++, _targetIcons[7].GetRawValue());
            stmt.AddValue(index++, (byte) groupFlags.getValue());
            stmt.AddValue(index++, (byte) dungeonDifficulty.getValue());
            stmt.AddValue(index++, (byte) raidDifficulty.getValue());
            stmt.AddValue(index++, (byte) legacyRaidDifficulty.getValue());
            stmt.AddValue(index++, masterLooterGuid.getCounter());

            DB.characters.execute(stmt);

            var leaderInstance = leader.getMap().toInstanceMap();

            if (leaderInstance != null) {
                leaderInstance.trySetOwningGroup(this);
            }

            addMember(leader); // If the leader can't be added to a new group because it appears full, something is clearly wrong.
        } else if (!addMember(leader)) {
            return false;
        }

        return true;
    }

    public final void loadGroupFromDB(SQLFields field) {
        dbStoreId = field.<Integer>Read(17);
        guid = ObjectGuid.create(HighGuid.Party, global.getGroupMgr().generateGroupId());
        leaderGuid = ObjectGuid.create(HighGuid.Player, field.<Long>Read(0));

        // group leader not exist
        var leader = global.getCharacterCacheStorage().getCharacterCacheByGuid(leaderGuid);

        if (leader == null) {
            return;
        }

        leaderFactionGroup = player.getFactionGroupForRace(leader.raceId);
        leaderName = leader.name;
        lootMethod = lootMethod.forValue(field.<Byte>Read(1));
        looterGuid = ObjectGuid.create(HighGuid.Player, field.<Long>Read(2));
        lootThreshold = itemQuality.forValue(field.<Byte>Read(3));

        for (byte i = 0; i < MapDefine.TargetIconsCount; ++i) {
            _targetIcons[i].SetRawValue(field.<byte[]>Read(4 + i));
        }

        groupFlags = GroupFlags.forValue(field.<Byte>Read(12));

        if (groupFlags.hasFlag(GroupFlags.raid)) {
            initRaidSubGroupsCounter();
        }

        dungeonDifficulty = player.checkLoadedDungeonDifficultyId(Difficulty.forValue(field.<Byte>Read(13)));
        raidDifficulty = player.checkLoadedRaidDifficultyId(Difficulty.forValue(field.<Byte>Read(14)));
        legacyRaidDifficulty = player.checkLoadedLegacyRaidDifficultyId(Difficulty.forValue(field.<Byte>Read(15)));

        masterLooterGuid = ObjectGuid.create(HighGuid.Player, field.<Long>Read(16));

        if (groupFlags.hasFlag(GroupFlags.Lfg)) {
            global.getLFGMgr()._LoadFromDB(field, getGUID());
        }
    }

    public final void loadMemberFromDB(long guidLow, byte memberFlags, byte subgroup, LfgRoles roles) {
        MemberSlot member = new MemberSlot();
        member.guid = ObjectGuid.create(HighGuid.Player, guidLow);

        // skip non-existed member
        var character = global.getCharacterCacheStorage().getCharacterCacheByGuid(member.guid);

        if (character == null) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GROUP_MEMBER);
            stmt.AddValue(0, guidLow);
            DB.characters.execute(stmt);

            return;
        }

        member.name = character.name;
        member.race = character.raceId;
        member.class = (byte) character.classId.getValue();
        member.group = subgroup;
        member.flags = GroupMemberFlags.forValue(memberFlags);
        member.roles = roles;
        member.readyChecked = false;

        memberSlots.add(member);

        subGroupCounterIncrease(subgroup);

        global.getLFGMgr().setupGroupMember(member.guid, getGUID());
    }

    public final void convertToLFG() {
        groupFlags = GroupFlags.forValue(groupFlags.getValue() | GroupFlags.Lfg.getValue().getValue() | GroupFlags.LfgRestricted.getValue().getValue());
        groupCategory = GroupCategory.instance;
        lootMethod = lootMethod.personalLoot;

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_TYPE);

            stmt.AddValue(0, (byte) groupFlags.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        sendUpdate();
    }

    public final void convertToRaid() {
        groupFlags = GroupFlags.forValue(groupFlags.getValue() | GroupFlags.raid.getValue());

        initRaidSubGroupsCounter();

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_TYPE);

            stmt.AddValue(0, (byte) groupFlags.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        sendUpdate();

        // update quest related GO states (quest activity dependent from raid membership)
        for (var member : memberSlots) {
            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player != null) {
                player.updateVisibleGameobjectsOrSpellClicks();
            }
        }
    }

    public final void convertToGroup() {
        if (memberSlots.size() > 5) {
            return; // What message error should we send?
        }

        groupFlags = GroupFlags.NONE;

        subGroupsCounts = null;

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_TYPE);

            stmt.AddValue(0, (byte) groupFlags.getValue());
            stmt.AddValue(1, dbStoreId);

            DB.characters.execute(stmt);
        }

        sendUpdate();

        // update quest related GO states (quest activity dependent from raid membership)
        for (var member : memberSlots) {
            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player != null) {
                player.updateVisibleGameobjectsOrSpellClicks();
            }
        }
    }

    public final boolean addInvite(Player player) {
        if (player == null || player.getGroupInvite()) {
            return false;
        }

        var group = player.getGroup();

        if (group && (group.isBGGroup() || group.isBFGroup())) {
            group = player.getOriginalGroup();
        }

        if (group) {
            return false;
        }

        removeInvite(player);

        invitees.add(player);

        player.setGroupInvite(this);

        global.getScriptMgr().<IGroupOnInviteMember>ForEach(p -> p.onInviteMember(this, player.getGUID()));

        return true;
    }

    public final boolean addLeaderInvite(Player player) {
        if (!addInvite(player)) {
            return false;
        }

        leaderGuid = player.getGUID();
        leaderFactionGroup = player.getFactionGroupForRace(player.getRace());
        leaderName = player.getName();

        return true;
    }

    public final void removeInvite(Player player) {
        if (player != null) {
            invitees.remove(player);
            player.setGroupInvite(null);
        }
    }

    public final void removeAllInvites() {
        for (var pl : invitees) {
            if (pl != null) {
                pl.setGroupInvite(null);
            }
        }

        invitees.clear();
    }

    public final Player getInvited(ObjectGuid guid) {
        for (var pl : invitees) {
            if (pl != null && Objects.equals(pl.getGUID(), guid)) {
                return pl;
            }
        }

        return null;
    }

    public final Player getInvited(String name) {
        for (var pl : invitees) {
            if (pl != null && Objects.equals(pl.getName(), name)) {
                return pl;
            }
        }

        return null;
    }

    public final boolean addMember(Player player) {
        // Get first not-full group
        byte subGroup = 0;

        if (subGroupsCounts != null) {
            var groupFound = false;

            for (; subGroup < MapDefine.MaxRaidSubGroups; ++subGroup) {
                if (_subGroupsCounts[subGroup] < MapDefine.MaxGroupSize) {
                    groupFound = true;

                    break;
                }
            }

            // We are raid group and no one slot is free
            if (!groupFound) {
                return false;
            }
        }

        MemberSlot member = new MemberSlot();
        member.guid = player.getGUID();
        member.name = player.getName();
        member.race = player.getRace();
        member.class = (byte) player.getUnitClass().getValue();
        member.group = subGroup;
        member.flags = GroupMemberFlags.forValue(0);
        member.roles = LfgRoles.forValue(0);
        member.readyChecked = false;
        memberSlots.add(member);

        subGroupCounterIncrease(subGroup);

        player.setGroupInvite(null);

        if (player.getGroup() != null) {
            if (isBGGroup() || isBFGroup()) // if player is in group and he is being added to BG raid group, then call SetBattlegroundRaid()
            {
                player.setBattlegroundOrBattlefieldRaid(this, subGroup);
            } else //if player is in bg raid and we are adding him to normal group, then call setOriginalGroup()
            {
                player.setOriginalGroup(this, subGroup);
            }
        } else //if player is not in group, then call set group
        {
            player.setGroup(this, subGroup);
        }

        player.setPartyType(groupCategory, GroupType.NORMAL);
        player.resetGroupUpdateSequenceIfNeeded(this);

        // if the same group invites the player back, cancel the homebind timer
        player.setInstanceValid(player.checkInstanceValidity(false));

        if (!isRaidGroup()) // reset targetIcons for non-raid-groups
        {
            for (byte i = 0; i < MapDefine.TargetIconsCount; ++i) {
                _targetIcons[i].clear();
            }
        }

        // insert into the table if we're not a Battlegroundgroup
        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GROUP_MEMBER);

            stmt.AddValue(0, dbStoreId);
            stmt.AddValue(1, member.guid.getCounter());
            stmt.AddValue(2, (byte) member.flags.getValue());
            stmt.AddValue(3, member.group);
            stmt.AddValue(4, (byte) member.roles.getValue());

            DB.characters.execute(stmt);
        }

        sendUpdate();
        global.getScriptMgr().<IGroupOnAddMember>ForEach(p -> p.onAddMember(this, player.getGUID()));

        if (!isLeader(player.getGUID()) && !isBGGroup() && !isBFGroup()) {
            if (player.getDungeonDifficultyId() != getDungeonDifficultyID()) {
                player.setDungeonDifficultyId(getDungeonDifficultyID());
                player.sendDungeonDifficulty();
            }

            if (player.getRaidDifficultyId() != getRaidDifficultyID()) {
                player.setRaidDifficultyId(getRaidDifficultyID());
                player.sendRaidDifficulty(false);
            }

            if (player.getLegacyRaidDifficultyId() != getLegacyRaidDifficultyID()) {
                player.setLegacyRaidDifficultyId(getLegacyRaidDifficultyID());
                player.sendRaidDifficulty(true);
            }
        }

        player.setGroupUpdateFlag(GroupUpdateFlags.Full);
        var pet = player.getCurrentPet();

        if (pet) {
            pet.setGroupUpdateFlag(GroupUpdatePetFlags.Full);
        }

        updatePlayerOutOfRange(player);

        // quest related GO state dependent from raid membership
        if (isRaidGroup()) {
            player.updateVisibleGameobjectsOrSpellClicks();
        }

        {
            // Broadcast new player group member fields to rest of the group
            UpdateData groupData = new UpdateData(player.getLocation().getMapId());

            // Broadcast group members' fields to player
            for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
                if (refe.getSource() == player) {
                    continue;
                }

                var existingMember = refe.getSource();

                if (existingMember != null) {
                    if (player.haveAtClient(existingMember)) {
                        existingMember.buildValuesUpdateBlockForPlayerWithFlag(groupData, UpdateFieldFlag.PartyMember, player);
                    }

                    if (existingMember.haveAtClient(player)) {
                        UpdateData newData = new UpdateData(player.getLocation().getMapId());
                        player.buildValuesUpdateBlockForPlayerWithFlag(newData, UpdateFieldFlag.PartyMember, existingMember);

                        if (newData.hasData()) {
                            UpdateObject newDataPacket;
                            tangible.OutObject<UpdateObject> tempOut_newDataPacket = new tangible.OutObject<UpdateObject>();
                            newData.buildPacket(tempOut_newDataPacket);
                            newDataPacket = tempOut_newDataPacket.outArgValue;
                            existingMember.sendPacket(newDataPacket);
                        }
                    }
                }
            }

            if (groupData.hasData()) {
                UpdateObject groupDataPacket;
                tangible.OutObject<UpdateObject> tempOut_groupDataPacket = new tangible.OutObject<UpdateObject>();
                groupData.buildPacket(tempOut_groupDataPacket);
                groupDataPacket = tempOut_groupDataPacket.outArgValue;
                player.sendPacket(groupDataPacket);
            }
        }

        return true;
    }

    public final boolean removeMember(ObjectGuid guid, RemoveMethod method, ObjectGuid kicker) {
        return removeMember(guid, method, kicker, null);
    }

    public final boolean removeMember(ObjectGuid guid, RemoveMethod method) {
        return removeMember(guid, method, null, null);
    }

    public final boolean removeMember(ObjectGuid guid) {
        return removeMember(guid, RemoveMethod.Default, null, null);
    }

    public final boolean removeMember(ObjectGuid guid, RemoveMethod method, ObjectGuid kicker, String reason) {
        broadcastGroupUpdate();

        global.getScriptMgr().<IGroupOnRemoveMember>ForEach(p -> p.onRemoveMember(this, guid, method, kicker, reason));

        var player = global.getObjAccessor().findConnectedPlayer(guid);

        if (player) {
            for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
                var groupMember = refe.getSource();

                if (groupMember) {
                    if (Objects.equals(groupMember.getGUID(), guid)) {
                        continue;
                    }

                    groupMember.removeAllGroupBuffsFromCaster(guid);
                    player.removeAllGroupBuffsFromCaster(groupMember.getGUID());
                }
            }
        }

        // LFG group vote kick handled in scripts
        if (isLFGGroup() && method == RemoveMethod.kick) {
            return !memberSlots.isEmpty();
        }

        // remove member and change leader (if need) only if strong more 2 members _before_ member remove (BG/BF allow 1 member group)
        if (getMembersCount() > ((isBGGroup() || isLFGGroup() || isBFGroup()) ? 1 : 2)) {
            if (player) {
                // Battlegroundgroup handling
                if (isBGGroup() || isBFGroup()) {
                    player.removeFromBattlegroundOrBattlefieldRaid();
                } else {
                    // Regular group
                    if (player.getOriginalGroup() == this) {
                        player.setOriginalGroup(null);
                    } else {
                        player.setGroup(null);
                    }

                    // quest related GO state dependent from raid membership
                    player.updateVisibleGameobjectsOrSpellClicks();
                }

                player.setPartyType(groupCategory, GroupType.NONE);

                if (method == RemoveMethod.kick || method == RemoveMethod.KickLFG) {
                    player.sendPacket(new GroupUninvite());
                }

                homebindIfInstance(player);
            }

            // Remove player from group in DB
            if (!isBGGroup() && !isBFGroup()) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GROUP_MEMBER);
                stmt.AddValue(0, guid.getCounter());
                DB.characters.execute(stmt);
                delinkMember(guid);
            }

            // Update subgroups
            var slot = getMemberSlot(guid);

            if (slot != null) {
                subGroupCounterDecrease(slot.group);
                memberSlots.remove(slot);
            }

            // Pick new leader if necessary
            if (Objects.equals(leaderGuid, guid)) {
                for (var member : memberSlots) {
                    if (global.getObjAccessor().findPlayer(member.guid) != null) {
                        changeLeader(member.guid);

                        break;
                    }
                }
            }

            sendUpdate();

            if (isLFGGroup() && getMembersCount() == 1) {
                var leader = global.getObjAccessor().findPlayer(getLeaderGUID());
                var mapId = global.getLFGMgr().getDungeonMapId(getGUID());

                if (mapId == 0 || leader == null || (leader.isAlive() && leader.getLocation().getMapId() != mapId)) {
                    disband();

                    return false;
                }
            }

            if (memberMgr.getSize() < ((isLFGGroup() || isBGGroup()) ? 1 : 2)) {
                disband();
            } else if (player) {
                // send update to removed player too so party frames are destroyed clientside
                sendUpdateDestroyGroupToPlayer(player);
            }

            return true;
        }
        // If group size before player removal <= 2 then disband it
        else {
            disband();

            return false;
        }
    }

    public final void changeLeader(ObjectGuid newLeaderGuid) {
        changeLeader(newLeaderGuid, 0);
    }

    public final void changeLeader(ObjectGuid newLeaderGuid, byte partyIndex) {
        var slot = getMemberSlot(newLeaderGuid);

        if (slot == null) {
            return;
        }

        var newLeader = global.getObjAccessor().findPlayer(slot.guid);

        // Don't allow switching leader to offline players
        if (newLeader == null) {
            return;
        }

        global.getScriptMgr().<IGroupOnChangeLeader>ForEach(p -> p.onChangeLeader(this, newLeaderGuid, leaderGuid));

        if (!isBGGroup() && !isBFGroup()) {
            PreparedStatement stmt;
            SQLTransaction trans = new SQLTransaction();

            // Update the group leader
            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_LEADER);

            stmt.AddValue(0, newLeader.getGUID().getCounter());
            stmt.AddValue(1, dbStoreId);

            trans.append(stmt);

            DB.characters.CommitTransaction(trans);
        }

        var oldLeader = global.getObjAccessor().findConnectedPlayer(leaderGuid);

        if (oldLeader) {
            oldLeader.removePlayerFlag(PlayerFlag.GroupLeader);
        }

        newLeader.setPlayerFlag(PlayerFlag.GroupLeader);
        leaderGuid = newLeader.getGUID();
        leaderFactionGroup = player.getFactionGroupForRace(newLeader.getRace());
        leaderName = newLeader.getName();
        toggleGroupMemberFlag(slot, GroupMemberFlags.Assistant, false);

        GroupNewLeader groupNewLeader = new GroupNewLeader();
        groupNewLeader.name = leaderName;
        groupNewLeader.partyIndex = partyIndex;
        broadcastPacket(groupNewLeader, true);
    }

    public final void disband() {
        disband(false);
    }

    public final void disband(boolean hideDestroy) {
        global.getScriptMgr().<IGroupOnDisband>ForEach(p -> p.onDisband(this));

        Player player;

        for (var member : memberSlots) {
            player = global.getObjAccessor().findPlayer(member.guid);

            if (player == null) {
                continue;
            }

            //we cannot call _removeMember because it would invalidate member iterator
            //if we are removing player from Battlegroundraid
            if (isBGGroup() || isBFGroup()) {
                player.removeFromBattlegroundOrBattlefieldRaid();
            } else {
                //we can remove player who is in Battlegroundfrom his original group
                if (player.getOriginalGroup() == this) {
                    player.setOriginalGroup(null);
                } else {
                    player.setGroup(null);
                }
            }

            player.setPartyType(groupCategory, GroupType.NONE);

            // quest related GO state dependent from raid membership
            if (isRaidGroup()) {
                player.updateVisibleGameobjectsOrSpellClicks();
            }

            if (!hideDestroy) {
                player.sendPacket(new GroupDestroyed());
            }

            sendUpdateDestroyGroupToPlayer(player);

            homebindIfInstance(player);
        }

        memberSlots.clear();

        removeAllInvites();

        if (!isBGGroup() && !isBFGroup()) {
            SQLTransaction trans = new SQLTransaction();

            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GROUP);
            stmt.AddValue(0, dbStoreId);
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GROUP_MEMBER_ALL);
            stmt.AddValue(0, dbStoreId);
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_LFG_DATA);
            stmt.AddValue(0, dbStoreId);
            trans.append(stmt);

            DB.characters.CommitTransaction(trans);

            global.getGroupMgr().freeGroupDbStoreId(this);
        }

        global.getGroupMgr().removeGroup(this);
    }

    public final void setTargetIcon(byte symbol, ObjectGuid target, ObjectGuid changedBy, byte partyIndex) {
        if (symbol >= MapDefine.TargetIconsCount) {
            return;
        }

        // clean other icons
        if (!target.isEmpty()) {
            for (byte i = 0; i < MapDefine.TargetIconsCount; ++i) {
                if (Objects.equals(_targetIcons[i], target)) {
                    setTargetIcon(i, ObjectGuid.Empty, changedBy, partyIndex);
                }
            }
        }

        _targetIcons[symbol] = target;

        SendRaidTargetUpdateSingle updateSingle = new SendRaidTargetUpdateSingle();
        updateSingle.partyIndex = partyIndex;
        updateSingle.target = target;
        updateSingle.changedBy = changedBy;
        updateSingle.symbol = (byte) symbol;
        broadcastPacket(updateSingle, true);
    }

    public final void sendTargetIconList(WorldSession session, byte partyIndex) {
        if (session == null) {
            return;
        }

        SendRaidTargetUpdateAll updateAll = new SendRaidTargetUpdateAll();
        updateAll.partyIndex = partyIndex;

        for (byte i = 0; i < MapDefine.TargetIconsCount; i++) {
            updateAll.targetIcons.put(i, _targetIcons[i]);
        }

        session.sendPacket(updateAll);
    }

    public final void sendUpdate() {
        for (var member : memberSlots) {
            sendUpdateToPlayer(member.guid, member);
        }
    }

    public final void sendUpdateToPlayer(ObjectGuid playerGUID) {
        sendUpdateToPlayer(playerGUID, null);
    }

    public final void sendUpdateToPlayer(ObjectGuid playerGUID, MemberSlot memberSlot) {
        var player = global.getObjAccessor().findPlayer(playerGUID);

        if (player == null || player.getSession() == null || player.getGroup() != this) {
            return;
        }

        // if MemberSlot wasn't provided
        if (memberSlot == null) {
            var slot = getMemberSlot(playerGUID);

            if (slot == null) // if there is no MemberSlot for such a player
            {
                return;
            }

            memberSlot = slot;
        }

        PartyUpdate partyUpdate = new PartyUpdate();

        partyUpdate.partyFlags = groupFlags;
        partyUpdate.partyIndex = (byte) groupCategory.getValue();
        partyUpdate.partyType = isCreated() ? GroupType.Normal : GroupType.NONE;

        partyUpdate.partyGUID = guid;
        partyUpdate.leaderGUID = leaderGuid;
        partyUpdate.leaderFactionGroup = leaderFactionGroup;

        partyUpdate.sequenceNum = player.nextGroupUpdateSequenceNumber(groupCategory);

        partyUpdate.myIndex = -1;
        byte index = 0;

        for (var i = 0; i < memberSlots.size(); ++i, ++index) {
            var member = memberSlots.get(i);

            if (Objects.equals(memberSlot.guid, member.guid)) {
                partyUpdate.myIndex = index;
            }

            var memberPlayer = global.getObjAccessor().findConnectedPlayer(member.guid);

            PartyPlayerInfo playerInfos = new PartyPlayerInfo();

            playerInfos.GUID = member.guid;
            playerInfos.name = member.name;
            playerInfos.class = member.class;

            playerInfos.factionGroup = player.getFactionGroupForRace(member.race);

            playerInfos.connected = memberPlayer == null ? null : memberPlayer.getSession() != null && !memberPlayer.getSession().getPlayerLogout();

            playerInfos.subgroup = member.group; // groupid
            playerInfos.flags = (byte) member.flags.getValue(); // See enum GroupMemberFlags
            playerInfos.rolesAssigned = (byte) member.roles.getValue(); // Lfg Roles

            partyUpdate.playerList.add(playerInfos);
        }

        if (getMembersCount() > 1) {
            // LootSettings
            PartyLootSettings lootSettings = new PartyLootSettings();

            lootSettings.method = (byte) lootMethod.getValue();
            lootSettings.threshold = (byte) lootThreshold.getValue();
            lootSettings.lootMaster = lootMethod == lootMethod.MasterLoot ? _masterLooterGuid : ObjectGuid.Empty;

            partyUpdate.lootSettings = lootSettings;

            // Difficulty Settings
            PartyDifficultySettings difficultySettings = new PartyDifficultySettings();

            difficultySettings.dungeonDifficultyID = (int) dungeonDifficulty.getValue();
            difficultySettings.raidDifficultyID = (int) raidDifficulty.getValue();
            difficultySettings.legacyRaidDifficultyID = (int) legacyRaidDifficulty.getValue();

            partyUpdate.difficultySettings = difficultySettings;
        }

        // LfgInfos
        if (isLFGGroup()) {
            PartyLFGInfo lfgInfos = new PartyLFGInfo();

            lfgInfos.slot = global.getLFGMgr().getLFGDungeonEntry(global.getLFGMgr().getDungeon(guid));
            lfgInfos.bootCount = 0;
            lfgInfos.aborted = false;

            lfgInfos.myFlags = (byte) (global.getLFGMgr().getState(guid) == LfgState.FinishedDungeon ? 2 : 0);
            lfgInfos.myRandomSlot = global.getLFGMgr().getSelectedRandomDungeon(player.getGUID());

            lfgInfos.myPartialClear = 0;
            lfgInfos.myGearDiff = 0.0f;
            lfgInfos.myFirstReward = false;

            var reward = global.getLFGMgr().getRandomDungeonReward(partyUpdate.lfgInfos.getValue().myRandomSlot, player.getLevel());

            if (reward != null) {
                var quest = global.getObjectMgr().getQuestTemplate(reward.firstQuest);

                if (quest != null) {
                    lfgInfos.myFirstReward = player.canRewardQuest(quest, false);
                }
            }

            lfgInfos.myStrangerCount = 0;
            lfgInfos.myKickVoteCount = 0;

            partyUpdate.lfgInfos = lfgInfos;
        }

        player.sendPacket(partyUpdate);
    }

    public final void updatePlayerOutOfRange(Player player) {
        if (!player || !player.isInWorld()) {
            return;
        }

        PartyMemberFullState packet = new PartyMemberFullState();
        packet.initialize(player);

        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var member = refe.getSource();

            if (member && member != player && (!member.isInMap(player) || !member.isWithinDist(player, member.getSightRange(), false))) {
                member.sendPacket(packet);
            }
        }
    }

    public final void broadcastAddonMessagePacket(ServerPacket packet, String prefix, boolean ignorePlayersInBGRaid, int group) {
        broadcastAddonMessagePacket(packet, prefix, ignorePlayersInBGRaid, group, null);
    }

    public final void broadcastAddonMessagePacket(ServerPacket packet, String prefix, boolean ignorePlayersInBGRaid) {
        broadcastAddonMessagePacket(packet, prefix, ignorePlayersInBGRaid, -1, null);
    }

    public final void broadcastAddonMessagePacket(ServerPacket packet, String prefix, boolean ignorePlayersInBGRaid, int group, ObjectGuid ignore) {
        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (player == null || (!ignore.isEmpty() && Objects.equals(player.getGUID(), ignore)) || (ignorePlayersInBGRaid && player.getGroup() != this)) {
                continue;
            }

            if ((group == -1 || refe.getSubGroup() == group)) {
                if (player.getSession().isAddonRegistered(prefix)) {
                    player.sendPacket(packet);
                }
            }
        }
    }

    public final void broadcastPacket(ServerPacket packet, boolean ignorePlayersInBGRaid, int group) {
        broadcastPacket(packet, ignorePlayersInBGRaid, group, null);
    }

    public final void broadcastPacket(ServerPacket packet, boolean ignorePlayersInBGRaid) {
        broadcastPacket(packet, ignorePlayersInBGRaid, -1, null);
    }

    public final void broadcastPacket(ServerPacket packet, boolean ignorePlayersInBGRaid, int group, ObjectGuid ignore) {
        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (!player || (!ignore.isEmpty() && Objects.equals(player.getGUID(), ignore)) || (ignorePlayersInBGRaid && player.getGroup() != this)) {
                continue;
            }

            if (player.getSession() != null && (group == -1 || refe.getSubGroup() == group)) {
                player.sendPacket(packet);
            }
        }
    }

    public final boolean sameSubGroup(Player member1, Player member2) {
        if (!member1 || !member2) {
            return false;
        }

        if (member1.getGroup() != this || member2.getGroup() != this) {
            return false;
        } else {
            return member1.getSubGroup() == member2.getSubGroup();
        }
    }

    public final void changeMembersGroup(ObjectGuid guid, byte group) {
        // Only raid groups have sub groups
        if (!isRaidGroup()) {
            return;
        }

        // Check if player is really in the raid
        var slot = getMemberSlot(guid);

        if (slot == null) {
            return;
        }

        var prevSubGroup = slot.group;

        // Abort if the player is already in the target sub group
        if (prevSubGroup == group) {
            return;
        }

        // Update the player slot with the new sub group setting
        slot.group = group;

        // Increase the counter of the new sub group..
        subGroupCounterIncrease(group);

        // ..and decrease the counter of the previous one
        subGroupCounterDecrease(prevSubGroup);

        // Preserve new sub group in database for non-raid groups
        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_MEMBER_SUBGROUP);

            stmt.AddValue(0, group);
            stmt.AddValue(1, guid.getCounter());

            DB.characters.execute(stmt);
        }

        // In case the moved player is online, update the player object with the new sub group references
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            if (player.getGroup() == this) {
                player.getGroupRef().setSubGroup(group);
            } else {
                // If player is in BG raid, it is possible that he is also in normal raid - and that normal raid is stored in m_originalGroup reference
                player.getOriginalGroupRef().setSubGroup(group);
            }
        }

        // Broadcast the changes to the group
        sendUpdate();
    }

    public final void swapMembersGroups(ObjectGuid firstGuid, ObjectGuid secondGuid) {
        if (!isRaidGroup()) {
            return;
        }

        var slots = new MemberSlot[2];
        slots[0] = getMemberSlot(firstGuid);
        slots[1] = getMemberSlot(secondGuid);

        if (slots[0] == null || slots[1] == null) {
            return;
        }

        if (slots[0].group == slots[1].group) {
            return;
        }

        var tmp = slots[0].group;
        slots[0].group = slots[1].group;
        slots[1].group = tmp;

        SQLTransaction trans = new SQLTransaction();

        for (byte i = 0; i < 2; i++) {
            // Preserve new sub group in database for non-raid groups
            if (!isBGGroup() && !isBFGroup()) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_MEMBER_SUBGROUP);
                stmt.AddValue(0, slots[i].group);
                stmt.AddValue(1, slots[i].guid.getCounter());

                trans.append(stmt);
            }

            var player = global.getObjAccessor().findConnectedPlayer(slots[i].guid);

            if (player) {
                if (player.getGroup() == this) {
                    player.getGroupRef().setSubGroup(slots[i].group);
                } else {
                    player.getOriginalGroupRef().setSubGroup(slots[i].group);
                }
            }
        }

        DB.characters.CommitTransaction(trans);

        sendUpdate();
    }

    public final void updateLooterGuid(WorldObject pLootedObject) {
        updateLooterGuid(pLootedObject, false);
    }

    public final void updateLooterGuid(WorldObject pLootedObject, boolean ifneed) {
        switch (getLootMethod()) {
            case MasterLoot:
            case FreeForAll:
                return;
            default:
                // round robin style looting applies for all low
                // quality items in each loot method except free for all and master loot
                break;
        }

        var oldLooterGUID = getLooterGuid();
        var memberSlot = getMemberSlot(oldLooterGUID);

        if (memberSlot != null) {
            if (ifneed) {
                // not update if only update if need and ok
                var looter = global.getObjAccessor().findPlayer(memberSlot.guid);

                if (looter && looter.isAtGroupRewardDistance(pLootedObject)) {
                    return;
                }
            }
        }

        // search next after current
        Player pNewLooter = null;

        for (var member : memberSlots) {
            if (member == memberSlot) {
                continue;
            }

            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player) {
                if (player.isAtGroupRewardDistance(pLootedObject)) {
                    pNewLooter = player;

                    break;
                }
            }
        }

        if (!pNewLooter) {
            // search from start
            for (var member : memberSlots) {
                var player = global.getObjAccessor().findPlayer(member.guid);

                if (player) {
                    if (player.isAtGroupRewardDistance(pLootedObject)) {
                        pNewLooter = player;

                        break;
                    }
                }
            }
        }

        if (pNewLooter) {
            if (ObjectGuid.opNotEquals(oldLooterGUID, pNewLooter.getGUID())) {
                setLooterGuid(pNewLooter.getGUID());
                sendUpdate();
            }
        } else {
            setLooterGuid(ObjectGuid.Empty);
            sendUpdate();
        }
    }

    public final GroupJoinBattlegroundResult canJoinBattlegroundQueue(Battleground bgOrTemplate, BattlegroundQueueTypeId bgQueueTypeId, int MinPlayerCount, int MaxPlayerCount, boolean isRated, int arenaSlot, tangible.OutObject<ObjectGuid> errorGuid) {
        errorGuid.outArgValue = ObjectGuid.EMPTY;

        // check if this group is LFG group
        if (isLFGGroup()) {
            return GroupJoinBattlegroundResult.LfgCantUseBattleground;
        }

        var bgEntry = CliDB.BattlemasterListStorage.get(bgOrTemplate.getTypeID());

        if (bgEntry == null) {
            return GroupJoinBattlegroundResult.BattlegroundJoinFailed; // shouldn't happen
        }

        // check for min / max count
        var memberscount = getMembersCount();

        if (memberscount > bgEntry.MaxGroupSize) // no MinPlayerCount for Battlegrounds
        {
            return GroupJoinBattlegroundResult.NONE; // ERR_GROUP_JOIN_Battleground_TOO_MANY handled on client side
        }

        // get a player as reference, to compare other players' stats to (arena team id, queue id based on level, etc.)
        var reference = getFirstMember().getSource();

        // no reference found, can't join this way
        if (!reference) {
            return GroupJoinBattlegroundResult.BattlegroundJoinFailed;
        }

        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bgOrTemplate.getMapId(), reference.getLevel());

        if (bracketEntry == null) {
            return GroupJoinBattlegroundResult.BattlegroundJoinFailed;
        }

        var arenaTeamId = reference.getArenaTeamId((byte) arenaSlot);
        var team = reference.getTeam();
        var isMercenary = reference.hasAura(BattlegroundConst.SpellMercenaryContractHorde) || reference.hasAura(BattlegroundConst.SpellMercenaryContractAlliance);

        // check every member of the group to be able to join
        memberscount = 0;

        for (var refe = getFirstMember(); refe != null; refe = refe.next(), ++memberscount) {
            var member = refe.getSource();

            // offline member? don't let join
            if (!member) {
                return GroupJoinBattlegroundResult.BattlegroundJoinFailed;
            }

            // rbac permissions
            if (!member.canJoinToBattleground(bgOrTemplate)) {
                return GroupJoinBattlegroundResult.JoinTimedOut;
            }

            // don't allow cross-faction join as group
            if (member.getTeam() != team) {
                errorGuid.outArgValue = member.getGUID();

                return GroupJoinBattlegroundResult.JoinTimedOut;
            }

            // not in the same Battleground level braket, don't let join
            var memberBracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bracketEntry.mapID, member.getLevel());

            if (memberBracketEntry != bracketEntry) {
                return GroupJoinBattlegroundResult.JoinRangeIndex;
            }

            // don't let join rated matches if the arena team id doesn't match
            if (isRated && member.getArenaTeamId((byte) arenaSlot) != arenaTeamId) {
                return GroupJoinBattlegroundResult.BattlegroundJoinFailed;
            }

            // don't let join if someone from the group is already in that bg queue
            if (member.inBattlegroundQueueForBattlegroundQueueType(bgQueueTypeId)) {
                return GroupJoinBattlegroundResult.BattlegroundJoinFailed; // not blizz-like
            }

            // don't let join if someone from the group is in bg queue random
            var isInRandomBgQueue = member.inBattlegroundQueueForBattlegroundQueueType(global.getBattlegroundMgr().BGQueueTypeId((short) BattlegroundTypeId.RB.getValue(), BattlegroundQueueIdType.Battleground, false, 0)) || member.inBattlegroundQueueForBattlegroundQueueType(global.getBattlegroundMgr().BGQueueTypeId((short) BattlegroundTypeId.RandomEpic.getValue(), BattlegroundQueueIdType.Battleground, false, 0));

            if (bgOrTemplate.getTypeID() != BattlegroundTypeId.AA && isInRandomBgQueue) {
                return GroupJoinBattlegroundResult.InRandomBg;
            }

            // don't let join to bg queue random if someone from the group is already in bg queue
            if ((bgOrTemplate.getTypeID() == BattlegroundTypeId.RB || bgOrTemplate.getTypeID() == BattlegroundTypeId.RandomEpic) && member.inBattlegroundQueue(true) && !isInRandomBgQueue) {
                return GroupJoinBattlegroundResult.InNonRandomBg;
            }

            // check for deserter debuff in case not arena queue
            if (bgOrTemplate.getTypeID() != BattlegroundTypeId.AA && member.isDeserter()) {
                return GroupJoinBattlegroundResult.Deserters;
            }

            // check if member can join any more Battleground queues
            if (!member.getHasFreeBattlegroundQueueId()) {
                return GroupJoinBattlegroundResult.TooManyQueues; // not blizz-like
            }

            // check if someone in party is using dungeon system
            if (member.isUsingLfg()) {
                return GroupJoinBattlegroundResult.LfgCantUseBattleground;
            }

            // check Freeze debuff
            if (member.hasAura(9454)) {
                return GroupJoinBattlegroundResult.BattlegroundJoinFailed;
            }

            if (isMercenary != (member.hasAura(BattlegroundConst.SpellMercenaryContractHorde) || member.hasAura(BattlegroundConst.SpellMercenaryContractAlliance))) {
                return GroupJoinBattlegroundResult.BattlegroundJoinMercenary;
            }
        }

        // only check for MinPlayerCount since MinPlayerCount == MaxPlayerCount for arenas...
        if (bgOrTemplate.isArena() && memberscount != MinPlayerCount) {
            return GroupJoinBattlegroundResult.ArenaTeamPartySize;
        }

        return GroupJoinBattlegroundResult.NONE;
    }

    public final Difficulty getDifficultyID(MapEntry mapEntry) {
        if (!mapEntry.isRaid()) {
            return dungeonDifficulty;
        }

        var defaultDifficulty = global.getDB2Mgr().GetDefaultMapDifficulty(mapEntry.id);

        if (defaultDifficulty == null) {
            return legacyRaidDifficulty;
        }

        var difficulty = CliDB.DifficultyStorage.get(defaultDifficulty.difficultyID);

        if (difficulty == null || difficulty.flags().hasFlag(DifficultyFlags.legacy)) {
            return legacyRaidDifficulty;
        }

        return raidDifficulty;
    }

    public final void resetInstances(InstanceResetMethod method, Player notifyPlayer) {
        for (var refe = instanceRefManager.getFirst(); refe != null; refe = refe.next()) {
            var map = refe.source;

            switch (map.reset(method)) {
                case InstanceResetResult.Success:
                    notifyPlayer.sendResetInstanceSuccess(map.id);
                    recentInstances.remove(map.id);

                    break;
                case InstanceResetResult.NotEmpty:
                    if (method == InstanceResetMethod.Manual) {
                        notifyPlayer.sendResetInstanceFailed(resetFailedReason.Failed, map.id);
                    } else if (method == InstanceResetMethod.OnChangeDifficulty) {
                        recentInstances.remove(map.id); // map might not have been reset on difficulty change but we still don't want to zone in there again
                    }

                    break;
                case InstanceResetResult.CannotReset:
                    recentInstances.remove(map.id); // forget the instance, allows retrying different lockout with a new leader

                    break;
                default:
                    break;
            }
        }
    }

    public final void linkOwnedInstance(GroupInstanceReference refe) {
        instanceRefManager.InsertLast(refe);
    }

    public final void broadcastGroupUpdate() {
        // FG: HACK: force flags update on group leave - for values update hack
        // -- not very efficient but safe
        for (var member : memberSlots) {
            var pp = global.getObjAccessor().findPlayer(member.guid);

            if (pp && pp.isInWorld()) {
                pp.getValues().modifyValue(pp.getUnitData()).modifyValue(pp.getUnitData().pvpFlags);
                pp.getValues().modifyValue(pp.getUnitData()).modifyValue(pp.getUnitData().factionTemplate);
                pp.forceUpdateFieldChange();
                Log.outDebug(LogFilter.Server, "-- Forced group second update for '{0}'", pp.getName());
            }
        }
    }

    public final void setLfgRoles(ObjectGuid guid, LfgRoles roles) {
        var slot = getMemberSlot(guid);

        if (slot == null) {
            return;
        }

        slot.roles = roles;
        sendUpdate();
    }

    public final LfgRoles getLfgRoles(ObjectGuid guid) {
        var slot = getMemberSlot(guid);

        if (slot == null) {
            return 0;
        }

        return slot.roles;
    }

    public final void startReadyCheck(ObjectGuid starterGuid, byte partyIndex, Duration duration) {
        if (readyCheckStarted) {
            return;
        }

        var slot = getMemberSlot(starterGuid);

        if (slot == null) {
            return;
        }

        readyCheckStarted = true;
        readyCheckTimer = duration;

        setOfflineMembersReadyChecked();

        setMemberReadyChecked(slot);

        ReadyCheckStarted readyCheckStarted = new ReadyCheckStarted();
        readyCheckStarted.partyGUID = guid;
        readyCheckStarted.partyIndex = partyIndex;
        readyCheckStarted.initiatorGUID = starterGuid;
        readyCheckStarted.duration = (int) duration.TotalMilliseconds;
        broadcastPacket(readyCheckStarted, false);
    }

    public final void setMemberReadyCheck(ObjectGuid guid, boolean ready) {
        if (!readyCheckStarted) {
            return;
        }

        var slot = getMemberSlot(guid);

        if (slot != null) {
            setMemberReadyCheck(slot, ready);
        }
    }

    public final void addRaidMarker(byte markerId, int mapId, float positionX, float positionY, float positionZ) {
        addRaidMarker(markerId, mapId, positionX, positionY, positionZ, null);
    }

    public final void addRaidMarker(byte markerId, int mapId, float positionX, float positionY, float positionZ, ObjectGuid transportGuid) {
        if (markerId >= MapDefine.RaidMarkersCount || _markers[markerId] != null) {
            return;
        }

        activeMarkers |= (1 << markerId);
        _markers[markerId] = new RaidMarker(mapId, positionX, positionY, positionZ, transportGuid);
        sendRaidMarkersChanged();
    }

    public final void deleteRaidMarker(byte markerId) {
        if (markerId > MapDefine.RaidMarkersCount) {
            return;
        }

        for (byte i = 0; i < MapDefine.RaidMarkersCount; i++) {
            if (_markers[i] != null && (markerId == i || markerId == MapDefine.RaidMarkersCount)) {
                _markers[i] = null;
                activeMarkers &= ~(1 << i);
            }
        }

        sendRaidMarkersChanged();
    }

    public final void sendRaidMarkersChanged(WorldSession session) {
        sendRaidMarkersChanged(session, 0);
    }

    public final void sendRaidMarkersChanged() {
        sendRaidMarkersChanged(null, 0);
    }

    public final void sendRaidMarkersChanged(WorldSession session, byte partyIndex) {
        RaidMarkersChanged packet = new RaidMarkersChanged();

        packet.partyIndex = partyIndex;
        packet.activeMarkers = activeMarkers;

        for (byte i = 0; i < MapDefine.RaidMarkersCount; i++) {
            if (_markers[i] != null) {
                packet.raidMarkers.add(_markers[i]);
            }
        }

        if (session) {
            session.sendPacket(packet);
        } else {
            broadcastPacket(packet, false);
        }
    }

    public final boolean isMember(ObjectGuid guid) {
        return getMemberSlot(guid) != null;
    }

    public final boolean isLeader(ObjectGuid guid) {
        return Objects.equals(getLeaderGUID(), guid);
    }

    public final boolean isAssistant(ObjectGuid guid) {
        return getMemberFlags(guid).hasFlag(GroupMemberFlags.Assistant);
    }

    public final ObjectGuid getMemberGUID(String name) {
        for (var member : memberSlots) {
            if (Objects.equals(member.name, name)) {
                return member.guid;
            }
        }

        return ObjectGuid.Empty;
    }

    public final GroupMemberFlags getMemberFlags(ObjectGuid guid) {
        var mslot = getMemberSlot(guid);

        if (mslot == null) {
            return 0;
        }

        return mslot.flags;
    }

    public final boolean sameSubGroup(ObjectGuid guid1, ObjectGuid guid2) {
        var mslot2 = getMemberSlot(guid2);

        if (mslot2 == null) {
            return false;
        }

        return sameSubGroup(guid1, mslot2);
    }

    public final boolean sameSubGroup(ObjectGuid guid1, MemberSlot slot2) {
        var mslot1 = getMemberSlot(guid1);

        if (mslot1 == null || slot2 == null) {
            return false;
        }

        return (mslot1.group == slot2.group);
    }

    public final boolean hasFreeSlotSubGroup(byte subgroup) {
        return (subGroupsCounts != null && _subGroupsCounts[subgroup] < MapDefine.MaxGroupSize);
    }

    public final byte getMemberGroup(ObjectGuid guid) {
        var mslot = getMemberSlot(guid);

        if (mslot == null) {
            return (byte) (MapDefine.MaxRaidSubGroups + 1);
        }

        return mslot.group;
    }

    public final void setBattlegroundGroup(Battleground bg) {
        bgGroup = bg;
    }

    public final void setBattlefieldGroup(BattleField bg) {
        bfGroup = bg;
    }

    public final void setGroupMemberFlag(ObjectGuid guid, boolean apply, GroupMemberFlags flag) {
        // Assistants, main assistants and main tanks are only available in raid groups
        if (!isRaidGroup()) {
            return;
        }

        // Check if player is really in the raid
        var slot = getMemberSlot(guid);

        if (slot == null) {
            return;
        }

        // Do flag specific actions, e.g ensure uniqueness
        switch (flag) {
            case MainAssist:
                removeUniqueGroupMemberFlag(GroupMemberFlags.MainAssist); // Remove main assist flag from current if any.

                break;
            case MainTank:
                removeUniqueGroupMemberFlag(GroupMemberFlags.MainTank); // Remove main tank flag from current if any.

                break;
            case Assistant:
                break;
            default:
                return; // This should never happen
        }

        // Switch the actual flag
        toggleGroupMemberFlag(slot, flag, apply);

        // Preserve the new setting in the db
        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_MEMBER_FLAG);

        stmt.AddValue(0, (byte) slot.flags.getValue());
        stmt.AddValue(1, guid.getCounter());

        DB.characters.execute(stmt);

        // Broadcast the changes to the group
        sendUpdate();
    }

    public final void linkMember(GroupReference pRef) {
        memberMgr.InsertFirst(pRef);
    }

    public final void removeUniqueGroupMemberFlag(GroupMemberFlags flag) {
        for (var member : memberSlots) {
            if (member.flags.hasFlag(flag)) {
                member.flags = GroupMemberFlags.forValue(member.flags.getValue() & ~flag.getValue());
            }
        }
    }

    public final void startLeaderOfflineTimer() {
        isLeaderOffline = true;
        leaderOfflineTimer.reset(2 * time.Minute * time.InMilliseconds);
    }

    public final void stopLeaderOfflineTimer() {
        isLeaderOffline = false;
    }

    public final void setEveryoneIsAssistant(boolean apply) {
        if (apply) {
            groupFlags = GroupFlags.forValue(groupFlags.getValue() | GroupFlags.EveryoneAssistant.getValue());
        } else {
            groupFlags = GroupFlags.forValue(groupFlags.getValue() & ~GroupFlags.EveryoneAssistant.getValue());
        }

        for (var member : memberSlots) {
            toggleGroupMemberFlag(member, GroupMemberFlags.Assistant, apply);
        }

        sendUpdate();
    }

    public final void broadcastWorker(tangible.Action1Param<Player> worker) {
        for (var refe = getFirstMember(); refe != null; refe = refe.next()) {
            worker.invoke(refe.getSource());
        }
    }

    public final ObjectGuid getRecentInstanceOwner(int mapId) {
        TValue value;
        if (recentInstances.containsKey(mapId) && (value = recentInstances.get(mapId)) == value) {
            return value.Item1;
        }

        return leaderGuid;
    }

    public final int getRecentInstanceId(int mapId) {
        Pair<ObjectGuid, Integer> value = recentInstances.get(mapId);
        if (value != null) {
            return value.second();
        }
        return 0;
    }

    public final void setRecentInstance(int mapId, ObjectGuid instanceOwner, int instanceId) {
        recentInstances.put(mapId, Pair.of(instanceOwner, instanceId));
    }

    private void selectNewPartyOrRaidLeader() {
        Player newLeader = null;

        // Attempt to give leadership to main assistant first
        if (isRaidGroup()) {
            for (var memberSlot : memberSlots) {
                if (memberSlot.flags.hasFlag(GroupMemberFlags.Assistant)) {
                    var player = global.getObjAccessor().findPlayer(memberSlot.guid);

                    if (player != null) {
                        newLeader = player;

                        break;
                    }
                }
            }
        }

        // If there aren't assistants in raid, or if the group is not a raid, pick the first available member
        if (!newLeader) {
            for (var memberSlot : memberSlots) {
                var player = global.getObjAccessor().findPlayer(memberSlot.guid);

                if (player != null) {
                    newLeader = player;

                    break;
                }
            }
        }

        if (newLeader) {
            changeLeader(newLeader.getGUID());
            sendUpdate();
        }
    }

    private void sendUpdateDestroyGroupToPlayer(Player player) {
        PartyUpdate partyUpdate = new PartyUpdate();
        partyUpdate.partyFlags = GroupFlags.destroyed;
        partyUpdate.partyIndex = (byte) groupCategory.getValue();
        partyUpdate.partyType = GroupType.NONE;
        partyUpdate.partyGUID = guid;
        partyUpdate.myIndex = -1;
        partyUpdate.sequenceNum = player.nextGroupUpdateSequenceNumber(groupCategory);
        player.sendPacket(partyUpdate);
    }

    private boolean setMembersGroup(ObjectGuid guid, byte group) {
        var slot = getMemberSlot(guid);

        if (slot == null) {
            return false;
        }

        slot.group = group;

        subGroupCounterIncrease(group);

        if (!isBGGroup() && !isBFGroup()) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GROUP_MEMBER_SUBGROUP);

            stmt.AddValue(0, group);
            stmt.AddValue(1, guid.getCounter());

            DB.characters.execute(stmt);
        }

        return true;
    }


//	public static implicit operator bool(PlayerGroup group)
//		{
//			return group != null;
//		}

    private void homebindIfInstance(Player player) {
        if (player && !player.isGameMaster() && CliDB.MapStorage.get(player.getLocation().getMapId()).IsDungeon()) {
            player.setInstanceValid(false);
        }
    }

    private void updateReadyCheck(int diff) {
        if (!readyCheckStarted) {
            return;
        }

        _readyCheckTimer -= duration.ofSeconds(diff);

        if (readyCheckTimer <= duration.Zero) {
            endReadyCheck();
        }
    }

    private void endReadyCheck() {
        if (!readyCheckStarted) {
            return;
        }

        readyCheckStarted = false;
        readyCheckTimer = duration.Zero;

        resetMemberReadyChecked();

        ReadyCheckCompleted readyCheckCompleted = new ReadyCheckCompleted();
        readyCheckCompleted.partyIndex = 0;
        readyCheckCompleted.partyGUID = guid;
        broadcastPacket(readyCheckCompleted, false);
    }

    private void setMemberReadyCheck(MemberSlot slot, boolean ready) {
        ReadyCheckResponse response = new ReadyCheckResponse();
        response.partyGUID = guid;
        response.player = slot.guid;
        response.isReady = ready;
        broadcastPacket(response, false);

        setMemberReadyChecked(slot);
    }

    private void setOfflineMembersReadyChecked() {
        for (var member : memberSlots) {
            var player = global.getObjAccessor().findConnectedPlayer(member.guid);

            if (!player || !player.getSession()) {
                setMemberReadyCheck(member, false);
            }
        }
    }

    private void setMemberReadyChecked(MemberSlot slot) {
        slot.readyChecked = true;

        if (isReadyCheckCompleted()) {
            endReadyCheck();
        }
    }

    private void resetMemberReadyChecked() {
        for (var member : memberSlots) {
            member.readyChecked = false;
        }
    }

    private void delinkMember(ObjectGuid guid) {
        var refe = memberMgr.getFirst();

        while (refe != null) {
            var nextRef = refe.next();

            if (Objects.equals(refe.source.GUID, guid)) {
                refe.Unlink();

                break;
            }

            refe = nextRef;
        }
    }

    private void initRaidSubGroupsCounter() {
        // Sub group counters initialization
        if (subGroupsCounts == null) {
            subGroupsCounts = new byte[MapDefine.MaxRaidSubGroups];
        }

        for (var memberSlot : memberSlots) {
            ++_subGroupsCounts[memberSlot.Group];
        }
    }

    private MemberSlot getMemberSlot(ObjectGuid guid) {
        for (var member : memberSlots) {
            if (Objects.equals(member.guid, guid)) {
                return member;
            }
        }

        return null;
    }

    private void subGroupCounterIncrease(byte subgroup) {
        if (subGroupsCounts != null) {
            ++_subGroupsCounts[subgroup];
        }
    }

    private void subGroupCounterDecrease(byte subgroup) {
        if (subGroupsCounts != null) {
            --_subGroupsCounts[subgroup];
        }
    }

    private void toggleGroupMemberFlag(MemberSlot slot, GroupMemberFlags flag, boolean apply) {
        if (apply) {
            slot.flags = GroupMemberFlags.forValue(slot.flags.getValue() | flag.getValue());
        } else {
            slot.flags = GroupMemberFlags.forValue(slot.flags.getValue() & ~flag.getValue());
        }
    }






}
