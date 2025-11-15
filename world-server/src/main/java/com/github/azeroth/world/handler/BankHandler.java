package com.github.azeroth.world.handler;

public class BankHandler {

    void HandleAutoBankItemOpcode(WorldPackets::Bank::AutoBankItem& packet)
    {
        TC_LOG_DEBUG("network", "STORAGE: receive bag = {}, slot = {}", packet.Bag, packet.Slot);

        if (!CanUseBank())
        {
            TC_LOG_ERROR("network", "WORLD: HandleAutoBankItemOpcode - Unit ({}) not found or you can't interact with him.", _player->PlayerTalkClass->GetInteractionData().SourceGuid.ToString());
            return;
        }

        if (packet.BankType != BankType::Character)
            return;

        Item* item = _player->GetItemByPos(packet.Bag, packet.Slot);
        if (!item)
            return;

        ItemPosCountVec dest;
        InventoryResult msg = _player->CanBankItem(NULL_BAG, NULL_SLOT, dest, item, false);
        if (msg != EQUIP_ERR_OK)
        {
            _player->SendEquipError(msg, item, nullptr);
            return;
        }

        if (dest.size() == 1 && dest[0].pos == item->GetPos())
        {
            _player->SendEquipError(EQUIP_ERR_CANT_SWAP, item, nullptr);
            return;
        }

        _player->RemoveItem(packet.Bag, packet.Slot, true);
        _player->ItemRemovedQuestCheck(item->GetEntry(), item->GetCount());
        _player->BankItem(dest, item, true);
    }

    void HandleBankerActivateOpcode(WorldPackets::Bank::BankerActivate const& bankerActivate)
    {
        if (bankerActivate.InteractionType != PlayerInteractionType::Banker)
            return;

        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(bankerActivate.Banker, UNIT_NPC_FLAG_ACCOUNT_BANKER | UNIT_NPC_FLAG_BANKER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_ERROR("network", "WORLD: HandleBankerActivateOpcode - {} not found or you can not interact with him.", bankerActivate.Banker);
            return;
        }

        switch (bankerActivate.InteractionType)
        {
            case PlayerInteractionType::Banker:
                if (!unit->HasNpcFlag(UNIT_NPC_FLAG_ACCOUNT_BANKER | UNIT_NPC_FLAG_BANKER)) // Classic only - the banker check has been adjusted
                    return;
                break;
            case PlayerInteractionType::CharacterBanker:
                if (!unit->HasNpcFlag(UNIT_NPC_FLAG_BANKER))
                    return;
                break;
            case PlayerInteractionType::AccountBanker:
                if (!unit->HasNpcFlag(UNIT_NPC_FLAG_ACCOUNT_BANKER))
                    return;
                break;
            default:
                break;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        // set currentBankerGUID for other bank action

        SendShowBank(bankerActivate.Banker, bankerActivate.InteractionType);
    }

    void HandleAutoStoreBankItemOpcode(WorldPackets::Bank::AutoStoreBankItem& packet)
    {
        TC_LOG_DEBUG("network", "STORAGE: receive bag = {}, slot = {}", packet.Bag, packet.Slot);

        if (!CanUseBank())
        {
            TC_LOG_ERROR("network", "WORLD: HandleAutoStoreBankItemOpcode - Unit ({}) not found or you can't interact with him.", _player->PlayerTalkClass->GetInteractionData().SourceGuid.ToString());
            return;
        }

        Item* item = _player->GetItemByPos(packet.Bag, packet.Slot);
        if (!item)
            return;

        if (_player->IsBankPos(packet.Bag, packet.Slot))                    // moving from bank to inventory
        {
            ItemPosCountVec dest;
            InventoryResult msg = _player->CanStoreItem(NULL_BAG, NULL_SLOT, dest, item, false);
            if (msg != EQUIP_ERR_OK)
            {
                _player->SendEquipError(msg, item, nullptr);
                return;
            }

            _player->RemoveItem(packet.Bag, packet.Slot, true);
            if (Item const* storedItem = _player->StoreItem(dest, item, true))
            _player->ItemAddedQuestCheck(storedItem->GetEntry(), storedItem->GetCount());

        }
        else                                                                // moving from inventory to bank
        {
            ItemPosCountVec dest;
            InventoryResult msg = _player->CanBankItem(NULL_BAG, NULL_SLOT, dest, item, false);
            if (msg != EQUIP_ERR_OK)
            {
                _player->SendEquipError(msg, item, nullptr);
                return;
            }

            _player->RemoveItem(packet.Bag, packet.Slot, true);
            _player->BankItem(dest, item, true);
        }
    }

    void HandleBuyBankSlotOpcode(WorldPackets::Bank::BuyBankSlot& packet)
    {
        if (!CanUseBank(packet.Guid))
        {
            TC_LOG_ERROR("network", "WORLD: HandleBuyBankSlotOpcode - {} not found or you can't interact with him.", packet.Guid.ToString());
            return;
        }

        uint32 slot = _player->GetBankBagSlotCount();

        // next slot
        ++slot;

        TC_LOG_INFO("network", "PLAYER: Buy bank bag slot, slot number = {}", slot);

        BankBagSlotPricesEntry const* slotEntry = sBankBagSlotPricesStore.LookupEntry(slot);
        if (!slotEntry)
            return;

        uint32 price = slotEntry->Cost;

        if (!_player->HasEnoughMoney(uint64(price)))
            return;

        _player->SetBankBagSlotCount(slot);
        _player->ModifyMoney(-int64(price));

        _player->UpdateCriteria(CriteriaType::BankSlotsPurchased);
    }

    void SendShowBank(ObjectGuid guid, PlayerInteractionType interactionType)
    {
        _player->PlayerTalkClass->GetInteractionData().Reset();
        _player->PlayerTalkClass->GetInteractionData().SourceGuid = guid;
        WorldPackets::NPC::NPCInteractionOpenResult npcInteraction;
        npcInteraction.Npc = guid;
        npcInteraction.InteractionType = interactionType;
        npcInteraction.Success = true;
        SendPacket(npcInteraction.Write());
    }

}
