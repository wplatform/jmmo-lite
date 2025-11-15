package com.github.azeroth.world.handler;

public class NPCHandler {


    enum class TabardVendorType : int32
    {
        Guild       = 0,
                Personal    = 1,
    };

    void HandleTabardVendorActivateOpcode(WorldPackets::NPC::TabardVendorActivate const& tabardVendorActivate)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(tabardVendorActivate.Vendor, UNIT_NPC_FLAG_TABARDDESIGNER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleTabardVendorActivateOpcode - {} not found or you can not interact with him.", tabardVendorActivate.Vendor.ToString());
            return;
        }

        TabardVendorType type = TabardVendorType(tabardVendorActivate.Type);
        if (type != TabardVendorType::Guild && type != TabardVendorType::Personal)
            return;

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        SendTabardVendorActivate(tabardVendorActivate.Vendor, TabardVendorType(tabardVendorActivate.Type));
    }

    void SendTabardVendorActivate(ObjectGuid guid, TabardVendorType type)
    {
        WorldPackets::NPC::NPCInteractionOpenResult npcInteraction;
        npcInteraction.Npc = guid;
        npcInteraction.InteractionType = [&]
        {
            switch (type)
            {
                case TabardVendorType::Guild:
                    return PlayerInteractionType::GuildTabardVendor;
                case TabardVendorType::Personal:
                    return PlayerInteractionType::PersonalTabardVendor;
                default:
                    ABORT_MSG("Unsupported tabard vendor type %d", AsUnderlyingType(type));
            }
        }();
        npcInteraction.Success = true;
        SendPacket(npcInteraction.Write());
    }

    void SendShowMailBox(ObjectGuid guid)
    {
        WorldPackets::NPC::NPCInteractionOpenResult npcInteraction;
        npcInteraction.Npc = guid;
        npcInteraction.InteractionType = PlayerInteractionType::MailInfo;
        npcInteraction.Success = true;
        SendPacket(npcInteraction.Write());
    }

    void HandleTrainerListOpcode(WorldPackets::NPC::Hello& packet)
    {
        Creature* npc = GetPlayer()->GetNPCIfCanInteractWith(packet.Unit, UNIT_NPC_FLAG_TRAINER, UNIT_NPC_FLAG_2_NONE);
        if (!npc)
        {
            TC_LOG_DEBUG("network", "SendTrainerList - {} not found or you can not interact with him.", packet.Unit.ToString());
            return;
        }

        if (uint32 trainerId = npc->GetTrainerId())
        SendTrainerList(npc, trainerId);
    else
        TC_LOG_DEBUG("network", "SendTrainerList - Creature id {} has no trainer data.", npc->GetEntry());
    }

    void SendTrainerList(Creature* npc, uint32 trainerId)
    {
        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        Trainer::Trainer const* trainer = sObjectMgr->GetTrainer(trainerId);
        if (!trainer)
        {
            TC_LOG_DEBUG("network", "SendTrainerList - trainer spells not found for trainer {} id {}", npc->GetGUID().ToString(), trainerId);
            return;
        }

        _player->PlayerTalkClass->GetInteractionData().Reset();
        _player->PlayerTalkClass->GetInteractionData().SourceGuid = npc->GetGUID();
        _player->PlayerTalkClass->GetInteractionData().TrainerId = trainerId;
        trainer->SendSpells(npc, _player, GetSessionDbLocaleIndex());
    }

    void HandleTrainerBuySpellOpcode(WorldPackets::NPC::TrainerBuySpell& packet)
    {
        TC_LOG_DEBUG("network", "WORLD: Received CMSG_TRAINER_BUY_SPELL {}, learn spell id is: {}", packet.TrainerGUID.ToString(), packet.SpellID);

        Creature* npc = GetPlayer()->GetNPCIfCanInteractWith(packet.TrainerGUID, UNIT_NPC_FLAG_TRAINER, UNIT_NPC_FLAG_2_NONE);
        if (!npc)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleTrainerBuySpellOpcode - {} not found or you can not interact with him.", packet.TrainerGUID.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        if (_player->PlayerTalkClass->GetInteractionData().SourceGuid != packet.TrainerGUID)
            return;

        if (_player->PlayerTalkClass->GetInteractionData().TrainerId != uint32(packet.TrainerID))
            return;

        Trainer::Trainer const* trainer = sObjectMgr->GetTrainer(packet.TrainerID);
        if (!trainer)
            return;

        trainer->TeachSpell(npc, _player, packet.SpellID);
    }

    void HandleGossipHelloOpcode(WorldPackets::NPC::Hello& packet)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(packet.Unit, UNIT_NPC_FLAG_GOSSIP, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleGossipHelloOpcode - {} not found or you can not interact with him.", packet.Unit.ToString());
            return;
        }

        // set faction visible if needed
        if (FactionTemplateEntry const* factionTemplateEntry = sFactionTemplateStore.LookupEntry(unit->GetFaction()))
        _player->GetReputationMgr().SetVisible(factionTemplateEntry);

        GetPlayer()->RemoveAurasWithInterruptFlags(SpellAuraInterruptFlags::Interacting);

        // Stop the npc if moving
        if (uint32 pause = unit->GetMovementTemplate().GetInteractionPauseTimer())
        unit->PauseMovement(pause);
        unit->SetHomePosition(unit->GetPosition());

        if (unit->IsAreaSpiritHealer())
        {
            _player->SetAreaSpiritHealer(unit);
            _player->SendAreaSpiritHealerTime(unit);
        }

        _player->PlayerTalkClass->ClearMenus();
        if (!unit->AI()->OnGossipHello(_player))
        {
//        _player->TalkedToCreature(unit->GetEntry(), unit->GetGUID());
            _player->PrepareGossipMenu(unit, _player->GetGossipMenuForSource(unit), true);
            _player->SendPreparedGossip(unit);
        }
    }

    void HandleGossipSelectOptionOpcode(WorldPackets::NPC::GossipSelectOption& packet)
    {
        GossipMenuItem const* gossipMenuItem = _player->PlayerTalkClass->GetGossipMenu().GetItem(packet.GossipOptionID);
        if (!gossipMenuItem)
            return;

        // Prevent cheating on C++ scripted menus
        if (_player->PlayerTalkClass->GetInteractionData().SourceGuid != packet.GossipUnit)
            return;

        Creature* unit = nullptr;
        GameObject* go = nullptr;
        if (packet.GossipUnit.IsCreatureOrVehicle())
        {
            unit = GetPlayer()->GetNPCIfCanInteractWith(packet.GossipUnit, UNIT_NPC_FLAG_GOSSIP, UNIT_NPC_FLAG_2_NONE);
            if (!unit)
            {
                TC_LOG_DEBUG("network", "WORLD: HandleGossipSelectOptionOpcode - {} not found or you can't interact with him.", packet.GossipUnit.ToString());
                return;
            }
        }
        else if (packet.GossipUnit.IsGameObject())
        {
            go = _player->GetGameObjectIfCanInteractWith(packet.GossipUnit);
            if (!go)
            {
                TC_LOG_DEBUG("network", "WORLD: HandleGossipSelectOptionOpcode - {} not found or you can't interact with it.", packet.GossipUnit.ToString());
                return;
            }
        }
        else
        {

            TC_LOG_DEBUG("network", "WORLD: HandleGossipSelectOptionOpcode - unsupported {}.", packet.GossipUnit.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        if ((unit && unit->GetScriptId() != unit->LastUsedScriptID) || (go && go->GetScriptId() != go->LastUsedScriptID))
        {
            TC_LOG_DEBUG("network", "WORLD: HandleGossipSelectOptionOpcode - Script reloaded while in use, ignoring and set new scipt id");
            if (unit)
                unit->LastUsedScriptID = unit->GetScriptId();

            if (go)
                go->LastUsedScriptID = go->GetScriptId();
            _player->PlayerTalkClass->SendCloseGossip();
            return;
        }

        if (!packet.PromotionCode.empty())
        {
            if (unit)
            {
                if (!unit->AI()->OnGossipSelectCode(_player, packet.GossipID, gossipMenuItem->OrderIndex, packet.PromotionCode.c_str()))
                _player->OnGossipSelect(unit, packet.GossipOptionID, packet.GossipID);
            }
            else
            {
                if (!go->AI()->OnGossipSelectCode(_player, packet.GossipID, gossipMenuItem->OrderIndex, packet.PromotionCode.c_str()))
                _player->OnGossipSelect(go, packet.GossipOptionID, packet.GossipID);
            }
        }
        else
        {
            if (unit)
            {
                if (!unit->AI()->OnGossipSelect(_player, packet.GossipID, gossipMenuItem->OrderIndex))
                _player->OnGossipSelect(unit, packet.GossipOptionID, packet.GossipID);
            }
            else
            {
                if (!go->AI()->OnGossipSelect(_player, packet.GossipID, gossipMenuItem->OrderIndex))
                _player->OnGossipSelect(go, packet.GossipOptionID, packet.GossipID);
            }
        }
    }

    void HandleSpiritHealerActivate(WorldPackets::NPC::SpiritHealerActivate& packet)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(packet.Healer, UNIT_NPC_FLAG_SPIRIT_HEALER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleSpiritHealerActivateOpcode - {} not found or you can not interact with him.", packet.Healer.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        SendSpiritResurrect();
    }

    void SendSpiritResurrect()
    {
        _player->ResurrectPlayer(0.5f, true);
        _player->DurabilityLossAll(0.25f, true);

        // get corpse nearest graveyard
        WorldSafeLocsEntry const* corpseGrave = nullptr;
        WorldLocation corpseLocation = _player->GetCorpseLocation();
        if (_player->HasCorpse())
        {
            corpseGrave = sObjectMgr->GetClosestGraveyard(corpseLocation, _player->GetTeam(), _player);
        }

        // now can spawn bones
        _player->SpawnCorpseBones();

        // teleport to nearest from corpse graveyard, if different from nearest to player ghost
        if (corpseGrave)
        {
            WorldSafeLocsEntry const* ghostGrave = sObjectMgr->GetClosestGraveyard(*_player, _player->GetTeam(), _player);

            if (corpseGrave != ghostGrave)
                _player->TeleportTo(corpseGrave->Loc);
        }
    }

    void HandleBinderActivateOpcode(WorldPackets::NPC::Hello& packet)
    {
        if (!GetPlayer()->IsInWorld() || !GetPlayer()->IsAlive())
        return;

        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(packet.Unit, UNIT_NPC_FLAG_INNKEEPER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleBinderActivateOpcode - {} not found or you can not interact with him.", packet.Unit.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        SendBindPoint(unit);
    }

    void SendBindPoint(Creature* npc)
    {
        // prevent set homebind to instances in any case
        if (GetPlayer()->GetMap()->Instanceable())
        return;

        uint32 bindspell = 3286;

        // send spell for homebinding (3286)
        npc->CastSpell(_player, bindspell, true);

        _player->PlayerTalkClass->SendCloseGossip();
    }

    void HandleRequestStabledPets(WorldPackets::NPC::RequestStabledPets& packet)
    {
        if (!CheckStableMaster(packet.StableMaster))
            return;

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        // remove mounts this fix bug where getting pet from stable while mounted deletes pet.
        if (GetPlayer()->IsMounted())
        GetPlayer()->RemoveAurasByType(SPELL_AURA_MOUNTED);

        _player->SetStableMaster(packet.StableMaster);
    }

    void SendPetStableResult(StableResult result)
    {
        WorldPackets::Pet::PetStableResult petStableResult;
        petStableResult.Result = AsUnderlyingType(result);
        SendPacket(petStableResult.Write());
    }

    void HandleSetPetSlot(WorldPackets::NPC::SetPetSlot& setPetSlot)
    {
        if (!CheckStableMaster(setPetSlot.StableMaster) || setPetSlot.DestSlot >= PET_SAVE_LAST_STABLE_SLOT)
        {
            SendPetStableResult(StableResult::InternalError);
            return;
        }

        _player->SetPetSlot(setPetSlot.PetNumber, PetSaveMode(setPetSlot.DestSlot));
    }

    void HandleRepairItemOpcode(WorldPackets::Item::RepairItem& packet)
    {
        TC_LOG_DEBUG("network", "WORLD: CMSG_REPAIR_ITEM: Npc {}, Item {}, UseGuildBank: {}",
                packet.NpcGUID.ToString(), packet.ItemGUID.ToString(), packet.UseGuildBank);

        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(packet.NpcGUID, UNIT_NPC_FLAG_REPAIR, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleRepairItemOpcode - {} not found or you can not interact with him.", packet.NpcGUID.ToString());
            return;
        }

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        // reputation discount
        float discountMod = _player->GetReputationPriceDiscount(unit);

        if (!packet.ItemGUID.IsEmpty())
        {
            TC_LOG_DEBUG("network", "ITEM: Repair {}, at {}", packet.ItemGUID.ToString(), packet.NpcGUID.ToString());

            Item* item = _player->GetItemByGuid(packet.ItemGUID);
            if (item)
                _player->DurabilityRepair(item->GetPos(), true, discountMod);
        }
        else
        {
            TC_LOG_DEBUG("network", "ITEM: Repair all items at {}", packet.NpcGUID.ToString());
            _player->DurabilityRepairAll(true, discountMod, packet.UseGuildBank);
        }
    }

}
