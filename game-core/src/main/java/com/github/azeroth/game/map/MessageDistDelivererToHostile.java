package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.interfaces.*;


public class MessageDistDelivererToHostile<T extends IDoWork<Player>> implements IGridNotifierPlayer, IGridNotifierDynamicObject, IGridNotifierCreature {
    private final Unit source;
    private final T packetSender;
    private final PhaseShift phaseShift;
    private final float distSq;

    public MessageDistDelivererToHostile(Unit src, T packetSender, float dist, GridType gridType) {
        source = src;
        packetSender = packetSender;
        phaseShift = src.getPhaseShift();
        distSq = dist * dist;
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (!creature.inSamePhase(phaseShift)) {
                continue;
            }

            if (creature.getLocation().getExactDist2DSq(source.getLocation()) > distSq) {
                continue;
            }

            // Send packet to all who are sharing the creature's vision
            if (creature.getHasSharedVision()) {
                for (var player : creature.getSharedVisionList()) {
                    if (player.getSeerView() == creature) {
                        sendPacket(player);
                    }
                }
            }
        }
    }

    public final void visit(list<DynamicObject> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var dynamicObject = objs.get(i);

            if (!dynamicObject.inSamePhase(phaseShift)) {
                continue;
            }

            if (dynamicObject.getLocation().getExactDist2DSq(source.getLocation()) > distSq) {
                continue;
            }

            var caster = dynamicObject.getCaster();

            if (caster != null) {
                // Send packet back to the caster if the caster has vision of dynamic object
                var player = caster.toPlayer();

                if (player && player.getSeerView() == dynamicObject) {
                    sendPacket(player);
                }
            }
        }
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (!player.inSamePhase(phaseShift)) {
                continue;
            }

            if (player.getLocation().getExactDist2DSq(source.getLocation()) > distSq) {
                continue;
            }

            // Send packet to all who are sharing the player's vision
            if (player.getHasSharedVision()) {
                for (var visionPlayer : player.getSharedVisionList()) {
                    if (visionPlayer.getSeerView() == player) {
                        sendPacket(visionPlayer);
                    }
                }
            }

            if (player.getSeerView() == player || player.getVehicle1()) {
                sendPacket(player);
            }
        }
    }

    private void sendPacket(Player player) {
        // never send packet to self
        if (player == source || !player.haveAtClient(source) || player.isFriendlyTo(source)) {
            return;
        }

        packetSender.invoke(player);
    }


}
