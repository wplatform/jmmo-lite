package com.github.azeroth.game.battleground.zones;


final class EotSMisc {

    public static final int eventStartBattle = 13180; // Achievement: Flurry
    public static final int FLAGRESPAWNTIME = (8 * time.InMilliseconds);
    public static final int FPointsTickTime = (2 * time.InMilliseconds);


    public static final int notEYWeekendHonorTicks = 260;

    public static final int EYWeekendHonorTicks = 160;


    public static final int objectiveCaptureFlag = 183;


    public static final int spellNetherstormFlag = 34976;

    public static final int spellPlayerDroppedFlag = 34991;


    public static final int exploitTeleportLocationAlliance = 3773;

    public static final int exploitTeleportLocationHorde = 3772;

    public static Position[] TRIGGERPOSITIONS =
            {
                    new Position(2044.28f, 1729.68f, 1189.96f, 0.017453f),
                    new Position(2048.83f, 1393.65f, 1194.49f, 0.20944f),
                    new Position(2286.56f, 1402.36f, 1197.11f, 3.72381f),
                    new Position(2284.48f, 1731.23f, 1189.99f, 2.89725f)
            };


    public static byte[] TICKPOINTS = {1, 2, 5, 10};


    public static int[] FLAGPOINTS = {75, 85, 100, 500};

    public static BattlegroundEYPointIconsStruct[] m_PointsIconStruct =
            {
                    new BattlegroundEYPointIconsStruct(EotSWorldStateIds.felReaverUncontrol, EotSWorldStateIds.felReaverAllianceControl, EotSWorldStateIds.felReaverHordeControl, EotSWorldStateIds.felReaverAllianceControlState, EotSWorldStateIds.felReaverHordeControlState),
                    new BattlegroundEYPointIconsStruct(EotSWorldStateIds.bloodElfUncontrol, EotSWorldStateIds.bloodElfAllianceControl, EotSWorldStateIds.bloodElfHordeControl, EotSWorldStateIds.bloodElfAllianceControlState, EotSWorldStateIds.bloodElfHordeControlState),
                    new BattlegroundEYPointIconsStruct(EotSWorldStateIds.draeneiRuinsUncontrol, EotSWorldStateIds.draeneiRuinsAllianceControl, EotSWorldStateIds.draeneiRuinsHordeControl, EotSWorldStateIds.draeneiRuinsAllianceControlState, EotSWorldStateIds.draeneiRuinsHordeControlState),
                    new BattlegroundEYPointIconsStruct(EotSWorldStateIds.mageTowerUncontrol, EotSWorldStateIds.mageTowerAllianceControl, EotSWorldStateIds.mageTowerHordeControl, EotSWorldStateIds.mageTowerAllianceControlState, EotSWorldStateIds.mageTowerHordeControlState)
            };

    public static BattlegroundEYLosingPointStruct[] m_LosingPointTypes =
            {
                    new BattlegroundEYLosingPointStruct(EotSObjectTypes.NBannerFelReaverCenter, EotSObjectTypes.ABannerFelReaverCenter, EotSBroadcastTexts.allianceLostFelReaverRuins, EotSObjectTypes.HBannerFelReaverCenter, EotSBroadcastTexts.hordeLostFelReaverRuins),
                    new BattlegroundEYLosingPointStruct(EotSObjectTypes.NBannerBloodElfCenter, EotSObjectTypes.ABannerBloodElfCenter, EotSBroadcastTexts.allianceLostBloodElfTower, EotSObjectTypes.HBannerBloodElfCenter, EotSBroadcastTexts.hordeLostBloodElfTower),
                    new BattlegroundEYLosingPointStruct(EotSObjectTypes.NBannerDraeneiRuinsCenter, EotSObjectTypes.ABannerDraeneiRuinsCenter, EotSBroadcastTexts.allianceLostDraeneiRuins, EotSObjectTypes.HBannerDraeneiRuinsCenter, EotSBroadcastTexts.hordeLostDraeneiRuins),
                    new BattlegroundEYLosingPointStruct(EotSObjectTypes.NBannerMageTowerCenter, EotSObjectTypes.ABannerMageTowerCenter, EotSBroadcastTexts.allianceLostMageTower, EotSObjectTypes.HBannerMageTowerCenter, EotSBroadcastTexts.hordeLostMageTower)
            };

    public static BattlegroundEYCapturingPointStruct[] m_CapturingPointTypes =
            {
                    new BattlegroundEYCapturingPointStruct(EotSObjectTypes.NBannerFelReaverCenter, EotSObjectTypes.ABannerFelReaverCenter, EotSBroadcastTexts.allianceTakenFelReaverRuins, EotSObjectTypes.HBannerFelReaverCenter, EotSBroadcastTexts.hordeTakenFelReaverRuins, EotSGaveyardIds.FELREAVER),
                    new BattlegroundEYCapturingPointStruct(EotSObjectTypes.NBannerBloodElfCenter, EotSObjectTypes.ABannerBloodElfCenter, EotSBroadcastTexts.allianceTakenBloodElfTower, EotSObjectTypes.HBannerBloodElfCenter, EotSBroadcastTexts.hordeTakenBloodElfTower, EotSGaveyardIds.BLOODELF),
                    new BattlegroundEYCapturingPointStruct(EotSObjectTypes.NBannerDraeneiRuinsCenter, EotSObjectTypes.ABannerDraeneiRuinsCenter, EotSBroadcastTexts.allianceTakenDraeneiRuins, EotSObjectTypes.HBannerDraeneiRuinsCenter, EotSBroadcastTexts.hordeTakenDraeneiRuins, EotSGaveyardIds.DRAENEIRUINS),
                    new BattlegroundEYCapturingPointStruct(EotSObjectTypes.NBannerMageTowerCenter, EotSObjectTypes.ABannerMageTowerCenter, EotSBroadcastTexts.allianceTakenMageTower, EotSObjectTypes.HBannerMageTowerCenter, EotSBroadcastTexts.hordeTakenMageTower, EotSGaveyardIds.MAGETOWER)
            };
}
