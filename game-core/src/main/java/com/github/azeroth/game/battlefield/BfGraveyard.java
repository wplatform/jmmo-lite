package com.github.azeroth.game.battlefield;



import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.Objects;

public class BfGraveyard {
    private final ObjectGuid[] m_SpiritGuide = new ObjectGuid[SharedConst.PvpTeamsCount];
    private final ArrayList<ObjectGuid> m_ResurrectQueue = new ArrayList<>();
    protected BattleField m_Bf;
    private int m_ControlTeam;
    private int m_GraveyardId;

    public BfGraveyard(BattleField battlefield) {
        m_Bf = battlefield;
        m_GraveyardId = 0;
        m_ControlTeam = TeamIds.Neutral;
        m_SpiritGuide[0] = ObjectGuid.Empty;
        m_SpiritGuide[1] = ObjectGuid.Empty;
    }

    public final void initialize(int startControl, int graveyardId) {
        m_ControlTeam = startControl;
        m_GraveyardId = graveyardId;
    }

    public final void setSpirit(Creature spirit, int teamIndex) {
        if (!spirit) {
            Log.outError(LogFilter.Battlefield, "BfGraveyard:SetSpirit: Invalid Spirit.");

            return;
        }

        m_SpiritGuide[teamIndex] = spirit.getGUID();
        spirit.setReactState(ReactStates.Passive);
    }

    public final float getDistance(Player player) {
        var safeLoc = global.getObjectMgr().getWorldSafeLoc(m_GraveyardId);

        return player.getDistance2d(safeLoc.loc.getX(), safeLoc.loc.getY());
    }

    public final void addPlayer(ObjectGuid playerGuid) {
        if (!m_ResurrectQueue.contains(playerGuid)) {
            m_ResurrectQueue.add(playerGuid);
            var player = global.getObjAccessor().findPlayer(playerGuid);

            if (player) {
                player.castSpell(player, BattlegroundConst.SpellWaitingForResurrect, true);
            }
        }
    }

    public final void removePlayer(ObjectGuid playerGuid) {
        m_ResurrectQueue.remove(playerGuid);

        var player = global.getObjAccessor().findPlayer(playerGuid);

        if (player) {
            player.removeAura(BattlegroundConst.SpellWaitingForResurrect);
        }
    }

    public final void resurrect() {
        if (m_ResurrectQueue.isEmpty()) {
            return;
        }

        for (var guid : m_ResurrectQueue) {
            // Get player object from his guid
            var player = global.getObjAccessor().findPlayer(guid);

            if (!player) {
                continue;
            }

            // Check  if the player is in world and on the good graveyard
            if (player.isInWorld()) {
                var spirit = m_Bf.getCreature(m_SpiritGuide[m_ControlTeam]);

                if (spirit) {
                    spirit.castSpell(spirit, BattlegroundConst.SpellSpiritHeal, true);
                }
            }

            // Resurect player
            player.castSpell(player, BattlegroundConst.SpellResurrectionVisual, true);
            player.resurrectPlayer(1.0f);
            player.castSpell(player, 6962, true);
            player.castSpell(player, BattlegroundConst.SpellSpiritHealMana, true);

            player.spawnCorpseBones(false);
        }

        m_ResurrectQueue.clear();
    }

    // For changing graveyard control
    public final void giveControlTo(int team) {
        // Guide switching
        // Note: Visiblity changes are made by phasing
		/*if (m_SpiritGuide[1 - team])
			m_SpiritGuide[1 - team].setVisible(false);
		if (m_SpiritGuide[team])
			m_SpiritGuide[team].setVisible(true);*/

        m_ControlTeam = team;
        // Teleport to other graveyard, player witch were on this graveyard
        relocateDeadPlayers();
    }

    public final boolean hasNpc(ObjectGuid guid) {
        if (m_SpiritGuide[TeamId.ALLIANCE].isEmpty() || m_SpiritGuide[TeamId.HORDE].isEmpty()) {
            return false;
        }

        if (!m_Bf.getCreature(m_SpiritGuide[TeamId.ALLIANCE]) || !m_Bf.getCreature(m_SpiritGuide[TeamId.HORDE])) {
            return false;
        }

        return (Objects.equals(m_SpiritGuide[TeamId.ALLIANCE], guid) || Objects.equals(m_SpiritGuide[TeamId.HORDE], guid));
    }

    // Check if a player is in this graveyard's ressurect queue
    public final boolean hasPlayer(ObjectGuid guid) {
        return m_ResurrectQueue.contains(guid);
    }

    // Get the graveyard's ID.
    public final int getGraveyardId() {
        return m_GraveyardId;
    }

    public final int getControlTeamId() {
        return m_ControlTeam;
    }

    private void relocateDeadPlayers() {
        WorldSafeLocsEntry closestGrave = null;

        for (var guid : m_ResurrectQueue) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (!player) {
                continue;
            }

            if (closestGrave != null) {
                player.teleportTo(closestGrave.loc);
            } else {
                closestGrave = m_Bf.getClosestGraveYard(player);

                if (closestGrave != null) {
                    player.teleportTo(closestGrave.loc);
                }
            }
        }
    }
}
