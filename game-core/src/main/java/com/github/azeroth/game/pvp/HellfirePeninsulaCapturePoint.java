package com.github.azeroth.game.pvp;


import com.github.azeroth.game.entity.gobject.GameObject;

class HellfirePeninsulaCapturePoint extends OPvPCapturePoint {

    private final int m_TowerType;

    private final long m_flagSpawnId;


    public HellfirePeninsulaCapturePoint(OutdoorPvP pvp, OutdoorPvPHPTowerType type, GameObject go, long flagSpawnId) {
        super(pvp);
        m_TowerType = (int) type.getValue();
        m_flagSpawnId = flagSpawnId;

        m_capturePointSpawnId = go.getSpawnId();
        m_capturePoint = go;
        setCapturePointData(go.getEntry());
    }

    @Override
    public void changeState() {
        int field = 0;

        switch (getOldState()) {
            case Neutral:
                field = HPConst.Map_N[m_TowerType];

                break;
            case Alliance:
                field = HPConst.Map_A[m_TowerType];
                var alliance_towers = ((HellfirePeninsulaPvP) getPvP()).getAllianceTowersControlled();

                if (alliance_towers != 0) {
                    ((HellfirePeninsulaPvP) getPvP()).setAllianceTowersControlled(--alliance_towers);
                }

                break;
            case Horde:
                field = HPConst.Map_H[m_TowerType];
                var horde_towers = ((HellfirePeninsulaPvP) getPvP()).getHordeTowersControlled();

                if (horde_towers != 0) {
                    ((HellfirePeninsulaPvP) getPvP()).setHordeTowersControlled(--horde_towers);
                }

                break;
            case NeutralAllianceChallenge:
                field = HPConst.Map_N[m_TowerType];

                break;
            case NeutralHordeChallenge:
                field = HPConst.Map_N[m_TowerType];

                break;
            case AllianceHordeChallenge:
                field = HPConst.Map_A[m_TowerType];

                break;
            case HordeAllianceChallenge:
                field = HPConst.Map_H[m_TowerType];

                break;
        }

        // send world state update
        if (field != 0) {
            getPvP().setWorldState((int) field, 0);
            field = 0;
        }

        int artkit = 21;
        var artkit2 = HPConst.TowerArtKit_N[m_TowerType];

        switch (getState()) {
            case Neutral:
                field = HPConst.Map_N[m_TowerType];

                break;
            case Alliance: {
                field = HPConst.Map_A[m_TowerType];
                artkit = 2;
                artkit2 = HPConst.TowerArtKit_A[m_TowerType];
                var alliance_towers = ((HellfirePeninsulaPvP) getPvP()).getAllianceTowersControlled();

                if (alliance_towers < 3) {
                    ((HellfirePeninsulaPvP) getPvP()).setAllianceTowersControlled(++alliance_towers);
                }

                getPvP().sendDefenseMessage(HPConst.BuffZones[0], HPConst.LangCapture_A[m_TowerType]);

                break;
            }
            case Horde: {
                field = HPConst.Map_H[m_TowerType];
                artkit = 1;
                artkit2 = HPConst.TowerArtKit_H[m_TowerType];
                var horde_towers = ((HellfirePeninsulaPvP) getPvP()).getHordeTowersControlled();

                if (horde_towers < 3) {
                    ((HellfirePeninsulaPvP) getPvP()).setHordeTowersControlled(++horde_towers);
                }

                getPvP().sendDefenseMessage(HPConst.BuffZones[0], HPConst.LangCapture_H[m_TowerType]);

                break;
            }
            case NeutralAllianceChallenge:
                field = HPConst.Map_N[m_TowerType];

                break;
            case NeutralHordeChallenge:
                field = HPConst.Map_N[m_TowerType];

                break;
            case AllianceHordeChallenge:
                field = HPConst.Map_A[m_TowerType];
                artkit = 2;
                artkit2 = HPConst.TowerArtKit_A[m_TowerType];

                break;
            case HordeAllianceChallenge:
                field = HPConst.Map_H[m_TowerType];
                artkit = 1;
                artkit2 = HPConst.TowerArtKit_H[m_TowerType];

                break;
        }

        var map = global.getMapMgr().findMap(530, 0);
        var bounds = map.getGameObjectBySpawnIdStore().get(m_capturePointSpawnId);

        for (var go : bounds) {
            go.GoArtKit = artkit;
        }

        bounds = map.getGameObjectBySpawnIdStore().get(m_flagSpawnId);

        for (var go : bounds) {
            go.GoArtKit = artkit2;
        }

        // send world state update
        if (field != 0) {
            getPvP().setWorldState((int) field, 1);
        }

        // complete quest objective
        if (getState() == ObjectiveStates.Alliance || getState() == ObjectiveStates.Horde) {
            sendObjectiveComplete(HPConst.CreditMarker[m_TowerType], ObjectGuid.Empty);
        }
    }
}
