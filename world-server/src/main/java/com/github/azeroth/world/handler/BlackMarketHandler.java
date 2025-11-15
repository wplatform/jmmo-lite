package com.github.azeroth.world.handler;

public class BlackMarketHandler {

    void HandleBlackMarketOpen(WorldPackets::BlackMarket::BlackMarketOpen& blackMarketOpen)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(blackMarketOpen.Guid, UNIT_NPC_FLAG_BLACK_MARKET, UNIT_NPC_FLAG_2_BLACK_MARKET_VIEW);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleBlackMarketHello - Unit (GUID: {}) not found or you can't interact with him.", blackMarketOpen.Guid.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        SendBlackMarketOpenResult(blackMarketOpen.Guid, unit);
    }

    void SendBlackMarketOpenResult(ObjectGuid guid, Creature* /*auctioneer*/)
    {
        WorldPackets::NPC::NPCInteractionOpenResult npcInteraction;
        npcInteraction.Npc = guid;
        npcInteraction.InteractionType = PlayerInteractionType::BlackMarketAuctioneer;
        npcInteraction.Success = sBlackMarketMgr->IsEnabled();
        SendPacket(npcInteraction.Write());
    }

    void SendBlackMarketBidOnItemResult(int32 result, int32 marketId, WorldPackets::Item::ItemInstance& item)
    {
        WorldPackets::BlackMarket::BlackMarketBidOnItemResult packet;

        packet.MarketID = marketId;
        packet.Item = item;
        packet.Result = result;

        SendPacket(packet.Write());
    }

    void SendBlackMarketWonNotification(BlackMarketEntry const* entry, Item const* item)
    {
        WorldPackets::BlackMarket::BlackMarketWon packet;

        packet.MarketID = entry->GetMarketId();
        packet.Item.Initialize(item);

        SendPacket(packet.Write());
    }

    void SendBlackMarketOutbidNotification(BlackMarketTemplate const* templ)
    {
        WorldPackets::BlackMarket::BlackMarketOutbid packet;

        packet.MarketID = templ->MarketID;
        packet.Item = templ->Item;
        packet.RandomPropertiesID = 0;

        SendPacket(packet.Write());
    }

}
