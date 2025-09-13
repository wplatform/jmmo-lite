package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;

class BgEyeofStorm extends Battleground {

    private final int[] m_HonorScoreTics = new int[2];

    private final int[] m_TeamPointsCount = new int[2];

    private final int[] m_Points_Trigger = new int[EotSPoints.PointsMax];
    private final Team[] m_PointOwnedByTeam = new Team[EotSPoints.PointsMax];
    private final EotSPointState[] m_PointState = new EotSPointState[EotSPoints.PointsMax];
    private final EotSProgressBarConsts[] m_PointBarStatus = new EotSProgressBarConsts[EotSPoints.PointsMax];
    private final BattlegroundPointCaptureStatus[] m_LastPointCaptureStatus = new BattlegroundPointCaptureStatus[EotSPoints.PointsMax];
    private final ArrayList<ObjectGuid>[] m_PlayersNearPoint = new ArrayList<ObjectGuid>[EotSPoints.POINTSMAX + 1];

    private final byte[] m_CurrentPointPlayersCount = new byte[2 * EotSPoints.PointsMax];

    private ObjectGuid m_FlagKeeper = ObjectGuid.EMPTY; // keepers guid
    private ObjectGuid m_DroppedFlagGUID = ObjectGuid.EMPTY;

    private int m_FlagCapturedBgObjectType; // type that should be despawned when flag is captured
    private EotSFlagState m_FlagState = EotSFlagState.values()[0]; // for checking flag state
    private int m_FlagsTimer;
    private int m_TowerCapCheckTimer;

    private int m_PointAddingTimer;

    private int m_HonorTics;

    public BgEyeofStorm(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        m_BuffChange = true;
        bgObjects = new ObjectGuid[EotSObjectTypes.Max];
        bgCreatures = new ObjectGuid[EotSCreaturesTypes.Max];
        m_Points_Trigger[EotSPoints.FelReaver] = EotSPointsTrigger.felReaverBuff;
        m_Points_Trigger[EotSPoints.BloodElf] = EotSPointsTrigger.bloodElfBuff;
        m_Points_Trigger[EotSPoints.DraeneiRuins] = EotSPointsTrigger.draeneiRuinsBuff;
        m_Points_Trigger[EotSPoints.MageTower] = EotSPointsTrigger.mageTowerBuff;
        m_HonorScoreTics[TeamId.ALLIANCE] = 0;
        m_HonorScoreTics[TeamId.HORDE] = 0;
        m_TeamPointsCount[TeamId.ALLIANCE] = 0;
        m_TeamPointsCount[TeamId.HORDE] = 0;
        m_FlagKeeper.clear();
        m_DroppedFlagGUID.clear();
        m_FlagCapturedBgObjectType = 0;
        m_FlagState = EotSFlagState.OnBase;
        m_FlagsTimer = 0;
        m_TowerCapCheckTimer = 0;
        m_PointAddingTimer = 0;
        m_HonorTics = 0;

        for (byte i = 0; i < EotSPoints.POINTSMAX; ++i) {
            m_PointOwnedByTeam[i] = Team.other;
            m_PointState[i] = EotSPointState.Uncontrolled;
            m_PointBarStatus[i] = EotSProgressBarConsts.ProgressBarStateMiddle;
            m_LastPointCaptureStatus[i] = BattlegroundPointCaptureStatus.Neutral;
        }

        for (byte i = 0; i < EotSPoints.POINTSMAX + 1; ++i) {
            m_PlayersNearPoint[i] = new ArrayList<>();
        }

        for (byte i = 0; i < 2 * EotSPoints.POINTSMAX; ++i) {
            m_CurrentPointPlayersCount[i] = 0;
        }
    }


    @Override
    public void postUpdateImpl(int diff) {
        if (getStatus() == BattlegroundStatus.inProgress) {
            m_PointAddingTimer -= diff;

            if (m_PointAddingTimer <= 0) {
                m_PointAddingTimer = EotSMisc.FPointsTickTime;

                if (m_TeamPointsCount[TeamId.ALLIANCE] > 0) {
                    addPoints(Team.ALLIANCE, EotSMisc.TickPoints[m_TeamPointsCount[TeamId.ALLIANCE] - 1]);
                }

                if (m_TeamPointsCount[TeamId.HORDE] > 0) {
                    addPoints(Team.Horde, EotSMisc.TickPoints[m_TeamPointsCount[TeamId.HORDE] - 1]);
                }
            }

            if (m_FlagState == EotSFlagState.WaitRespawn || m_FlagState == EotSFlagState.OnGround) {
                m_FlagsTimer -= diff;

                if (m_FlagsTimer < 0) {
                    m_FlagsTimer = 0;

                    if (m_FlagState == EotSFlagState.WaitRespawn) {
                        respawnFlag(true);
                    } else {
                        respawnFlagAfterDrop();
                    }
                }
            }

            m_TowerCapCheckTimer -= diff;

            if (m_TowerCapCheckTimer <= 0) {
                //check if player joined point
				/*I used this order of calls, because although we will check if one player is in gameobject's distance 2 times
				but we can count of players on current point in CheckSomeoneLeftPoint
				*/
                checkSomeoneJoinedPoint();
                //check if player left point
                checkSomeoneLeftPo();
                updatePointStatuses();
                m_TowerCapCheckTimer = EotSMisc.FPointsTickTime;
            }
        }
    }

    @Override
    public void startingEventCloseDoors() {
        spawnBGObject(EotSObjectTypes.DOORA, BattlegroundConst.RespawnImmediately);
        spawnBGObject(EotSObjectTypes.DOORH, BattlegroundConst.RespawnImmediately);

        for (var i = EotSObjectTypes.ABannerFelReaverCenter; i < EotSObjectTypes.max; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnOneDay);
        }
    }

    @Override
    public void startingEventOpenDoors() {
        spawnBGObject(EotSObjectTypes.DOORA, BattlegroundConst.RespawnOneDay);
        spawnBGObject(EotSObjectTypes.DOORH, BattlegroundConst.RespawnOneDay);

        for (var i = EotSObjectTypes.NBannerFelReaverLeft; i <= EotSObjectTypes.FLAGNETHERSTORM; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
        }

        for (var i = 0; i < EotSPoints.POINTSMAX; ++i) {
            //randomly spawn buff
            var buff = (byte) RandomUtil.URand(0, 2);
            spawnBGObject(EotSObjectTypes.SPEEDBUFFFELREAVER + buff + i * 3, BattlegroundConst.RespawnImmediately);
        }

        // Achievement: Flurry
        triggerGameEvent(EotSMisc.eventStartBattle);
    }

    @Override
    public void endBattleground(Team winner) {
        // Win reward
        if (winner == Team.ALLIANCE) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.ALLIANCE);
        }

        if (winner == Team.Horde) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.Horde);
        }

        // Complete map reward
        rewardHonorToTeam(getBonusHonorFromKill(1), Team.ALLIANCE);
        rewardHonorToTeam(getBonusHonorFromKill(1), Team.Horde);

        super.endBattleground(winner);
    }

    @Override
    public void addPlayer(Player player) {
        var isInBattleground = isPlayerInBattleground(player.getGUID());
        super.addPlayer(player);

        if (!isInBattleground) {
            playerScores.put(player.getGUID(), new BgEyeOfStormScore(player.getGUID(), player.getBgTeam()));
        }

        m_PlayersNearPoint[EotSPoints.PointsMax].add(player.getGUID());
    }

    @Override
    public void removePlayer(Player player, ObjectGuid guid, Team team) {
        // sometimes flag aura not removed :(
        for (var j = EotSPoints.POINTSMAX; j >= 0; --j) {
            for (var i = 0; i < m_PlayersNearPoint[j].size(); ++i) {
                if (Objects.equals(m_PlayersNearPoint[j].get(i), guid)) {
                    m_PlayersNearPoint[j].remove(i);
                }
            }
        }

        if (isFlagPickedup()) {
            if (Objects.equals(m_FlagKeeper, guid)) {
                if (player) {
                    eventPlayerDroppedFlag(player);
                } else {
                    setFlagPicker(ObjectGuid.Empty);
                    respawnFlag(true);
                }
            }
        }
    }


    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (!player.isAlive()) //hack code, must be removed later
        {
            return;
        }

        switch (trigger) {
            case 4530: // Horde Start
            case 4531: // Alliance Start
                if (getStatus() == BattlegroundStatus.WaitJoin && !entered) {
                    teleportPlayerToExploitLocation(player);
                }

                break;
            case EotSPointsTrigger.BloodElfPoint:
                if (m_PointState[EotSPoints.BloodElf] == EotSPointState.UnderControl && m_PointOwnedByTeam[EotSPoints.BloodElf] == getPlayerTeam(player.getGUID())) {
                    if (m_FlagState != 0 && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                        eventPlayerCapturedFlag(player, EotSObjectTypes.FLAGBLOODELF);
                    }
                }

                break;
            case EotSPointsTrigger.FelReaverPoint:
                if (m_PointState[EotSPoints.FelReaver] == EotSPointState.UnderControl && m_PointOwnedByTeam[EotSPoints.FelReaver] == getPlayerTeam(player.getGUID())) {
                    if (m_FlagState != 0 && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                        eventPlayerCapturedFlag(player, EotSObjectTypes.FLAGFELREAVER);
                    }
                }

                break;
            case EotSPointsTrigger.MageTowerPoint:
                if (m_PointState[EotSPoints.MageTower] == EotSPointState.UnderControl && m_PointOwnedByTeam[EotSPoints.MageTower] == getPlayerTeam(player.getGUID())) {
                    if (m_FlagState != 0 && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                        eventPlayerCapturedFlag(player, EotSObjectTypes.FLAGMAGETOWER);
                    }
                }

                break;
            case EotSPointsTrigger.DraeneiRuinsPoint:
                if (m_PointState[EotSPoints.DraeneiRuins] == EotSPointState.UnderControl && m_PointOwnedByTeam[EotSPoints.DraeneiRuins] == getPlayerTeam(player.getGUID())) {
                    if (m_FlagState != 0 && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                        eventPlayerCapturedFlag(player, EotSObjectTypes.FLAGDRAENEIRUINS);
                    }
                }

                break;
            case 4512:
            case 4515:
            case 4517:
            case 4519:
            case 4568:
            case 4569:
            case 4570:
            case 4571:
            case 5866:
                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    @Override
    public boolean setupBattleground() {
        // doors
        if (!addObject(EotSObjectTypes.DOORA, EotSObjectIds.ADoorEyEntry, 2527.59716796875f, 1596.90625f, 1238.4544677734375f, 3.159139871597290039f, 0.173641681671142578f, 0.001514434814453125f, -0.98476982116699218f, 0.008638577535748481f, BattlegroundConst.RespawnImmediately) || !addObject(EotSObjectTypes.DOORH, EotSObjectIds.HDoorEyEntry, 1803.2066650390625f, 1539.486083984375f, 1238.4544677734375f, 3.13898324966430664f, 0.173647880554199218f, 0.0f, 0.984807014465332031f, 0.001244877814315259f, BattlegroundConst.RespawnImmediately) || !addObject(EotSObjectTypes.ABannerFelReaverCenter, EotSObjectIds.ABannerEyEntry, 2057.47265625f, 1735.109130859375f, 1188.065673828125f, 5.305802345275878906f, 0.0f, 0.0f, -0.46947097778320312f, 0.882947921752929687f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerFelReaverLeft, EotSObjectIds.ABannerEyEntry, 2032.248291015625f, 1729.546875f, 1191.2296142578125f, 1.797688722610473632f, 0.0f, 0.0f, 0.7826080322265625f, 0.622514784336090087f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerFelReaverRight, EotSObjectIds.ABannerEyEntry, 2092.338623046875f, 1775.4739990234375f, 1187.504150390625f, 5.811946868896484375f, 0.0f, 0.0f, -0.2334451675415039f, 0.972369968891143798f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerBloodElfCenter, EotSObjectIds.ABannerEyEntry, 2047.1910400390625f, 1349.1927490234375f, 1189.0032958984375f, 4.660029888153076171f, 0.0f, 0.0f, -0.72537422180175781f, 0.688354730606079101f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerBloodElfLeft, EotSObjectIds.ABannerEyEntry, 2074.319580078125f, 1385.779541015625f, 1194.7203369140625f, 0.488691210746765136f, 0.0f, 0.0f, 0.241921424865722656f, 0.970295846462249755f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerBloodElfRight, EotSObjectIds.ABannerEyEntry, 2025.125f, 1386.123291015625f, 1192.7354736328125f, 2.391098499298095703f, 0.0f, 0.0f, 0.930417060852050781f, 0.366502493619918823f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerDraeneiRuinsCenter, EotSObjectIds.ABannerEyEntry, 2276.796875f, 1400.407958984375f, 1196.333740234375f, 2.44346022605895996f, 0.0f, 0.0f, 0.939692497253417968f, 0.34202045202255249f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerDraeneiRuinsLeft, EotSObjectIds.ABannerEyEntry, 2305.776123046875f, 1404.5572509765625f, 1199.384765625f, 1.745326757431030273f, 0.0f, 0.0f, 0.766043663024902343f, 0.642788589000701904f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerDraeneiRuinsRight, EotSObjectIds.ABannerEyEntry, 2245.395751953125f, 1366.4132080078125f, 1195.27880859375f, 2.216565132141113281f, 0.0f, 0.0f, 0.894933700561523437f, 0.44619917869567871f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerMageTowerCenter, EotSObjectIds.ABannerEyEntry, 2270.8359375f, 1784.080322265625f, 1186.757080078125f, 2.426007747650146484f, 0.0f, 0.0f, 0.936672210693359375f, 0.350207358598709106f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerMageTowerLeft, EotSObjectIds.ABannerEyEntry, 2269.126708984375f, 1737.703125f, 1186.8145751953125f, 0.994837164878845214f, 0.0f, 0.0f, 0.477158546447753906f, 0.878817260265350341f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.ABannerMageTowerRight, EotSObjectIds.ABannerEyEntry, 2300.85595703125f, 1741.24658203125f, 1187.793212890625f, 5.497788906097412109f, 0.0f, 0.0f, -0.38268280029296875f, 0.923879802227020263f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerFelReaverCenter, EotSObjectIds.HBannerEyEntry, 2057.45654296875f, 1735.07470703125f, 1187.9063720703125f, 5.35816192626953125f, 0.0f, 0.0f, -0.446197509765625f, 0.894934535026550292f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerFelReaverLeft, EotSObjectIds.HBannerEyEntry, 2032.251708984375f, 1729.532958984375f, 1190.3251953125f, 1.867502212524414062f, 0.0f, 0.0f, 0.803856849670410156f, 0.594822824001312255f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerFelReaverRight, EotSObjectIds.HBannerEyEntry, 2092.354248046875f, 1775.4583740234375f, 1187.079345703125f, 5.881760597229003906f, 0.0f, 0.0f, -0.19936752319335937f, 0.979924798011779785f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerBloodElfCenter, EotSObjectIds.HBannerEyEntry, 2047.1978759765625f, 1349.1875f, 1188.5650634765625f, 4.625123500823974609f, 0.0f, 0.0f, -0.73727703094482421f, 0.67559051513671875f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerBloodElfLeft, EotSObjectIds.HBannerEyEntry, 2074.3056640625f, 1385.7725830078125f, 1194.4686279296875f, 0.471238493919372558f, 0.0f, 0.0f, 0.233445167541503906f, 0.972369968891143798f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerBloodElfRight, EotSObjectIds.HBannerEyEntry, 2025.09375f, 1386.12158203125f, 1192.6536865234375f, 2.373644113540649414f, 0.0f, 0.0f, 0.927183151245117187f, 0.37460830807685852f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerDraeneiRuinsCenter, EotSObjectIds.HBannerEyEntry, 2276.798583984375f, 1400.4410400390625f, 1196.2200927734375f, 2.495818138122558593f, 0.0f, 0.0f, 0.948323249816894531f, 0.317305892705917358f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerDraeneiRuinsLeft, EotSObjectIds.HBannerEyEntry, 2305.763916015625f, 1404.5972900390625f, 1199.3333740234375f, 1.640606880187988281f, 0.0f, 0.0f, 0.731352806091308593f, 0.6819993257522583f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerDraeneiRuinsRight, EotSObjectIds.HBannerEyEntry, 2245.382080078125f, 1366.454833984375f, 1195.1815185546875f, 2.373644113540649414f, 0.0f, 0.0f, 0.927183151245117187f, 0.37460830807685852f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerMageTowerCenter, EotSObjectIds.HBannerEyEntry, 2270.869873046875f, 1784.0989990234375f, 1186.4384765625f, 2.356194972991943359f, 0.0f, 0.0f, 0.923879623413085937f, 0.382683247327804565f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerMageTowerLeft, EotSObjectIds.HBannerEyEntry, 2268.59716796875f, 1737.0191650390625f, 1186.75390625f, 0.942476630210876464f, 0.0f, 0.0f, 0.453989982604980468f, 0.891006767749786376f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.HBannerMageTowerRight, EotSObjectIds.HBannerEyEntry, 2301.01904296875f, 1741.4930419921875f, 1187.48974609375f, 5.375615119934082031f, 0.0f, 0.0f, -0.4383707046508789f, 0.898794233798980712f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerFelReaverCenter, EotSObjectIds.NBannerEyEntry, 2057.4931640625f, 1735.111083984375f, 1187.675537109375f, 5.340708732604980468f, 0.0f, 0.0f, -0.45398998260498046f, 0.891006767749786376f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerFelReaverLeft, EotSObjectIds.NBannerEyEntry, 2032.2569580078125f, 1729.5572509765625f, 1191.0802001953125f, 1.797688722610473632f, 0.0f, 0.0f, 0.7826080322265625f, 0.622514784336090087f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerFelReaverRight, EotSObjectIds.NBannerEyEntry, 2092.395751953125f, 1775.451416015625f, 1186.965576171875f, 5.89921426773071289f, 0.0f, 0.0f, -0.19080829620361328f, 0.981627285480499267f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerBloodElfCenter, EotSObjectIds.NBannerEyEntry, 2047.1875f, 1349.1944580078125f, 1188.5731201171875f, 4.642575740814208984f, 0.0f, 0.0f, -0.731353759765625f, 0.681998312473297119f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerBloodElfLeft, EotSObjectIds.NBannerEyEntry, 2074.3212890625f, 1385.76220703125f, 1194.362060546875f, 0.488691210746765136f, 0.0f, 0.0f, 0.241921424865722656f, 0.970295846462249755f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerBloodElfRight, EotSObjectIds.NBannerEyEntry, 2025.13720703125f, 1386.1336669921875f, 1192.5482177734375f, 2.391098499298095703f, 0.0f, 0.0f, 0.930417060852050781f, 0.366502493619918823f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerDraeneiRuinsCenter, EotSObjectIds.NBannerEyEntry, 2276.833251953125f, 1400.4375f, 1196.146728515625f, 2.478367090225219726f, 0.0f, 0.0f, 0.94551849365234375f, 0.325568377971649169f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerDraeneiRuinsLeft, EotSObjectIds.NBannerEyEntry, 2305.77783203125f, 1404.5364990234375f, 1199.246337890625f, 1.570795774459838867f, 0.0f, 0.0f, 0.707106590270996093f, 0.707106947898864746f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerDraeneiRuinsRight, EotSObjectIds.NBannerEyEntry, 2245.40966796875f, 1366.4410400390625f, 1195.1107177734375f, 2.356194972991943359f, 0.0f, 0.0f, 0.923879623413085937f, 0.382683247327804565f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerMageTowerCenter, EotSObjectIds.NBannerEyEntry, 2270.84033203125f, 1784.1197509765625f, 1186.1473388671875f, 2.303830623626708984f, 0.0f, 0.0f, 0.913544654846191406f, 0.406738430261611938f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerMageTowerLeft, EotSObjectIds.NBannerEyEntry, 2268.46533203125f, 1736.8385009765625f, 1186.742919921875f, 0.942476630210876464f, 0.0f, 0.0f, 0.453989982604980468f, 0.891006767749786376f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.NBannerMageTowerRight, EotSObjectIds.NBannerEyEntry, 2300.9931640625f, 1741.5504150390625f, 1187.10693359375f, 5.375615119934082031f, 0.0f, 0.0f, -0.4383707046508789f, 0.898794233798980712f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.FLAGNETHERSTORM, EotSObjectIds.flag2EyEntry, 2174.444580078125f, 1569.421875f, 1159.852783203125f, 4.625123500823974609f, 0.0f, 0.0f, -0.73727703094482421f, 0.67559051513671875f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.FLAGFELREAVER, EotSObjectIds.flag1EyEntry, 2044.28f, 1729.68f, 1189.96f, -0.017453f, 0, 0, 0.008727f, -0.999962f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.FLAGBLOODELF, EotSObjectIds.flag1EyEntry, 2048.83f, 1393.65f, 1194.49f, 0.20944f, 0, 0, 0.104528f, 0.994522f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.FLAGDRAENEIRUINS, EotSObjectIds.flag1EyEntry, 2286.56f, 1402.36f, 1197.11f, 3.72381f, 0, 0, 0.957926f, -0.287016f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.FLAGMAGETOWER, EotSObjectIds.flag1EyEntry, 2284.48f, 1731.23f, 1189.99f, 2.89725f, 0, 0, 0.992546f, 0.121869f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.TOWERCAPFELREAVER, EotSObjectIds.frTowerCapEyEntry, 2024.600708f, 1742.819580f, 1195.157715f, 2.443461f, 0, 0, 0.939693f, 0.342020f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.TOWERCAPBLOODELF, EotSObjectIds.beTowerCapEyEntry, 2050.493164f, 1372.235962f, 1194.563477f, 1.710423f, 0, 0, 0.754710f, 0.656059f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.TOWERCAPDRAENEIRUINS, EotSObjectIds.drTowerCapEyEntry, 2301.010498f, 1386.931641f, 1197.183472f, 1.570796f, 0, 0, 0.707107f, 0.707107f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.TOWERCAPMAGETOWER, EotSObjectIds.huTowerCapEyEntry, 2282.121582f, 1760.006958f, 1189.707153f, 1.919862f, 0, 0, 0.819152f, 0.573576f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.SPEEDBUFFFELREAVER, EotSObjectIds.speedBuffFelReaverEyEntry, 2046.462646484375f, 1749.1666259765625f, 1190.010498046875f, 5.410521507263183593f, 0.0f, 0.0f, -0.42261791229248046f, 0.906307935714721679f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.REGENBUFFFELREAVER, EotSObjectIds.restorationBuffFelReaverEyEntry, 2046.462646484375f, 1749.1666259765625f, 1190.010498046875f, 5.410521507263183593f, 0.0f, 0.0f, -0.42261791229248046f, 0.906307935714721679f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.BERSERKBUFFFELREAVER, EotSObjectIds.berserkBuffFelReaverEyEntry, 2046.462646484375f, 1749.1666259765625f, 1190.010498046875f, 5.410521507263183593f, 0.0f, 0.0f, -0.42261791229248046f, 0.906307935714721679f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.SPEEDBUFFBLOODELF, EotSObjectIds.speedBuffBloodElfEyEntry, 2050.46826171875f, 1372.2020263671875f, 1194.5634765625f, 1.675513744354248046f, 0.0f, 0.0f, 0.743144035339355468f, 0.669131457805633544f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.REGENBUFFBLOODELF, EotSObjectIds.restorationBuffBloodElfEyEntry, 2050.46826171875f, 1372.2020263671875f, 1194.5634765625f, 1.675513744354248046f, 0.0f, 0.0f, 0.743144035339355468f, 0.669131457805633544f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.BERSERKBUFFBLOODELF, EotSObjectIds.berserkBuffBloodElfEyEntry, 2050.46826171875f, 1372.2020263671875f, 1194.5634765625f, 1.675513744354248046f, 0.0f, 0.0f, 0.743144035339355468f, 0.669131457805633544f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.SPEEDBUFFDRAENEIRUINS, EotSObjectIds.speedBuffDraeneiRuinsEyEntry, 2302.4765625f, 1391.244873046875f, 1197.7364501953125f, 1.762782454490661621f, 0.0f, 0.0f, 0.771624565124511718f, 0.636078238487243652f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.REGENBUFFDRAENEIRUINS, EotSObjectIds.restorationBuffDraeneiRuinsEyEntry, 2302.4765625f, 1391.244873046875f, 1197.7364501953125f, 1.762782454490661621f, 0.0f, 0.0f, 0.771624565124511718f, 0.636078238487243652f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.BERSERKBUFFDRAENEIRUINS, EotSObjectIds.berserkBuffDraeneiRuinsEyEntry, 2302.4765625f, 1391.244873046875f, 1197.7364501953125f, 1.762782454490661621f, 0.0f, 0.0f, 0.771624565124511718f, 0.636078238487243652f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.SPEEDBUFFMAGETOWER, EotSObjectIds.speedBuffMageTowerEyEntry, 2283.7099609375f, 1748.8699951171875f, 1189.7071533203125f, 4.782202720642089843f, 0.0f, 0.0f, -0.68199825286865234f, 0.731353819370269775f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.REGENBUFFMAGETOWER, EotSObjectIds.restorationBuffMageTowerEyEntry, 2283.7099609375f, 1748.8699951171875f, 1189.7071533203125f, 4.782202720642089843f, 0.0f, 0.0f, -0.68199825286865234f, 0.731353819370269775f, BattlegroundConst.RespawnOneDay) || !addObject(EotSObjectTypes.BERSERKBUFFMAGETOWER, EotSObjectIds.berserkBuffMageTowerEyEntry, 2283.7099609375f, 1748.8699951171875f, 1189.7071533203125f, 4.782202720642089843f, 0.0f, 0.0f, -0.68199825286865234f, 0.731353819370269775f, BattlegroundConst.RespawnOneDay)) {
            Logs.SQL.error("BatteGroundEY: Failed to spawn some Objects. The battleground was not created.");

            return false;
        }

        var sg = global.getObjectMgr().getWorldSafeLoc(EotSGaveyardIds.mainAlliance);

        if (sg == null || !addSpiritGuide(EotSCreaturesTypes.SPIRITMAINALLIANCE, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), 3.124139f, TeamId.ALLIANCE)) {
            Logs.SQL.error("BatteGroundEY: Failed to spawn spirit guide. The battleground was not created.");

            return false;
        }

        sg = global.getObjectMgr().getWorldSafeLoc(EotSGaveyardIds.mainHorde);

        if (sg == null || !addSpiritGuide(EotSCreaturesTypes.SPIRITMAINHORDE, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), 3.193953f, TeamId.HORDE)) {
            Logs.SQL.error("BatteGroundEY: Failed to spawn spirit guide. The battleground was not created.");

            return false;
        }

        return true;
    }

    @Override
    public void reset() {
        //call parent's class reset
        super.reset();

        m_TeamScores[TeamId.ALLIANCE] = 0;
        m_TeamScores[TeamId.HORDE] = 0;
        m_TeamPointsCount[TeamId.ALLIANCE] = 0;
        m_TeamPointsCount[TeamId.HORDE] = 0;
        m_HonorScoreTics[TeamId.ALLIANCE] = 0;
        m_HonorScoreTics[TeamId.HORDE] = 0;
        m_FlagState = EotSFlagState.OnBase;
        m_FlagCapturedBgObjectType = 0;
        m_FlagKeeper.clear();
        m_DroppedFlagGUID.clear();
        m_PointAddingTimer = 0;
        m_TowerCapCheckTimer = 0;
        var isBGWeekend = global.getBattlegroundMgr().isBGWeekend(getTypeID());
        m_HonorTics = (isBGWeekend) ? EotSMisc.EYWeekendHonorTicks : EotSMisc.notEYWeekendHonorTicks;

        for (byte i = 0; i < EotSPoints.POINTSMAX; ++i) {
            m_PointOwnedByTeam[i] = Team.other;
            m_PointState[i] = EotSPointState.Uncontrolled;
            m_PointBarStatus[i] = EotSProgressBarConsts.ProgressBarStateMiddle;
            m_PlayersNearPoint[i].clear();
        }

        m_PlayersNearPoint[EotSPoints.PlayersOutOfPoints].clear();
    }

    @Override
    public void handleKillPlayer(Player player, Player killer) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        super.handleKillPlayer(player, killer);
        eventPlayerDroppedFlag(player);
    }

    @Override
    public void eventPlayerDroppedFlag(Player player) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            // if not running, do not cast things at the dropper player, neither send unnecessary messages
            // just take off the aura
            if (isFlagPickedup() && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                setFlagPicker(ObjectGuid.Empty);
                player.removeAura(EotSMisc.spellNetherstormFlag);
            }

            return;
        }

        if (!isFlagPickedup()) {
            return;
        }

        if (ObjectGuid.opNotEquals(getFlagPickerGUID(), player.getGUID())) {
            return;
        }

        setFlagPicker(ObjectGuid.Empty);
        player.removeAura(EotSMisc.spellNetherstormFlag);
        m_FlagState = EotSFlagState.OnGround;
        m_FlagsTimer = EotSMisc.FLAGRESPAWNTIME;
        player.castSpell(player, BattlegroundConst.SpellRecentlyDroppedFlag, true);
        player.castSpell(player, EotSMisc.spellPlayerDroppedFlag, true);
        //this does not work correctly :((it should remove flag carrier name)
        updateWorldState(EotSWorldStateIds.netherstormFlagStateHorde, EotSFlagState.WaitRespawn.getValue());
        updateWorldState(EotSWorldStateIds.netherstormFlagStateAlliance, EotSFlagState.WaitRespawn.getValue());

        if (getPlayerTeam(player.getGUID()) == Team.ALLIANCE) {
            sendBroadcastText(EotSBroadcastTexts.flagDropped, ChatMsg.BgSystemAlliance, null);
        } else {
            sendBroadcastText(EotSBroadcastTexts.flagDropped, ChatMsg.BgSystemHorde, null);
        }
    }

    @Override
    public void eventPlayerClickedOnFlag(Player player, GameObject target_obj) {
        if (getStatus() != BattlegroundStatus.inProgress || isFlagPickedup() || !player.isWithinDistInMap(target_obj, 10)) {
            return;
        }

        if (getPlayerTeam(player.getGUID()) == Team.ALLIANCE) {
            updateWorldState(EotSWorldStateIds.netherstormFlagStateAlliance, EotSFlagState.OnPlayer.getValue());
            playSoundToAll(EotSSoundIds.flagPickedUpAlliance);
        } else {
            updateWorldState(EotSWorldStateIds.netherstormFlagStateHorde, EotSFlagState.OnPlayer.getValue());
            playSoundToAll(EotSSoundIds.flagPickedUpHorde);
        }

        if (m_FlagState == EotSFlagState.OnBase) {
            updateWorldState(EotSWorldStateIds.netherstormFlag, 0);
        }

        m_FlagState = EotSFlagState.OnPlayer;

        spawnBGObject(EotSObjectTypes.FLAGNETHERSTORM, BattlegroundConst.RespawnOneDay);
        setFlagPicker(player.getGUID());
        //get flag aura on player
        player.castSpell(player, EotSMisc.spellNetherstormFlag, true);
        player.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.PvPActive);

        if (getPlayerTeam(player.getGUID()) == Team.ALLIANCE) {
            sendBroadcastText(EotSBroadcastTexts.takenFlag, ChatMsg.BgSystemAlliance, player);
        } else {
            sendBroadcastText(EotSBroadcastTexts.takenFlag, ChatMsg.BgSystemHorde, player);
        }
    }


    @Override
    public boolean updatePlayerScore(Player player, ScoreType type, int value) {
        return updatePlayerScore(player, type, value, true);
    }

    @Override
    public boolean updatePlayerScore(Player player, ScoreType type, int value, boolean doAddHonor) {
        if (!super.updatePlayerScore(player, type, value, doAddHonor)) {
            return false;
        }

        switch (type) {
            case FlagCaptures:
                player.updateCriteria(CriteriaType.TrackedWorldStateUIModified, EotSMisc.objectiveCaptureFlag);

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public WorldSafeLocsEntry getClosestGraveYard(Player player) {
        int g_id;
        var team = getPlayerTeam(player.getGUID());

        switch (team) {
            case Alliance:
                g_id = EotSGaveyardIds.mainAlliance;

                break;
            case Horde:
                g_id = EotSGaveyardIds.mainHorde;

                break;
            default:
                return null;
        }

        var entry = global.getObjectMgr().getWorldSafeLoc(g_id);
        var nearestEntry = entry;

        if (entry == null) {
            Log.outError(LogFilter.Battleground, "BattlegroundEY: The main team graveyard could not be found. The graveyard system will not be operational!");

            return null;
        }

        var plr_x = player.getLocation().getX();
        var plr_y = player.getLocation().getY();
        var plr_z = player.getLocation().getZ();

        var distance = (entry.loc.getX() - plr_x) * (entry.loc.getX() - plr_x) + (entry.loc.getY() - plr_y) * (entry.loc.getY() - plr_y) + (entry.loc.getZ() - plr_z) * (entry.loc.getZ() - plr_z);
        var nearestDistance = distance;

        for (byte i = 0; i < EotSPoints.POINTSMAX; ++i) {
            if (m_PointOwnedByTeam[i] == team && m_PointState[i] == EotSPointState.UnderControl) {
                entry = global.getObjectMgr().getWorldSafeLoc(EotSMisc.m_CapturingPointTypes[i].graveYardId);

                if (entry == null) {
                    Log.outError(LogFilter.Battleground, "BattlegroundEY: Graveyard {0} could not be found.", EotSMisc.m_CapturingPointTypes[i].graveYardId);
                } else {
                    distance = (entry.loc.getX() - plr_x) * (entry.loc.getX() - plr_x) + (entry.loc.getY() - plr_y) * (entry.loc.getY() - plr_y) + (entry.loc.getZ() - plr_z) * (entry.loc.getZ() - plr_z);

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestEntry = entry;
                    }
                }
            }
        }

        return nearestEntry;
    }

    @Override
    public WorldSafeLocsEntry getExploitTeleportLocation(Team team) {
        return global.getObjectMgr().getWorldSafeLoc(team == Team.ALLIANCE ? EotSMisc.ExploitTeleportLocationAlliance : EotSMisc.exploitTeleportLocationHorde);
    }

    @Override
    public Team getPrematureWinner() {
        if (getTeamScore(TeamId.ALLIANCE) > getTeamScore(TeamId.HORDE)) {
            return Team.ALLIANCE;
        } else if (getTeamScore(TeamId.HORDE) > getTeamScore(TeamId.ALLIANCE)) {
            return Team.Horde;
        }

        return super.getPrematureWinner();
    }


    @Override
    public ObjectGuid getFlagPickerGUID() {
        return getFlagPickerGUID(-1);
    }

    @Override
    public ObjectGuid getFlagPickerGUID(int team) {
        return m_FlagKeeper;
    }

    @Override
    public void setDroppedFlagGUID(ObjectGuid guid, int teamID) {
        m_DroppedFlagGUID = guid;
    }

    private void addPoints(Team team, int points) {
        var team_index = getTeamIndexByTeamId(team);
        m_TeamScores[team_index] += points;
        m_HonorScoreTics[team_index] += points;

        if (m_HonorScoreTics[team_index] >= m_HonorTics) {
            rewardHonorToTeam(getBonusHonorFromKill(1), team);
            m_HonorScoreTics[team_index] -= m_HonorTics;
        }

        updateTeamScore(team_index);
    }

    private BattlegroundPointCaptureStatus getPointCaptureStatus(int point) {
        if (m_PointBarStatus[point].getValue() >= EotSProgressBarConsts.ProgressBarAliControlled.getValue()) {
            return BattlegroundPointCaptureStatus.AllianceControlled;
        }

        if (m_PointBarStatus[point].getValue() <= EotSProgressBarConsts.ProgressBarHordeControlled.getValue()) {
            return BattlegroundPointCaptureStatus.HordeControlled;
        }

        if (m_CurrentPointPlayersCount[2 * point] == m_CurrentPointPlayersCount[2 * point + 1]) {
            return BattlegroundPointCaptureStatus.Neutral;
        }

        return m_CurrentPointPlayersCount[2 * point] > m_CurrentPointPlayersCount[2 * point + 1] ? BattlegroundPointCaptureStatus.AllianceCapturing : BattlegroundPointCaptureStatus.HordeCapturing;
    }

    private void checkSomeoneJoinedPoint() {
        GameObject obj;

        for (byte i = 0; i < EotSPoints.POINTSMAX; ++i) {
            obj = getBgMap().getGameObject(BgObjects[EotSObjectTypes.TOWERCAPFELREAVER + i]);

            if (obj) {
                byte j = 0;

                while (j < m_PlayersNearPoint[EotSPoints.PointsMax].size()) {
                    var player = global.getObjAccessor().findPlayer(m_PlayersNearPoint[EotSPoints.PointsMax].get(j));

                    if (!player) {
                        Log.outError(LogFilter.Battleground, "BattlegroundEY:CheckSomeoneJoinedPoint: player ({0}) could not be found!", m_PlayersNearPoint[EotSPoints.PointsMax].get(j).toString());
                        ++j;

                        continue;
                    }

                    if (player.getCanCaptureTowerPoint() && player.isWithinDistInMap(obj, (float) EotSProgressBarConsts.PointRadius.getValue())) {
                        //player joined point!
                        //show progress bar
                        player.sendUpdateWorldState(EotSWorldStateIds.progressBarPercentGrey, (int) EotSProgressBarConsts.progressBarPercentGrey.getValue());
                        player.sendUpdateWorldState(EotSWorldStateIds.progressBarStatus, (int) m_PointBarStatus[i]);
                        player.sendUpdateWorldState(EotSWorldStateIds.progressBarShow, (int) EotSProgressBarConsts.progressBarShow.getValue());
                        //add player to point
                        m_PlayersNearPoint[i].add(m_PlayersNearPoint[EotSPoints.PointsMax].get(j));
                        //remove player from "free space"
                        m_PlayersNearPoint[EotSPoints.PointsMax].remove(j);
                    } else {
                        ++j;
                    }
                }
            }
        }
    }

    private void checkSomeoneLeftPo() {
        //reset current point counts
        for (byte i = 0; i < 2 * EotSPoints.POINTSMAX; ++i) {
            m_CurrentPointPlayersCount[i] = 0;
        }

        GameObject obj;

        for (byte i = 0; i < EotSPoints.POINTSMAX; ++i) {
            obj = getBgMap().getGameObject(BgObjects[EotSObjectTypes.TOWERCAPFELREAVER + i]);

            if (obj) {
                byte j = 0;

                while (j < m_PlayersNearPoint[i].size()) {
                    var player = global.getObjAccessor().findPlayer(m_PlayersNearPoint[i].get(j));

                    if (!player) {
                        Log.outError(LogFilter.Battleground, "BattlegroundEY:CheckSomeoneLeftPoint player ({0}) could not be found!", m_PlayersNearPoint[i].get(j).toString());
                        //move non-existing players to "free space" - this will cause many errors showing in log, but it is a very important bug
                        m_PlayersNearPoint[EotSPoints.PointsMax].add(m_PlayersNearPoint[i].get(j));
                        m_PlayersNearPoint[i].remove(j);

                        continue;
                    }

                    if (!player.getCanCaptureTowerPoint() || !player.isWithinDistInMap(obj, (float) EotSProgressBarConsts.PointRadius.getValue())) {
                        //move player out of point (add him to players that are out of points
                        m_PlayersNearPoint[EotSPoints.PointsMax].add(m_PlayersNearPoint[i].get(j));
                        m_PlayersNearPoint[i].remove(j);
                        player.sendUpdateWorldState(EotSWorldStateIds.progressBarShow, (int) EotSProgressBarConsts.ProgressBarDontShow.getValue());
                    } else {
                        //player is neat flag, so update count:
                        m_CurrentPointPlayersCount[2 * i + getTeamIndexByTeamId(getPlayerTeam(player.getGUID()))]++;
                        ++j;
                    }
                }
            }
        }
    }

    private void updatePointStatuses() {
        for (byte point = 0; point < EotSPoints.POINTSMAX; ++point) {
            if (!m_PlayersNearPoint[point].isEmpty()) {
                //count new point bar status:
                var pointDelta = (int) (m_CurrentPointPlayersCount[2 * point]) - (int) (m_CurrentPointPlayersCount[2 * point + 1]);
                tangible.RefObject<Integer> tempRef_pointDelta = new tangible.RefObject<Integer>(pointDelta);
                MathUtil.RoundToInterval(tempRef_pointDelta, -EotSProgressBarConsts.PointMaxCapturersCount.getValue(), EotSProgressBarConsts.PointMaxCapturersCount);
                pointDelta = tempRef_pointDelta.refArgValue;
                m_PointBarStatus[point] += pointDelta;

                if (m_PointBarStatus[point].getValue() > EotSProgressBarConsts.ProgressBarAliControlled.getValue()) {
                    //point is fully alliance's
                    m_PointBarStatus[point] = EotSProgressBarConsts.ProgressBarAliControlled;
                }

                if (m_PointBarStatus[point].getValue() < EotSProgressBarConsts.ProgressBarHordeControlled.getValue()) {
                    //point is fully horde's
                    m_PointBarStatus[point] = EotSProgressBarConsts.ProgressBarHordeControlled;
                }

                int pointOwnerTeamId;

                //find which team should own this point
                if (m_PointBarStatus[point].getValue() <= EotSProgressBarConsts.ProgressBarNeutralLow.getValue()) {
                    pointOwnerTeamId = (int) Team.Horde.getValue();
                } else if (m_PointBarStatus[point].getValue() >= EotSProgressBarConsts.ProgressBarNeutralHigh.getValue()) {
                    pointOwnerTeamId = (int) Team.ALLIANCE.getValue();
                } else {
                    pointOwnerTeamId = EotSPointState.NoOwner.getValue();
                }

                for (byte i = 0; i < m_PlayersNearPoint[point].size(); ++i) {
                    var player = global.getObjAccessor().findPlayer(m_PlayersNearPoint[point].get(i));

                    if (player) {
                        player.sendUpdateWorldState(EotSWorldStateIds.progressBarStatus, (int) m_PointBarStatus[point]);
                        var team = getPlayerTeam(player.getGUID());

                        //if point owner changed we must evoke event!
                        if (pointOwnerTeamId != (int) m_PointOwnedByTeam[point]) {
                            //point was uncontrolled and player is from team which captured point
                            if (m_PointState[point] == EotSPointState.Uncontrolled && (int) team.getValue() == pointOwnerTeamId) {
                                eventTeamCapturedPoint(player, point);
                            }

                            //point was under control and player isn't from team which controlled it
                            if (m_PointState[point] == EotSPointState.UnderControl && team != m_PointOwnedByTeam[point]) {
                                eventTeamLostPoint(player, point);
                            }
                        }

                        // @workaround The original AreaTrigger is covered by a bigger one and not triggered on client side.
                        if (point == EotSPoints.FELREAVER && m_PointOwnedByTeam[point] == team) {
                            if (m_FlagState != 0 && Objects.equals(getFlagPickerGUID(), player.getGUID())) {
                                if (player.getDistance(2044.0f, 1729.729f, 1190.03f) < 3.0f) {
                                    eventPlayerCapturedFlag(player, EotSObjectTypes.FLAGFELREAVER);
                                }
                            }
                        }
                    }
                }
            }

            var captureStatus = getPointCaptureStatus(point);

            if (m_LastPointCaptureStatus[point] != captureStatus) {
                updateWorldState(EotSMisc.m_PointsIconStruct[point].worldStateAllianceStatusBarIcon, (int) (captureStatus == BattlegroundPointCaptureStatus.AllianceControlled ? 2 : (captureStatus == BattlegroundPointCaptureStatus.AllianceCapturing ? 1 : 0)));
                updateWorldState(EotSMisc.m_PointsIconStruct[point].worldStateHordeStatusBarIcon, (int) (captureStatus == BattlegroundPointCaptureStatus.HordeControlled ? 2 : (captureStatus == BattlegroundPointCaptureStatus.HordeCapturing ? 1 : 0)));
                m_LastPointCaptureStatus[point] = captureStatus;
            }
        }
    }

    private void updateTeamScore(int team) {
        var score = getTeamScore(team);

        if (score >= EotSScoreIds.maxTeamScore) {
            score = EotSScoreIds.maxTeamScore;

            if (team == TeamId.ALLIANCE) {
                endBattleground(Team.ALLIANCE);
            } else {
                endBattleground(Team.Horde);
            }
        }

        if (team == TeamId.ALLIANCE) {
            updateWorldState(EotSWorldStateIds.allianceResources, (int) score);
        } else {
            updateWorldState(EotSWorldStateIds.hordeResources, (int) score);
        }
    }

    private void updatePointsCount(Team team) {
        if (team == Team.ALLIANCE) {
            updateWorldState(EotSWorldStateIds.allianceBase, (int) m_TeamPointsCount[TeamId.ALLIANCE]);
        } else {
            updateWorldState(EotSWorldStateIds.hordeBase, (int) m_TeamPointsCount[TeamId.HORDE]);
        }
    }

    private void updatePointsIcons(Team team, int Point) {
        //we MUST firstly send 0, after that we can send 1!!!
        if (m_PointState[Point] == EotSPointState.UnderControl) {
            updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateControlIndex, 0);

            if (team == Team.ALLIANCE) {
                updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateAllianceControlledIndex, 1);
            } else {
                updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateHordeControlledIndex, 1);
            }
        } else {
            if (team == Team.ALLIANCE) {
                updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateAllianceControlledIndex, 0);
            } else {
                updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateHordeControlledIndex, 0);
            }

            updateWorldState(EotSMisc.m_PointsIconStruct[Point].worldStateControlIndex, 1);
        }
    }

    private void respawnFlag(boolean send_message) {
        if (m_FlagCapturedBgObjectType > 0) {
            spawnBGObject((int) m_FlagCapturedBgObjectType, BattlegroundConst.RespawnOneDay);
        }

        m_FlagCapturedBgObjectType = 0;
        m_FlagState = EotSFlagState.OnBase;
        spawnBGObject(EotSObjectTypes.FLAGNETHERSTORM, BattlegroundConst.RespawnImmediately);

        if (send_message) {
            sendBroadcastText(EotSBroadcastTexts.flagReset, ChatMsg.BgSystemNeutral);
            playSoundToAll(EotSSoundIds.flagReset); // flags respawned sound...
        }

        updateWorldState(EotSWorldStateIds.netherstormFlag, 1);
    }

    private void respawnFlagAfterDrop() {
        respawnFlag(true);

        var obj = getBgMap().getGameObject(getDroppedFlagGUID());

        if (obj) {
            obj.delete();
        } else {
            Log.outError(LogFilter.Battleground, "BattlegroundEY: Unknown dropped flag ({0}).", getDroppedFlagGUID().toString());
        }

        setDroppedFlagGUID(ObjectGuid.Empty);
    }

    private void eventTeamLostPoint(Player player, int Point) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        //Natural point
        var team = m_PointOwnedByTeam[Point];

        if (team == 0) {
            return;
        }

        if (team == Team.ALLIANCE) {
            m_TeamPointsCount[TeamId.ALLIANCE]--;
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeAlliance, BattlegroundConst.RespawnOneDay);
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeAlliance + 1, BattlegroundConst.RespawnOneDay);
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeAlliance + 2, BattlegroundConst.RespawnOneDay);
        } else {
            m_TeamPointsCount[TeamId.HORDE]--;
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeHorde, BattlegroundConst.RespawnOneDay);
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeHorde + 1, BattlegroundConst.RespawnOneDay);
            spawnBGObject(EotSMisc.m_LosingPointTypes[Point].despawnObjectTypeHorde + 2, BattlegroundConst.RespawnOneDay);
        }

        spawnBGObject(EotSMisc.m_LosingPointTypes[Point].spawnNeutralObjectType, BattlegroundConst.RespawnImmediately);
        spawnBGObject(EotSMisc.m_LosingPointTypes[Point].spawnNeutralObjectType + 1, BattlegroundConst.RespawnImmediately);
        spawnBGObject(EotSMisc.m_LosingPointTypes[Point].spawnNeutralObjectType + 2, BattlegroundConst.RespawnImmediately);

        //buff isn't despawned

        m_PointOwnedByTeam[Point] = Team.other;
        m_PointState[Point] = EotSPointState.NoOwner;

        if (team == Team.ALLIANCE) {
            sendBroadcastText(EotSMisc.m_LosingPointTypes[Point].messageIdAlliance, ChatMsg.BgSystemAlliance, player);
        } else {
            sendBroadcastText(EotSMisc.m_LosingPointTypes[Point].messageIdHorde, ChatMsg.BgSystemHorde, player);
        }

        updatePointsIcons(team, Point);
        updatePointsCount(team);

        //remove bonus honor aura trigger creature when node is lost
        if (Point < EotSPoints.POINTSMAX) {
            delCreature(Point + 6); //null checks are in DelCreature! 0-5 spirit guides
        }
    }

    private void eventTeamCapturedPoint(Player player, int Point) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        var team = getPlayerTeam(player.getGUID());

        spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].despawnNeutralObjectType, BattlegroundConst.RespawnOneDay);
        spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].despawnNeutralObjectType + 1, BattlegroundConst.RespawnOneDay);
        spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].despawnNeutralObjectType + 2, BattlegroundConst.RespawnOneDay);

        if (team == Team.ALLIANCE) {
            m_TeamPointsCount[TeamId.ALLIANCE]++;
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeAlliance, BattlegroundConst.RespawnImmediately);
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeAlliance + 1, BattlegroundConst.RespawnImmediately);
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeAlliance + 2, BattlegroundConst.RespawnImmediately);
        } else {
            m_TeamPointsCount[TeamId.HORDE]++;
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeHorde, BattlegroundConst.RespawnImmediately);
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeHorde + 1, BattlegroundConst.RespawnImmediately);
            spawnBGObject(EotSMisc.m_CapturingPointTypes[Point].spawnObjectTypeHorde + 2, BattlegroundConst.RespawnImmediately);
        }

        //buff isn't respawned

        m_PointOwnedByTeam[Point] = team;
        m_PointState[Point] = EotSPointState.UnderControl;

        if (team == Team.ALLIANCE) {
            sendBroadcastText(EotSMisc.m_CapturingPointTypes[Point].messageIdAlliance, ChatMsg.BgSystemAlliance, player);
        } else {
            sendBroadcastText(EotSMisc.m_CapturingPointTypes[Point].messageIdHorde, ChatMsg.BgSystemHorde, player);
        }

        if (!BgCreatures[Point].isEmpty()) {
            delCreature(Point);
        }

        var sg = global.getObjectMgr().getWorldSafeLoc(EotSMisc.m_CapturingPointTypes[Point].graveYardId);

        if (sg == null || !addSpiritGuide(Point, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), 3.124139f, getTeamIndexByTeamId(team))) {
            Log.outError(LogFilter.Battleground, "BatteGroundEY: Failed to spawn spirit guide. point: {0}, team: {1}, graveyard_id: {2}", Point, team, EotSMisc.m_CapturingPointTypes[Point].graveYardId);
        }

        //    SpawnBGCreature(Point, RESPAWN_IMMEDIATELY);

        updatePointsIcons(team, Point);
        updatePointsCount(team);

        if (Point >= EotSPoints.POINTSMAX) {
            return;
        }

        var trigger = getBGCreature(Point + 6); //0-5 spirit guides

        if (!trigger) {
            trigger = addCreature(SharedConst.worldTrigger, Point + 6, EotSMisc.TriggerPositions[Point], getTeamIndexByTeamId(team));
        }

        //add bonus honor aura trigger creature when node is accupied
        //cast bonus aura (+50% honor in 25yards)
        //aura should only apply to players who have accupied the node, set correct faction for trigger
        if (trigger) {
            trigger.setFaction(team == Team.ALLIANCE ? 84 : 83);
            trigger.castSpell(trigger, BattlegroundConst.SpellHonorableDefender25y, false);
        }
    }

    private void eventPlayerCapturedFlag(Player player, int BgObjectType) {
        if (getStatus() != BattlegroundStatus.inProgress || ObjectGuid.opNotEquals(getFlagPickerGUID(), player.getGUID())) {
            return;
        }

        setFlagPicker(ObjectGuid.Empty);
        m_FlagState = EotSFlagState.WaitRespawn;
        player.removeAura(EotSMisc.spellNetherstormFlag);

        player.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.PvPActive);

        var team = getPlayerTeam(player.getGUID());

        if (team == Team.ALLIANCE) {
            sendBroadcastText(EotSBroadcastTexts.allianceCapturedFlag, ChatMsg.BgSystemAlliance, player);
            playSoundToAll(EotSSoundIds.flagCapturedAlliance);
        } else {
            sendBroadcastText(EotSBroadcastTexts.hordeCapturedFlag, ChatMsg.BgSystemHorde, player);
            playSoundToAll(EotSSoundIds.flagCapturedHorde);
        }

        spawnBGObject((int) BgObjectType, BattlegroundConst.RespawnImmediately);

        m_FlagsTimer = EotSMisc.FLAGRESPAWNTIME;
        m_FlagCapturedBgObjectType = BgObjectType;

        var team_id = getTeamIndexByTeamId(team);

        if (m_TeamPointsCount[team_id] > 0) {
            addPoints(team, EotSMisc.FlagPoints[m_TeamPointsCount[team_id] - 1]);
        }

        updateWorldState(EotSWorldStateIds.netherstormFlagStateHorde, EotSFlagState.OnBase.getValue());
        updateWorldState(EotSWorldStateIds.netherstormFlagStateAlliance, EotSFlagState.OnBase.getValue());

        updatePlayerScore(player, ScoreType.flagCaptures, 1);
    }

    private void setFlagPicker(ObjectGuid guid) {
        m_FlagKeeper = guid;
    }

    private boolean isFlagPickedup() {
        return !m_FlagKeeper.isEmpty();
    }

    private ObjectGuid getDroppedFlagGUID() {
        return m_DroppedFlagGUID;
    }

    @Override
    public void setDroppedFlagGUID(ObjectGuid guid) {
        setDroppedFlagGUID(guid, -1);
    }
}
