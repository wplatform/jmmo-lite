package com.github.azeroth.game.battlefield;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.map.ZoneScript;


import java.util.ArrayList;
import java.util.HashMap;

public class BattleField extends ZoneScript {
    // Map of the objectives belonging to this OutdoorPvP
    private final HashMap<Integer, BfCapturePoint> m_capturePoints = new HashMap<Integer, BfCapturePoint>();
    private final ArrayList<ObjectGuid>[] m_Groups = new ArrayList<ObjectGuid>[2]; // Contain different raid group
    private final HashMap<Integer, Long> m_Data64 = new HashMap<Integer, Long>();
    public ObjectGuid stalkerGuid = ObjectGuid.EMPTY;
    protected int m_Timer; // Global timer for event
    protected boolean m_IsEnabled;
    protected boolean m_isActive;
    protected int m_DefenderTeam;
    // Players info maps
    protected ArrayList<ObjectGuid>[] m_players = new ArrayList<ObjectGuid>[2]; // Players in zone
    protected ArrayList<ObjectGuid>[] m_PlayersInQueue = new ArrayList<ObjectGuid>[2]; // Players in the queue
    protected ArrayList<ObjectGuid>[] m_PlayersInWar = new ArrayList<ObjectGuid>[2]; // Players in WG combat
    protected HashMap<ObjectGuid, Long>[] m_InvitedPlayers = new HashMap<ObjectGuid, Long>[2];
    protected HashMap<ObjectGuid, Long>[] m_PlayersWillBeKick = new HashMap<ObjectGuid, Long>[2];
    // Variables that must exist for each battlefield
    protected int m_TypeId; // See enum BattlefieldTypes
    protected int m_BattleId; // BattleID (for packet)
    protected int m_ZoneId; // ZoneID of wintergrasp = 4197
    protected int m_MapId; // MapId where is Battlefield
    protected Map m_Map;
    protected int m_MaxPlayer; // Maximum number of player that participated to Battlefield
    protected int m_MinPlayer; // Minimum number of player for Battlefield start
    protected int m_MinLevel; // Required level to participate at Battlefield
    protected int m_BattleTime; // Length of a battle
    protected int m_NoWarBattleTime; // Time between two battles
    protected int m_RestartAfterCrash; // Delay to restart Wintergrasp if the server crashed during a running battle.
    protected int m_TimeForAcceptInvite;
    protected int m_uiKickDontAcceptTimer;
    protected WorldLocation kickPosition; // Position where players are teleported if they switch to afk during the battle or if they don't accept invitation
    // Graveyard variables
    protected ArrayList<BfGraveyard> m_GraveyardList = new ArrayList<>(); // Vector witch contain the different GY of the battle
    protected int m_StartGroupingTimer; // Timer for invite players in area 15 minute before start battle
    protected boolean m_StartGrouping; // bool for know if all players in area has been invited
    protected HashMap<Integer, Integer> m_Data32 = new HashMap<Integer, Integer>();
    private int m_uiKickAfkPlayersTimer; // Timer for check Afk in war
    private int m_LastResurectTimer; // Timer for resurect player every 30 sec

    public BattleField(Map map) {
        m_IsEnabled = true;
        m_DefenderTeam = TeamIds.Neutral;

        m_TimeForAcceptInvite = 20;
        m_uiKickDontAcceptTimer = 1000;
        m_uiKickAfkPlayersTimer = 1000;

        m_LastResurectTimer = 30 * time.InMilliseconds;

        m_Map = map;
        m_MapId = map.getId();

        for (byte i = 0; i < 2; ++i) {
            m_players[i] = new ArrayList<>();
            m_PlayersInQueue[i] = new ArrayList<>();
            m_PlayersInWar[i] = new ArrayList<>();
            m_InvitedPlayers[i] = new HashMap<ObjectGuid, Long>();
            m_PlayersWillBeKick[i] = new HashMap<ObjectGuid, Long>();
            m_Groups[i] = new ArrayList<>();
        }
    }

    public final void handlePlayerEnterZone(Player player, int zone) {
        // If battle is started,
        // If not full of players > invite player to join the war
        // If full of players > announce to player that BF is full and kick him after a few second if he desn't leave
        if (isWarTime()) {
            if (m_PlayersInWar[player.getTeamId()].size() + m_InvitedPlayers[player.getTeamId()].size() < m_MaxPlayer) // Vacant spaces
            {
                invitePlayerToWar(player);
            } else // No more vacant places
            {
                // todo Send a packet to announce it to player
                m_PlayersWillBeKick[player.getTeamId()].put(player.getGUID(), gameTime.GetGameTime() + 10);
                invitePlayerToQueue(player);
            }
        } else {
            // If time left is < 15 minutes invite player to join queue
            if (m_Timer <= m_StartGroupingTimer) {
                invitePlayerToQueue(player);
            }
        }

        // Add player in the list of player in zone
        m_players[player.getTeamId()].add(player.getGUID());
        onPlayerEnterZone(player);
    }

    // Called when a player leave the zone
    public final void handlePlayerLeaveZone(Player player, int zone) {
        if (isWarTime()) {
            // If the player is participating to the battle
            if (m_PlayersInWar[player.getTeamId()].contains(player.getGUID())) {
                m_PlayersInWar[player.getTeamId()].remove(player.getGUID());
                var group = player.getGroup();

                if (group) // Remove the player from the raid group
                {
                    group.removeMember(player.getGUID());
                }

                onPlayerLeaveWar(player);
            }
        }

        for (var capturePoint : m_capturePoints.values()) {
            capturePoint.handlePlayerLeave(player);
        }

        m_InvitedPlayers[player.getTeamId()].remove(player.getGUID());
        m_PlayersWillBeKick[player.getTeamId()].remove(player.getGUID());
        m_players[player.getTeamId()].remove(player.getGUID());
        sendRemoveWorldStates(player);
        removePlayerFromResurrectQueue(player.getGUID());
        onPlayerLeaveZone(player);
    }

    public boolean update(int diff) {
        if (m_Timer <= diff) {
            // Battlefield ends on time
            if (isWarTime()) {
                endBattle(true);
            } else // Time to start a new battle!
            {
                startBattle();
            }
        } else {
            m_Timer -= diff;
        }

        // Invite players a few minutes before the battle's beginning
        if (!isWarTime() && !m_StartGrouping && m_Timer <= m_StartGroupingTimer) {
            m_StartGrouping = true;
            invitePlayersInZoneToQueue();
            onStartGrouping();
        }

        var objective_changed = false;

        if (isWarTime()) {
            if (m_uiKickAfkPlayersTimer <= diff) {
                m_uiKickAfkPlayersTimer = 1000;
                kickAfkPlayers();
            } else {
                m_uiKickAfkPlayersTimer -= diff;
            }

            // Kick players who chose not to accept invitation to the battle
            if (m_uiKickDontAcceptTimer <= diff) {
                var now = gameTime.GetGameTime();

                for (var team = 0; team < SharedConst.PvpTeamsCount; team++) {
                    for (var pair : m_InvitedPlayers[team].entrySet()) {
                        if (pair.getValue() <= now) {
                            kickPlayerFromBattlefield(pair.getKey());
                        }
                    }
                }

                invitePlayersInZoneToWar();

                for (var team = 0; team < SharedConst.PvpTeamsCount; team++) {
                    for (var pair : m_PlayersWillBeKick[team].entrySet()) {
                        if (pair.getValue() <= now) {
                            kickPlayerFromBattlefield(pair.getKey());
                        }
                    }
                }

                m_uiKickDontAcceptTimer = 1000;
            } else {
                m_uiKickDontAcceptTimer -= diff;
            }

            for (var pair : m_capturePoints.entrySet()) {
                if (pair.getValue().update(diff)) {
                    objective_changed = true;
                }
            }
        }


        if (m_LastResurectTimer <= diff) {
            for (byte i = 0; i < m_GraveyardList.size(); i++) {
                if (getGraveyardById(i) != null) {
                    m_GraveyardList.get(i).resurrect();
                }
            }

            m_LastResurectTimer = BattlegroundConst.ResurrectionInterval;
        } else {
            m_LastResurectTimer -= diff;
        }

        return objective_changed;
    }

    public final void initStalker(int entry, Position pos) {
        var creature = spawnCreature(entry, pos);

        if (creature) {
            stalkerGuid = creature.getGUID();
        } else {
            Log.outError(LogFilter.Battlefield, "Battlefield.InitStalker: could not spawn Stalker (Creature entry {0}), zone messeges will be un-available", entry);
        }
    }

    public final void kickPlayerFromBattlefield(ObjectGuid guid) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            if (player.getZone() == getZoneId()) {
                player.teleportTo(kickPosition);
            }
        }
    }

    public final void startBattle() {
        if (m_isActive) {
            return;
        }

        for (var team = 0; team < 2; team++) {
            m_PlayersInWar[team].clear();
            m_Groups[team].clear();
        }

        m_Timer = m_BattleTime;
        m_isActive = true;

        invitePlayersInZoneToWar();
        invitePlayersInQueueToWar();

        onBattleStart();
    }

    public final void endBattle(boolean endByTimer) {
        if (!m_isActive) {
            return;
        }

        m_isActive = false;

        m_StartGrouping = false;

        if (!endByTimer) {
            setDefenderTeam(getAttackerTeam());
        }

        // Reset battlefield timer
        m_Timer = m_NoWarBattleTime;

        onBattleEnd(endByTimer);
    }

    public final boolean hasPlayer(Player player) {
        return m_players[player.getTeamId()].contains(player.getGUID());
    }

    // Called in WorldSession:HandleBfQueueInviteResponse
    public final void playerAcceptInviteToQueue(Player player) {
        // Add player in queue
        m_PlayersInQueue[player.getTeamId()].add(player.getGUID());
    }

    // Called in WorldSession:HandleBfExitRequest
    public final void askToLeaveQueue(Player player) {
        // Remove player from queue
        m_PlayersInQueue[player.getTeamId()].remove(player.getGUID());
    }

    // Called in WorldSession::HandleHearthAndResurrect
    public final void playerAskToLeave(Player player) {
        // Player leaving wintergrasp, teleport to Dalaran.
        // ToDo: confirm teleport destination.
        player.teleportTo(571, 5804.1499f, 624.7710f, 647.7670f, 1.6400f);
    }

    // Called in WorldSession:HandleBfEntryInviteResponse
    public final void playerAcceptInviteToWar(Player player) {
        if (!isWarTime()) {
            return;
        }

        if (addOrSetPlayerToCorrectBfGroup(player)) {
            m_PlayersInWar[player.getTeamId()].add(player.getGUID());
            m_InvitedPlayers[player.getTeamId()].remove(player.getGUID());

            if (player.isAFK()) {
                player.toggleAFK();
            }

            onPlayerJoinWar(player); //for scripting
        }
    }

    public final void teamCastSpell(int teamIndex, int spellId) {
        for (var guid : m_PlayersInWar[teamIndex]) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                if (spellId > 0) {
                    player.castSpell(player, (int) spellId, true);
                } else {
                    player.removeAuraFromStack((int) -spellId);
                }
            }
        }
    }

    public final void broadcastPacketToZone(ServerPacket data) {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_players[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendPacket(data);
                }
            }
        }
    }

    public final void broadcastPacketToQueue(ServerPacket data) {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_PlayersInQueue[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendPacket(data);
                }
            }
        }
    }

    public final void broadcastPacketToWar(ServerPacket data) {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_PlayersInWar[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendPacket(data);
                }
            }
        }
    }


    public final void sendWarning(int id) {
        sendWarning(id, null);
    }

    public final void sendWarning(int id, WorldObject target) {
        var stalker = getCreature(stalkerGuid);

        if (stalker) {
            global.getCreatureTextMgr().sendChat(stalker, (byte) id, target);
        }
    }

    public final void addCapturePoint(BfCapturePoint cp) {
        if (m_capturePoints.containsKey(cp.getCapturePointEntry())) {
            Log.outError(LogFilter.Battlefield, "Battlefield.AddCapturePoint: CapturePoint {0} already exists!", cp.getCapturePointEntry());
        }

        m_capturePoints.put(cp.getCapturePointEntry(), cp);
    }

    public final void registerZone(int zoneId) {
        global.getBattleFieldMgr().addZone(zoneId, this);
    }

    public final void hideNpc(Creature creature) {
        creature.combatStop();
        creature.setReactState(ReactStates.Passive);
        creature.setUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue());
        creature.disappearAndDie();
        creature.setVisible(false);
    }

    public final void showNpc(Creature creature, boolean aggressive) {
        creature.setVisible(true);
        creature.removeUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue());

        if (!creature.isAlive()) {
            creature.respawn(true);
        }

        if (aggressive) {
            creature.setReactState(ReactStates.Aggressive);
        } else {
            creature.setUnitFlag(UnitFlag.NonAttackable);
            creature.setReactState(ReactStates.Passive);
        }
    }

    //***************End of Group System*******************

    public final BfGraveyard getGraveyardById(int id) {
        if (id < m_GraveyardList.size()) {
            var graveyard = m_GraveyardList.get(id);

            if (graveyard != null) {
                return graveyard;
            } else {
                Log.outError(LogFilter.Battlefield, "Battlefield:GetGraveyardById Id: {0} not existed", id);
            }
        } else {
            Log.outError(LogFilter.Battlefield, "Battlefield:GetGraveyardById Id: {0} cant be found", id);
        }

        return null;
    }

    public final WorldSafeLocsEntry getClosestGraveYard(Player player) {
        BfGraveyard closestGY = null;
        float maxdist = -1;

        for (byte i = 0; i < m_GraveyardList.size(); i++) {
            if (m_GraveyardList.get(i) != null) {
                if (m_GraveyardList.get(i).getControlTeamId() != player.getTeamId()) {
                    continue;
                }

                var dist = m_GraveyardList.get(i).getDistance(player);

                if (dist < maxdist || maxdist < 0) {
                    closestGY = m_GraveyardList.get(i);
                    maxdist = dist;
                }
            }
        }

        if (closestGY != null) {
            return global.getObjectMgr().getWorldSafeLoc(closestGY.getGraveyardId());
        }

        return null;
    }

    public void addPlayerToResurrectQueue(ObjectGuid npcGuid, ObjectGuid playerGuid) {
        for (byte i = 0; i < m_GraveyardList.size(); i++) {
            if (m_GraveyardList.get(i) == null) {
                continue;
            }

            if (m_GraveyardList.get(i).hasNpc(npcGuid)) {
                m_GraveyardList.get(i).addPlayer(playerGuid);

                break;
            }
        }
    }

    public final void removePlayerFromResurrectQueue(ObjectGuid playerGuid) {
        for (byte i = 0; i < m_GraveyardList.size(); i++) {
            if (m_GraveyardList.get(i) == null) {
                continue;
            }

            if (m_GraveyardList.get(i).hasPlayer(playerGuid)) {
                m_GraveyardList.get(i).removePlayer(playerGuid);

                break;
            }
        }
    }

    public final void sendAreaSpiritHealerQuery(Player player, ObjectGuid guid) {
        AreaSpiritHealerTime areaSpiritHealerTime = new AreaSpiritHealerTime();
        areaSpiritHealerTime.healerGuid = guid;
        areaSpiritHealerTime.timeLeft = m_LastResurectTimer; // resurrect every 30 seconds

        player.sendPacket(areaSpiritHealerTime);
    }

    public final Creature spawnCreature(int entry, Position pos) {
        if (global.getObjectMgr().getCreatureTemplate(entry) == null) {
            Log.outError(LogFilter.Battlefield, "Battlefield:SpawnCreature: entry {0} does not exist.", entry);

            return null;
        }

        var creature = CREATURE.createCreature(entry, m_Map, pos);

        if (!creature) {
            Log.outError(LogFilter.Battlefield, "Battlefield:SpawnCreature: Can't create creature entry: {0}", entry);

            return null;
        }

        creature.setHomePosition(pos);

        // Set creature in world
        m_Map.addToMap(creature);
        creature.setActive(true);
        creature.setFarVisible(true);

        return creature;
    }

    // Method for spawning gameobject on map
    public final GameObject spawnGameObject(int entry, Position pos, Quaternion rotation) {
        if (global.getObjectMgr().getGameObjectTemplate(entry) == null) {
            Log.outError(LogFilter.Battlefield, "Battlefield.SpawnGameObject: GameObject template {0} not found in database! Battlefield not created!", entry);

            return null;
        }

        // Create gameobject
        var go = gameObject.createGameObject(entry, m_Map, pos, rotation, 255, GOState.Ready);

        if (!go) {
            Log.outError(LogFilter.Battlefield, "Battlefield:SpawnGameObject: Cannot create gameobject template {1}! Battlefield not created!", entry);

            return null;
        }

        // Add to world
        m_Map.addToMap(go);
        go.setActive(true);
        go.setFarVisible(true);

        return go;
    }

    public final Creature getCreature(ObjectGuid guid) {
        if (!m_Map) {
            return null;
        }

        return m_Map.getCreature(guid);
    }

    public final GameObject getGameObject(ObjectGuid guid) {
        if (!m_Map) {
            return null;
        }

        return m_Map.getGameObject(guid);
    }

    // Call this to init the Battlefield
    public boolean setupBattlefield() {
        return true;
    }

    // Called when a Unit is kill in battlefield zone
    public void handleKill(Player killer, Unit killed) {
    }

    public final int getTypeId() {
        return m_TypeId;
    }

    public final int getZoneId() {
        return m_ZoneId;
    }

    public final int getMapId() {
        return m_MapId;
    }

    public final Map getMap() {
        return m_Map;
    }

    public final long getQueueId() {
        return MathUtil.MakePair64(m_BattleId | 0x20000, 0x1F100000);
    }

    // Return true if battle is start, false if battle is not started
    public final boolean isWarTime() {
        return m_isActive;
    }

    // Enable or Disable battlefield
    public final void toggleBattlefield(boolean enable) {
        m_IsEnabled = enable;
    }

    // Return if battlefield is enable
    public final boolean isEnabled() {
        return m_IsEnabled;
    }

    // All-purpose data storage 64 bit
    public long getData64(int dataId) {
        return m_Data64.get(dataId);
    }

    public void setData64(int dataId, long value) {
        m_Data64.put(dataId, value);
    }

    // All-purpose data storage 32 bit
    public int getData(int dataId) {
        return m_Data32.get(dataId);
    }

    public void setData(int dataId, int value) {
        m_Data32.put(dataId, value);
    }

    public void updateData(int index, int pad) {
        if (pad < 0) {
            m_Data32.put(index, m_Data32.get(index) - (int) -pad);
        } else {
            m_Data32.put(index, m_Data32.get(index) + (int) pad);
        }
    }

    // Battlefield - generic methods
    public final int getDefenderTeam() {
        return m_DefenderTeam;
    }

    private void setDefenderTeam(int team) {
        m_DefenderTeam = team;
    }

    public final int getAttackerTeam() {
        return 1 - m_DefenderTeam;
    }

    public final int getOtherTeam(int teamIndex) {
        return (teamIndex == TeamId.HORDE ? TeamId.ALLIANCE : TeamId.HORDE);
    }

    // Called on start
    public void onBattleStart() {
    }

    // Called at the end of battle
    public void onBattleEnd(boolean endByTimer) {
    }

    // Called x minutes before battle start when player in zone are invite to join queue
    public void onStartGrouping() {
    }

    // Called when a player accept to join the battle
    public void onPlayerJoinWar(Player player) {
    }

    // Called when a player leave the battle
    public void onPlayerLeaveWar(Player player) {
    }

    // Called when a player leave battlefield zone
    public void onPlayerLeaveZone(Player player) {
    }

    // Called when a player enter in battlefield zone
    public void onPlayerEnterZone(Player player) {
    }

    public final int getBattleId() {
        return m_BattleId;
    }

    public void doCompleteOrIncrementAchievement(int achievement, Player player) {
        doCompleteOrIncrementAchievement(achievement, player, 1);
    }

    public void doCompleteOrIncrementAchievement(int achievement, Player player, byte incrementNumber) {
    }

    // Return if we can use mount in battlefield
    public final boolean canFlyIn() {
        return !m_isActive;
    }

    public final int getTimer() {
        return m_Timer;
    }

    public final void setTimer(int timer) {
        m_Timer = timer;
    }

    // use for switch off all worldstate for client
    public void sendRemoveWorldStates(Player player) {
    }

    private void invitePlayersInZoneToQueue() {
        for (byte team = 0; team < SharedConst.PvpTeamsCount; ++team) {
            for (var guid : m_players[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    invitePlayerToQueue(player);
                }
            }
        }
    }

    private void invitePlayerToQueue(Player player) {
        if (m_PlayersInQueue[player.getTeamId()].contains(player.getGUID())) {
            return;
        }

        if (m_PlayersInQueue[player.getTeamId()].size() <= m_MinPlayer || m_PlayersInQueue[GetOtherTeam(player.getTeamId())].size() >= m_MinPlayer) {
            playerAcceptInviteToQueue(player);
        }
    }

    private void invitePlayersInQueueToWar() {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_PlayersInQueue[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    if (m_PlayersInWar[player.getTeamId()].size() + m_InvitedPlayers[player.getTeamId()].size() < m_MaxPlayer) {
                        invitePlayerToWar(player);
                    } else {
                        //Full
                    }
                }
            }

            m_PlayersInQueue[team].clear();
        }
    }

    private void invitePlayersInZoneToWar() {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_players[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    if (m_PlayersInWar[player.getTeamId()].contains(player.getGUID()) || m_InvitedPlayers[player.getTeamId()].containsKey(player.getGUID())) {
                        continue;
                    }

                    if (m_PlayersInWar[player.getTeamId()].size() + m_InvitedPlayers[player.getTeamId()].size() < m_MaxPlayer) {
                        invitePlayerToWar(player);
                    } else // Battlefield is full of players
                    {
                        m_PlayersWillBeKick[player.getTeamId()].put(player.getGUID(), gameTime.GetGameTime() + 10);
                    }
                }
            }
        }
    }

    private void invitePlayerToWar(Player player) {
        if (!player) {
            return;
        }

        // todo needed ?
        if (player.isInFlight()) {
            return;
        }

        if (player.getInArena() || player.getBattleground()) {
            m_PlayersInQueue[player.getTeamId()].remove(player.getGUID());

            return;
        }

        // If the player does not match minimal level requirements for the battlefield, kick him
        if (player.getLevel() < m_MinLevel) {
            if (!m_PlayersWillBeKick[player.getTeamId()].containsKey(player.getGUID())) {
                m_PlayersWillBeKick[player.getTeamId()].put(player.getGUID(), gameTime.GetGameTime() + 10);
            }

            return;
        }

        // Check if player is not already in war
        if (m_PlayersInWar[player.getTeamId()].contains(player.getGUID()) || m_InvitedPlayers[player.getTeamId()].containsKey(player.getGUID())) {
            return;
        }

        m_PlayersWillBeKick[player.getTeamId()].remove(player.getGUID());
        m_InvitedPlayers[player.getTeamId()].put(player.getGUID(), gameTime.GetGameTime() + m_TimeForAcceptInvite);
        playerAcceptInviteToWar(player);
    }

    private void kickAfkPlayers() {
        for (byte team = 0; team < 2; ++team) {
            for (var guid : m_PlayersInWar[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    if (player.isAFK()) {
                        kickPlayerFromBattlefield(guid);
                    }
                }
            }
        }
    }

    private void doPlaySoundToAll(int soundID) {
        broadcastPacketToWar(new playSound(ObjectGuid.Empty, soundID, 0));
    }

    private BfCapturePoint getCapturePoint(int entry) {
        return m_capturePoints.get(entry);
    }

    // ****************************************************
    // ******************* Group System *******************
    // ****************************************************
    private PlayerGroup getFreeBfRaid(int teamIndex) {
        for (var guid : m_Groups[teamIndex]) {
            var group = global.getGroupMgr().getGroupByGUID(guid);

            if (group) {
                if (!group.isFull()) {
                    return group;
                }
            }
        }

        return null;
    }

    private PlayerGroup getGroupPlayer(ObjectGuid plguid, int teamIndex) {
        for (var guid : m_Groups[teamIndex]) {
            var group = global.getGroupMgr().getGroupByGUID(guid);

            if (group) {
                if (group.isMember(plguid)) {
                    return group;
                }
            }
        }

        return null;
    }

    private boolean addOrSetPlayerToCorrectBfGroup(Player player) {
        if (!player.isInWorld()) {
            return false;
        }

        var oldgroup = player.getGroup();

        if (oldgroup) {
            oldgroup.removeMember(player.getGUID());
        }

        var group = getFreeBfRaid(player.getTeamId());

        if (!group) {
            group = new PlayerGroup();
            group.setBattlefieldGroup(this);
            group.create(player);
            global.getGroupMgr().addGroup(group);
            m_Groups[player.getTeamId()].add(group.getGUID());
        } else if (group.isMember(player.getGUID())) {
            var subgroup = group.getMemberGroup(player.getGUID());
            player.setBattlegroundOrBattlefieldRaid(group, subgroup);
        } else {
            group.addMember(player);
        }

        return true;
    }

    private BattlefieldState getState() {
        return m_isActive ? BattlefieldState.InProgress : (m_Timer <= m_StartGroupingTimer ? BattlefieldState.Warnup : BattlefieldState.inactive);
    }

    private ArrayList<BfGraveyard> getGraveyardVector() {
        return m_GraveyardList;
    }
}
