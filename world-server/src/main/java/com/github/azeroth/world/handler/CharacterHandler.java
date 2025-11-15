package com.github.azeroth.world.handler;

import com.github.azeroth.character.repository.CharacterRepository;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.authentication.ResumeComms;
import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.world.World;
import com.github.azeroth.world.network.WorldRequest;
import com.github.azeroth.world.network.WorldResponse;
import com.github.azeroth.world.query.LoginQueryHolder;
import lombok.RequiredArgsConstructor;

import static com.github.azeroth.time.StopWatch.SPECIAL_RESUME_COMMS_TIME_SYNC_COUNTER;

@RequiredArgsConstructor
public class CharacterHandler {


    private static final String LOADING_PLAYER = "loadingPlayer";
    private final CharacterRepository characterRepo;
    private final WorldSetting worldSetting;
    private final World world;


    public void handleContinuePlayerLogin(WorldRequest request, WorldResponse response) {
        var session = request.getSession();
        var loadingPlayer = (ObjectGuid) session.getAttribute(LOADING_PLAYER);
        if (loadingPlayer == null || session.getPlayer() != null) {
            session.kickout("HandleContinuePlayerLogin incorrect player state when logging in");
            return;
        }

        LoginQueryHolder holder = new LoginQueryHolder(world, loadingPlayer, session.getAccountId());
        holder.runSync(() -> {
            handlePlayerLogin(request, response, holder);
        }, () -> {
            session.removeAttribute(LOADING_PLAYER);
        });

        response.setWorldPacket(new ResumeComms());

        // client will respond to SMSG_RESUME_COMMS with CMSG_QUEUED_MESSAGES_END
        session.getStopWatch().start(SPECIAL_RESUME_COMMS_TIME_SYNC_COUNTER);
    }


    void handlePlayerLogin(WorldRequest request, WorldResponse response,LoginQueryHolder holder)
    {
        ObjectGuid playerGuid = holder.getLoadingPlayer();

        Player pCurrChar = new Player(request.getSession());
        // for send server info and strings (config)
        ChatHandler chH = ChatHandler(pCurrChar->GetSession());

        // "GetAccountId() == db stored account id" checked in LoadFromDB (prevent login not own character using cheating tools)
        if (!pCurrChar.loadFromDB(playerGuid, holder))
        {
            SetPlayer(nullptr);
            KickPlayer("HandlePlayerLogin Player::LoadFromDB failed"); // disconnect client, player no set to session and it will not deleted or saved at kick
            delete pCurrChar;                                   // delete it manually
            m_playerLoading.Clear();
            return;
        }

        if (!_timeSyncClockDeltaQueue->empty())
        {
            pCurrChar->SetPlayerLocalFlag(PLAYER_LOCAL_FLAG_OVERRIDE_TRANSPORT_SERVER_TIME);
            pCurrChar->SetTransportServerTime(_timeSyncClockDelta);
        }

        pCurrChar->SetVirtualPlayerRealm(GetVirtualRealmAddress());

        SendAccountDataTimes(ObjectGuid::Empty, GLOBAL_CACHE_MASK);
        SendTutorialsData();

        pCurrChar->GetMotionMaster()->Initialize();
        pCurrChar->SendDungeonDifficulty();

        WorldPackets::Character::LoginVerifyWorld loginVerifyWorld;
        loginVerifyWorld.MapID = pCurrChar->GetMapId();
        loginVerifyWorld.Pos = pCurrChar->GetPosition();
        SendPacket(loginVerifyWorld.Write());

        // load player specific part before send times
        LoadAccountData(holder.GetPreparedResult(PLAYER_LOGIN_QUERY_LOAD_ACCOUNT_DATA), PER_CHARACTER_CACHE_MASK);
        SendAccountDataTimes(playerGuid, ALL_ACCOUNT_DATA_CACHE_MASK);

        SendFeatureSystemStatus();

        // Send MOTD
        {
            for (std::string const& motdLine : sWorld->GetMotd())
            sWorld->SendServerMessage(SERVER_MSG_STRING, motdLine, pCurrChar);
        }

        SendSetTimeZoneInformation();

        // Send PVPSeason
        {
            WorldPackets::Arena::PvpSeason pvpSeason;
            pvpSeason.PreviousArenaSeason = sWorld->getIntConfig(CONFIG_ARENA_SEASON_ID) - sWorld->getBoolConfig(CONFIG_ARENA_SEASON_IN_PROGRESS);
            if (sWorld->getBoolConfig(CONFIG_ARENA_SEASON_IN_PROGRESS))
                pvpSeason.CurrentArenaSeason = sWorld->getIntConfig(CONFIG_ARENA_SEASON_ID);

            SendPacket(pvpSeason.Write());
        }

        // send server info
        {
            if (sWorld->getIntConfig(CONFIG_ENABLE_SINFO_LOGIN) == 1)
                chH.PSendSysMessage(GitRevision::GetFullVersion());
        }

        //QueryResult* result = CharacterDatabase.PQuery("SELECT guildid, `rank` FROM guild_member WHERE guid = '{}'", pCurrChar->GetGUIDLow());
        if (PreparedQueryResult resultGuild = holder.GetPreparedResult(PLAYER_LOGIN_QUERY_LOAD_GUILD))
        {
            Field* fields = resultGuild->Fetch();
            pCurrChar->SetInGuild(fields[0].GetUInt64());
            pCurrChar->SetGuildRank(fields[1].GetUInt8());
            if (Guild* guild = sGuildMgr->GetGuildById(pCurrChar->GetGuildId()))
                pCurrChar->SetGuildLevel(guild->GetLevel());
        }
    else if (pCurrChar->GetGuildId())                        // clear guild related fields in case wrong data about non existed membership
    {
        pCurrChar->SetInGuild(UI64LIT(0));
        pCurrChar->SetGuildRank(0);
        pCurrChar->SetGuildLevel(0);
    }

        SendAuctionFavoriteList();

        pCurrChar->GetSession()->GetBattlePetMgr()->SendJournalLockStatus();

        pCurrChar->SendInitialPacketsBeforeAddToMap();

        //Show cinematic at the first time that player login
        if (!pCurrChar->getCinematic())
        {
            pCurrChar->setCinematic(1);

            if (PlayerInfo const* playerInfo = sObjectMgr->GetPlayerInfo(pCurrChar->GetRace(), pCurrChar->GetClass()))
            {
                switch (pCurrChar->GetCreateMode())
                {
                    case PlayerCreateMode::Normal:
                        if (playerInfo->introMovieId)
                            pCurrChar->SendMovieStart(*playerInfo->introMovieId);
                    else if (playerInfo->introSceneId)
                        pCurrChar->GetSceneMgr().PlayScene(*playerInfo->introSceneId);
                    else if (sChrClassesStore.AssertEntry(pCurrChar->GetClass())->CinematicSequenceID)
                        pCurrChar->SendCinematicStart(sChrClassesStore.AssertEntry(pCurrChar->GetClass())->CinematicSequenceID);
                    else if (sChrRacesStore.AssertEntry(pCurrChar->GetRace())->CinematicSequenceID)
                        pCurrChar->SendCinematicStart(sChrRacesStore.AssertEntry(pCurrChar->GetRace())->CinematicSequenceID);
                        break;
                    case PlayerCreateMode::NPE:
                        if (playerInfo->introSceneIdNPE)
                            pCurrChar->GetSceneMgr().PlayScene(*playerInfo->introSceneIdNPE);
                        break;
                    default:
                        break;
                }
            }

            // send new char string if not empty
            if (!sWorld->GetNewCharString().empty())
                chH.PSendSysMessage("%s", sWorld->GetNewCharString().c_str());
        }

        if (!pCurrChar->GetMap()->AddPlayerToMap(pCurrChar))
        {
            AreaTriggerStruct const* at = sObjectMgr->GetGoBackTrigger(pCurrChar->GetMapId());
            if (at)
                pCurrChar->TeleportTo(at->target_mapId, at->target_X, at->target_Y, at->target_Z, pCurrChar->GetOrientation());
            else
                pCurrChar->TeleportTo(pCurrChar->m_homebind);
        }

        ObjectAccessor::AddObject(pCurrChar);
        //TC_LOG_DEBUG("Player {} added to Map.", pCurrChar->GetName());

        if (pCurrChar->GetGuildId())
        {
            if (Guild* guild = sGuildMgr->GetGuildById(pCurrChar->GetGuildId()))
                guild->SendLoginInfo(this);
            else
            {
                // remove wrong guild data
                TC_LOG_ERROR("misc", "Player {} ({}) marked as member of not existing guild (id: {}), removing guild membership for player.", pCurrChar->GetName(), pCurrChar->GetGUID().ToString(), pCurrChar->GetGuildId());
                pCurrChar->SetInGuild(UI64LIT(0));
            }
        }

        pCurrChar->RemoveAurasWithInterruptFlags(SpellAuraInterruptFlags::Login);

        pCurrChar->SendInitialPacketsAfterAddToMap();

        CharacterDatabasePreparedStatement* stmt = CharacterDatabase.GetPreparedStatement(CHAR_UPD_CHAR_ONLINE);
        stmt->setUInt64(0, pCurrChar->GetGUID().GetCounter());
        CharacterDatabase.Execute(stmt);

        LoginDatabasePreparedStatement* loginStmt = LoginDatabase.GetPreparedStatement(LOGIN_UPD_ACCOUNT_ONLINE);
        loginStmt->setUInt32(0, GetAccountId());
        LoginDatabase.Execute(loginStmt);

        pCurrChar->SetInGameTime(GameTime::GetGameTimeMS());

        // announce group about member online (must be after add to player list to receive announce to self)
        if (Group* group = pCurrChar->GetGroup())
        {
            //pCurrChar->groupInfo.group->SendInit(this); // useless
            group->SendUpdate();
            if (group->GetLeaderGUID() == pCurrChar->GetGUID())
                group->StopLeaderOfflineTimer();
        }

        // friend status
        sSocialMgr->SendFriendStatus(pCurrChar, FRIEND_ONLINE, pCurrChar->GetGUID(), true);

        // Place character in world (and load zone) before some object loading
        pCurrChar->LoadCorpse(holder.GetPreparedResult(PLAYER_LOGIN_QUERY_LOAD_CORPSE_LOCATION));

        // setting Ghost+speed if dead
        if (pCurrChar->m_deathState == DEAD)
        {
            // not blizz like, we must correctly save and load player instead...
            if (pCurrChar->GetRace() == RACE_NIGHTELF && !pCurrChar->HasAura(20584))
                pCurrChar->CastSpell(pCurrChar, 20584, true);// auras SPELL_AURA_INCREASE_SPEED(+speed in wisp form), SPELL_AURA_INCREASE_SWIM_SPEED(+swim speed in wisp form), SPELL_AURA_TRANSFORM (to wisp form)

            if (!pCurrChar->HasAura(8326))
                pCurrChar->CastSpell(pCurrChar, 8326, true); // auras SPELL_AURA_GHOST, SPELL_AURA_INCREASE_SPEED(why?), SPELL_AURA_INCREASE_SWIM_SPEED(why?)

            pCurrChar->SetWaterWalking(true);
        }

        pCurrChar->ContinueTaxiFlight();

        // reset for all pets before pet loading
        if (pCurrChar->HasAtLoginFlag(AT_LOGIN_RESET_PET_TALENTS))
        {
            // Delete all of the player's pet spells
            CharacterDatabasePreparedStatement* stmtSpells = CharacterDatabase.GetPreparedStatement(CHAR_DEL_ALL_PET_SPELLS_BY_OWNER);
            stmtSpells->setUInt64(0, pCurrChar->GetGUID().GetCounter());
            CharacterDatabase.Execute(stmtSpells);

            // Then reset all of the player's pet specualizations
            CharacterDatabasePreparedStatement* stmtSpec = CharacterDatabase.GetPreparedStatement(CHAR_UPD_PET_SPECS_BY_OWNER);
            stmtSpec->setUInt64(0, pCurrChar->GetGUID().GetCounter());
            CharacterDatabase.Execute(stmtSpec);
        }

        // Load pet if any (if player not alive and in taxi flight or another then pet will remember as temporary unsummoned)
        pCurrChar->ResummonPetTemporaryUnSummonedIfAny();

        // Set FFA PvP for non GM in non-rest mode
        if (sWorld->IsFFAPvPRealm() && !pCurrChar->IsGameMaster() && !pCurrChar->HasPlayerFlag(PLAYER_FLAGS_RESTING))
            pCurrChar->SetPvpFlag(UNIT_BYTE2_FLAG_FFA_PVP);

        if (pCurrChar->HasPlayerFlag(PLAYER_FLAGS_CONTESTED_PVP))
            pCurrChar->SetContestedPvP();

        // Apply at_login requests
        if (pCurrChar->HasAtLoginFlag(AT_LOGIN_RESET_SPELLS))
        {
            pCurrChar->ResetSpells();
            SendNotification(LANG_RESET_SPELLS);
        }

        if (pCurrChar->HasAtLoginFlag(AT_LOGIN_RESET_TALENTS))
        {
            pCurrChar->ResetTalents(true);
            SendNotification(LANG_RESET_TALENTS);
        }

        bool firstLogin = pCurrChar->HasAtLoginFlag(AT_LOGIN_FIRST);
        if (firstLogin)
        {
            pCurrChar->RemoveAtLoginFlag(AT_LOGIN_FIRST);

            PlayerInfo const* info = sObjectMgr->GetPlayerInfo(pCurrChar->GetRace(), pCurrChar->GetClass());
            for (uint32 spellId : info->castSpells[AsUnderlyingType(pCurrChar->GetCreateMode())])
                pCurrChar->CastSpell(pCurrChar, spellId, true);

            // start with every map explored
            if (sWorld->getBoolConfig(CONFIG_START_ALL_EXPLORED))
            {
                for (uint32 i = 0; i < PLAYER_EXPLORED_ZONES_SIZE; ++i)
                    pCurrChar->AddExploredZones(i, UI64LIT(0xFFFFFFFFFFFFFFFF));
            }

            // Max relevant reputations if "StartAllReputation" is enabled
            if (sWorld->getBoolConfig(CONFIG_START_ALL_REP))
            {
                ReputationMgr& repMgr = pCurrChar->GetReputationMgr();
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 942), 42999, false); // Cenarion Expedition
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 935), 42999, false); // The Sha'tar
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 936), 42999, false); // Shattrath City
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1011), 42999, false); // Lower City
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 970), 42999, false); // Sporeggar
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 967), 42999, false); // The Violet Eye
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 989), 42999, false); // Keepers of Time
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 932), 42999, false); // The Aldor
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 934), 42999, false); // The Scryers
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1038), 42999, false); // Ogri'la
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1077), 42999, false); // Shattered Sun Offensive
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1106), 42999, false); // Argent Crusade
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1104), 42999, false); // Frenzyheart Tribe
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1090), 42999, false); // Kirin Tor
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1098), 42999, false); // Knights of the Ebon Blade
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1156), 42999, false); // The Ashen Verdict
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1073), 42999, false); // The Kalu'ak
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1105), 42999, false); // The Oracles
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1119), 42999, false); // The Sons of Hodir
                repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1091), 42999, false); // The Wyrmrest Accord

                // Factions depending on team, like cities and some more stuff
                switch (pCurrChar->GetTeam())
                {
                    case ALLIANCE:
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  72), 42999, false); // Stormwind
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  47), 42999, false); // Ironforge
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  69), 42999, false); // Darnassus
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 930), 42999, false); // Exodar
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 730), 42999, false); // Stormpike Guard
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 978), 42999, false); // Kurenai
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  54), 42999, false); // Gnomeregan Exiles
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 946), 42999, false); // Honor Hold
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1037), 42999, false); // Alliance Vanguard
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1068), 42999, false); // Explorers' League
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1126), 42999, false); // The Frostborn
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1094), 42999, false); // The Silver Covenant
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1050), 42999, false); // Valiance Expedition
                        break;
                    case HORDE:
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  76), 42999, false); // Orgrimmar
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  68), 42999, false); // Undercity
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(  81), 42999, false); // Thunder Bluff
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 911), 42999, false); // Silvermoon City
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 729), 42999, false); // Frostwolf Clan
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 941), 42999, false); // The Mag'har
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 530), 42999, false); // Darkspear Trolls
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry( 947), 42999, false); // Thrallmar
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1052), 42999, false); // Horde Expedition
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1067), 42999, false); // The Hand of Vengeance
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1124), 42999, false); // The Sunreavers
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1064), 42999, false); // The Taunka
                        repMgr.SetOneFactionReputation(sFactionStore.LookupEntry(1085), 42999, false); // Warsong Offensive
                        break;
                    default:
                        break;
                }
                repMgr.SendState(nullptr);
            }
        }

        // show time before shutdown if shutdown planned.
        if (sWorld->IsShuttingDown())
            sWorld->ShutdownMsg(true, pCurrChar);

        if (sWorld->getBoolConfig(CONFIG_ALL_TAXI_PATHS))
            pCurrChar->SetTaxiCheater(true);

        if (pCurrChar->IsGameMaster())
            SendNotification(LANG_GM_ON);

        TC_LOG_INFO("entities.player.character", "Account: {} (IP: {}) Login Character: [{}] {} Level: {}, XP: {}/{} ({} left)",
                GetAccountId(), GetRemoteAddress(), pCurrChar->GetName(), pCurrChar->GetGUID().ToString(), pCurrChar->GetLevel(),
                _player->GetXP(), _player->GetXPForNextLevel(), std::max(0, (int32)_player->GetXPForNextLevel() - (int32)_player->GetXP()));

        if (!pCurrChar->IsStandState() && !pCurrChar->HasUnitState(UNIT_STATE_STUNNED))
            pCurrChar->SetStandState(UNIT_STAND_STATE_STAND);

        pCurrChar->UpdateAverageItemLevelTotal();
        pCurrChar->UpdateAverageItemLevelEquipped();

        m_playerLoading.Clear();

        _player->UpdateMountCapability();

        // Handle Login-Achievements (should be handled after loading)
        _player->UpdateCriteria(CriteriaType::Login, 1);

        sScriptMgr->OnPlayerLogin(pCurrChar, firstLogin);

        TC_METRIC_EVENT("player_events", "Login", pCurrChar->GetName());
    }

}
