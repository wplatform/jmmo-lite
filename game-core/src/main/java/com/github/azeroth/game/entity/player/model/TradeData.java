package com.github.azeroth.game.entity.player.model;


import com.github.azeroth.defines.TradeStatus;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.enums.InventoryResult;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.enums.TradeSlots;
import com.github.azeroth.game.networking.packet.trade.TradeStatusPkt;
import com.github.azeroth.utils.RandomUtil;

import java.util.Arrays;
import java.util.Objects;

public class TradeData {
    private final Player player;
    private final Player trader;
    private final ObjectGuid[] items = new ObjectGuid[TradeSlots.COUNT.value];
    private boolean accepted;
    private boolean acceptProccess;

    private long money;

    private int spell;
    private ObjectGuid spellCastItem = ObjectGuid.EMPTY;

    private int clientStateIndex;

    private int serverStateIndex;

    public TradeData(Player player, Player trader) {
        this.player = player;
        this.trader = trader;
        clientStateIndex = 1;
        serverStateIndex = 1;
    }

    public final TradeData getTraderData() {
        return trader.getTradeData();
    }

    public final Item getItem(TradeSlots slot) {
        ObjectGuid objectGuid = slot.value < TradeSlots.COUNT.value ? items[slot.value] : null;
        return objectGuid != null && !objectGuid.isEmpty() ? player.getItemByGuid(objectGuid) : null;
    }

    public final boolean hasItem(ObjectGuid itemGuid) {
        return Arrays.asList(items).contains(itemGuid);
    }

    public final TradeSlots getTradeSlotForItem(ObjectGuid itemGuid) {
        for (int i = 0; i < TradeSlots.TRADED_COUNT.value; i++) {
            if (Objects.equals(items[i], itemGuid)) {
                return TradeSlots.values()[i];
            }
        }
        return TradeSlots.INVALID;
    }

    public final Item getSpellCastItem() {
        return !spellCastItem.isEmpty() ? player.getItemByGuid(spellCastItem) : null;
    }


    public final void setItem(TradeSlots slot, Item item) {
        setItem(slot, item, false);
    }


    public final void setItem(TradeSlots slot, Item item, boolean update) {
        var itemGuid = item != null ? item.getGUID() : ObjectGuid.EMPTY;

        if (items[slot.value] == itemGuid && !update) {
            return;
        }

        items[slot.value] = itemGuid;

        setAccepted(false);
        getTraderData().setAccepted(false);

        updateServerStateIndex();

        update();

        // need remove possible trader spell applied to changed item
        if (slot == TradeSlots.NON_TRADED) {
            getTraderData().setSpell(0);
        }

        // need remove possible player spell applied (possible move reagent)
        setSpell(0);
    }


    public final int getSpell() {
        return spell;
    }


    public final void setSpell(int spell_id) {
        setSpell(spell_id, null);
    }


    public final void setSpell(int spell_id, Item castItem) {
        var itemGuid = castItem != null ? castItem.getGUID() : ObjectGuid.EMPTY;

        if (spell == spell_id && Objects.equals(spellCastItem, itemGuid)) {
            return;
        }

        spell = spell_id;
        spellCastItem = itemGuid;

        setAccepted(false);
        getTraderData().setAccepted(false);

        updateServerStateIndex();

        update(true); // send spell info to item owner
        update(false); // send spell info to caster self
    }

    public final void setAccepted(boolean state, boolean crosssend) {
        accepted = state;

        if (!state) {
            TradeStatusPkt info = new TradeStatusPkt();
            info.status = TradeStatus.UNACCEPTED;

            if (crosssend) {
                trader.getSession().sendTradeStatus(info);
            } else {
                player.getSession().sendTradeStatus(info);
            }
        }
    }

    public final Player getTrader() {
        return trader;
    }

    public final boolean hasSpellCastItem() {
        return !spellCastItem.isEmpty();
    }

    public final long getMoney() {
        return money;
    }

    public final void setMoney(long money) {
        if (this.money == money) {
            return;
        }

        if (!player.hasEnoughMoney(money)) {
            TradeStatusPkt info = new TradeStatusPkt();
            info.status = TradeStatus.FAILED;
            info.bagResult = InventoryResult.NOT_ENOUGH_MONEY;
            player.getSession().sendTradeStatus(info);

            return;
        }

        this.money = money;

        setAccepted(false);
        getTraderData().setAccepted(false);

        updateServerStateIndex();

        update(true);
    }

    public final boolean isAccepted() {
        return accepted;
    }

    public final void setAccepted(boolean state) {
        setAccepted(state, false);
    }

    public final boolean isInAcceptProcess() {
        return acceptProccess;
    }

    public final void setInAcceptProcess(boolean state) {
        acceptProccess = state;
    }


    public final int getClientStateIndex() {
        return clientStateIndex;
    }

    public final void updateClientStateIndex() {
        ++clientStateIndex;
    }


    public final int getServerStateIndex() {
        return serverStateIndex;
    }

    public final void updateServerStateIndex() {
        serverStateIndex = RandomUtil.randomInt();
    }


    private void update() {
        update(true);
    }


    private void update(boolean forTarget) {
        if (forTarget) {
            trader.getSession().sendUpdateTrade(true); // player state for trader
        } else {
            player.getSession().sendUpdateTrade(false); // player state for player
        }
    }
}
