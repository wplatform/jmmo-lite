package com.github.azeroth.game.map.grid.visitor;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.NotifyFlag;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.gobject.Transport;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.object.update.UpdateData;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class VisibleNotifier implements GridVisitor {
    private final Player player;
    private final UpdateData data;
    private final ArrayList<ObjectGuid> visGuids;
    private final ArrayList<Unit> visibleNow;

    public VisibleNotifier(Player pl) {
        player = (pl);
        data = new UpdateData(pl.getLocation().getMapId());
        visGuids = new ArrayList<>(pl.getClientGuiDs());
        visibleNow = new ArrayList<>();
    }


    public GridVisitorResult visit(WorldObject obj) {
        visGuids.remove(obj.getGUID());
        player.updateVisibilityOf(obj, data, visibleNow);
        return GridVisitorResult.CONTINUE;
    }

    public final void sendToSelf() {
        // at this moment i_clientGUIDs have guids that not iterate at grid level checks
        // but exist one case when this possible and object not out of range: transports
        var transport = (Transport) player.getTransport();

        if (transport != null) {
            for (var obj : transport.getPassengers()) {
                if (visGuids.contains(obj.getGUID())) {
                    visGuids.remove(obj.getGUID());

                    switch (obj.getObjectTypeId()) {
                        case TypeId.GAME_OBJECT:
                            player.updateVisibilityOf(obj.toGameObject(), data, visibleNow);

                            break;
                        case TypeId.PLAYER:
                            player.updateVisibilityOf(obj.toPlayer(), data, visibleNow);

                            if (!obj.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED)) {
                                obj.toPlayer().updateVisibilityOf(player);
                            }
                            break;
                        case TypeId.UNIT:
                            player.updateVisibilityOf(obj.toCreature(), data, visibleNow);

                            break;
                        case TypeId.DYNAMIC_OBJECT:
                            player.updateVisibilityOf(obj.toDynObject(), data, visibleNow);

                            break;
                        case TypeId.AREA_TRIGGER:
                            player.updateVisibilityOf(obj.toAreaTrigger(), data, visibleNow);

                            break;
                        default:
                            break;
                    }
                }
            }
        }

        for (var guid : visGuids) {
            player.getClientGuiDs().remove(guid);
            data.addOutOfRangeGUID(guid);

            if (guid.isPlayer()) {
                var pl = player.getWorldContext().findPlayer(guid);

                if (pl != null && pl.isInWorld() && !pl.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED)) {
                    pl.updateVisibilityOf(player);
                }
            }
        }

        if (!data.hasData()) {
            return;
        }
        player.sendPacket(data.buildPacket());

        for (var obj : visibleNow) {
            player.sendInitialVisiblePackets(obj);
        }
    }


}
