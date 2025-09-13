package com.github.azeroth.game.pvp;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.networking.packet.InitWorldStates;

class HellfirePeninsulaPvP extends OutdoorPvP {
    private final long[] m_towerFlagSpawnIds = new long[OutdoorPvPHPTowerType.Num.getValue()];

    // how many towers are controlled
    private int m_AllianceTowersControlled;
    private int m_HordeTowersControlled;

    public HellfirePeninsulaPvP(Map map) {
        super(map);
        m_TypeId = OutdoorPvPTypes.HellfirePeninsula;
        m_AllianceTowersControlled = 0;
        m_HordeTowersControlled = 0;
    }

    @Override
    public boolean setupOutdoorPvP() {
        m_AllianceTowersControlled = 0;
        m_HordeTowersControlled = 0;

        // add the zones affected by the pvp buff
        for (var i = 0; i < HPConst.BUFFZONES.length; ++i) {
            registerZone(HPConst.BuffZones[i]);
        }

        return true;
    }

    @Override
    public void onGameObjectCreate(GameObject go) {
        switch (go.getEntry()) {
            case 182175:
                addCapturePoint(new HellfirePeninsulaCapturePoint(this, OutdoorPvPHPTowerType.BrokenHill, go, m_towerFlagSpawnIds[OutdoorPvPHPTowerType.BrokenHill.getValue()]));

                break;
            case 182174:
                addCapturePoint(new HellfirePeninsulaCapturePoint(this, OutdoorPvPHPTowerType.Overlook, go, m_towerFlagSpawnIds[OutdoorPvPHPTowerType.Overlook.getValue()]));

                break;
            case 182173:
                addCapturePoint(new HellfirePeninsulaCapturePoint(this, OutdoorPvPHPTowerType.Stadium, go, m_towerFlagSpawnIds[OutdoorPvPHPTowerType.Stadium.getValue()]));

                break;
            case 183514:
                m_towerFlagSpawnIds[OutdoorPvPHPTowerType.BrokenHill.getValue()] = go.getSpawnId();

                break;
            case 182525:
                m_towerFlagSpawnIds[OutdoorPvPHPTowerType.Overlook.getValue()] = go.getSpawnId();

                break;
            case 183515:
                m_towerFlagSpawnIds[OutdoorPvPHPTowerType.Stadium.getValue()] = go.getSpawnId();

                break;
            default:
                break;
        }

        super.onGameObjectCreate(go);
    }

    @Override
    public void handlePlayerEnterZone(Player player, int zone) {
        // add buffs
        if (player.getTeam() == Team.ALLIANCE) {
            if (m_AllianceTowersControlled >= 3) {
                player.castSpell(player, OutdoorPvPHPSpells.allianceBuff, true);
            }
        } else {
            if (m_HordeTowersControlled >= 3) {
                player.castSpell(player, OutdoorPvPHPSpells.hordeBuff, true);
            }
        }

        super.handlePlayerEnterZone(player, zone);
    }

    @Override
    public void handlePlayerLeaveZone(Player player, int zone) {
        // remove buffs
        if (player.getTeam() == Team.ALLIANCE) {
            player.removeAura(OutdoorPvPHPSpells.allianceBuff);
        } else {
            player.removeAura(OutdoorPvPHPSpells.hordeBuff);
        }

        super.handlePlayerLeaveZone(player, zone);
    }

    @Override
    public boolean update(int diff) {
        var changed = super.update(diff);

        if (changed) {
            if (m_AllianceTowersControlled == 3) {
                teamApplyBuff(TeamId.ALLIANCE, OutdoorPvPHPSpells.allianceBuff, OutdoorPvPHPSpells.hordeBuff);
            } else if (m_HordeTowersControlled == 3) {
                teamApplyBuff(TeamId.HORDE, OutdoorPvPHPSpells.hordeBuff, OutdoorPvPHPSpells.allianceBuff);
            } else {
                teamCastSpell(TeamId.ALLIANCE, -(int) OutdoorPvPHPSpells.allianceBuff);
                teamCastSpell(TeamId.HORDE, -(int) OutdoorPvPHPSpells.hordeBuff);
            }

            setWorldState(OutdoorPvPHPWorldStates.COUNT_A, (int) m_AllianceTowersControlled);
            setWorldState(OutdoorPvPHPWorldStates.COUNT_H, (int) m_HordeTowersControlled);
        }

        return changed;
    }

    @Override
    public void sendRemoveWorldStates(Player player) {
        InitWorldStates initWorldStates = new InitWorldStates();
        initWorldStates.mapID = player.getLocation().getMapId();
        initWorldStates.areaID = player.getZoneId();
        initWorldStates.subareaID = player.getAreaId();
        initWorldStates.addState(OutdoorPvPHPWorldStates.DISPLAY_A, 0);
        initWorldStates.addState(OutdoorPvPHPWorldStates.DISPLAY_H, 0);
        initWorldStates.addState(OutdoorPvPHPWorldStates.COUNT_H, 0);
        initWorldStates.addState(OutdoorPvPHPWorldStates.COUNT_A, 0);

        for (var i = 0; i < OutdoorPvPHPTowerType.Num.getValue(); ++i) {
            initWorldStates.addState(HPConst.Map_N[i], 0);
            initWorldStates.addState(HPConst.Map_A[i], 0);
            initWorldStates.addState(HPConst.Map_H[i], 0);
        }

        player.sendPacket(initWorldStates);
    }

    @Override
    public void handleKillImpl(Player killer, Unit killed) {
        if (!killed.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (killer.getTeam() == Team.ALLIANCE && killed.toPlayer().getTeam() != Team.ALLIANCE) {
            killer.castSpell(killer, OutdoorPvPHPSpells.alliancePlayerKillReward, true);
        } else if (killer.getTeam() == Team.Horde && killed.toPlayer().getTeam() != Team.Horde) {
            killer.castSpell(killer, OutdoorPvPHPSpells.hordePlayerKillReward, true);
        }
    }

    public final int getAllianceTowersControlled() {
        return m_AllianceTowersControlled;
    }

    public final void setAllianceTowersControlled(int count) {
        m_AllianceTowersControlled = count;
    }

    public final int getHordeTowersControlled() {
        return m_HordeTowersControlled;
    }

    public final void setHordeTowersControlled(int count) {
        m_HordeTowersControlled = count;
    }
}
