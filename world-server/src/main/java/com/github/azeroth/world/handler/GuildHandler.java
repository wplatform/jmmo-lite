package com.github.azeroth.world.handler;

public class GuildHandler {

    void HandleGuildQueryOpcode(WorldPackets::Guild::QueryGuildInfo& query)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_QUERY [{}]: Guild: {} Target: {}",
                GetPlayerInfo(), query.GuildGuid.ToString(), query.PlayerGuid.ToString());

        if (Guild* guild = sGuildMgr->GetGuildByGuid(query.GuildGuid))
        {
            guild->SendQueryResponse(this);
            return;
        }

        WorldPackets::Guild::QueryGuildInfoResponse response;
        response.GuildGuid = query.GuildGuid;
        SendPacket(response.Write());

        TC_LOG_DEBUG("guild", "SMSG_GUILD_QUERY_RESPONSE [{}]", GetPlayerInfo());
    }

    void HandleGuildInviteByName(WorldPackets::Guild::GuildInviteByName& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_INVITE [{}]: Invited: {}", GetPlayerInfo(), packet.Name);
        if (normalizePlayerName(packet.Name))
            if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleInviteMember(this, packet.Name);
    }

    void HandleGuildOfficerRemoveMember(WorldPackets::Guild::GuildOfficerRemoveMember& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_REMOVE [{}]: Target: {}", GetPlayerInfo(), packet.Removee.ToString());

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleRemoveMember(this, packet.Removee);
    }

    void HandleGuildAcceptInvite(WorldPackets::Guild::AcceptGuildInvite& /*invite*/)
    {
        if (!GetPlayer()->GetGuildId())
        if (Guild* guild = sGuildMgr->GetGuildById(GetPlayer()->GetGuildIdInvited()))
        guild->HandleAcceptMember(this);
    }

    void HandleGuildDeclineInvitation(WorldPackets::Guild::GuildDeclineInvitation& /*decline*/)
    {
        if (GetPlayer()->GetGuildId())
        return;

        GetPlayer()->SetGuildIdInvited(UI64LIT(0));
    }

    void HandleGuildGetRoster(WorldPackets::Guild::GuildGetRoster& /*packet*/)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleRoster(this);
    else
        Guild::SendCommandResult(this, GUILD_COMMAND_GET_ROSTER, ERR_GUILD_PLAYER_NOT_IN_GUILD);
    }

    void HandleGuildPromoteMember(WorldPackets::Guild::GuildPromoteMember& promote)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_PROMOTE [{}]: Target: {}", GetPlayerInfo(), promote.Promotee.ToString());

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleUpdateMemberRank(this, promote.Promotee, false);
    }

    void HandleGuildDemoteMember(WorldPackets::Guild::GuildDemoteMember& demote)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_DEMOTE [{}]: Target: {}", GetPlayerInfo(), demote.Demotee.ToString());

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleUpdateMemberRank(this, demote.Demotee, true);
    }

    void HandleGuildAssignRank(WorldPackets::Guild::GuildAssignMemberRank& packet)
    {
        ObjectGuid setterGuid = GetPlayer()->GetGUID();

        TC_LOG_DEBUG("guild", "CMSG_GUILD_ASSIGN_MEMBER_RANK [{}]: Target: {} Rank: {}, Issuer: {}",
                GetPlayerInfo(), packet.Member.ToString(), packet.RankOrder, setterGuid.ToString());

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetMemberRank(this, packet.Member, setterGuid, GuildRankOrder(packet.RankOrder));
    }

    void HandleGuildLeave(WorldPackets::Guild::GuildLeave& /*leave*/)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleLeaveMember(this);
    }

    void HandleGuildDelete(WorldPackets::Guild::GuildDelete& /*packet*/)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleDelete(this);
    }

    void HandleGuildUpdateMotdText(WorldPackets::Guild::GuildUpdateMotdText& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_UPDATE_MOTD_TEXT [{}]: MOTD: {}", GetPlayerInfo(), std::string_view(packet.MotdText));

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetMOTD(this, packet.MotdText);
    }

    void HandleGuildSetMemberNote(WorldPackets::Guild::GuildSetMemberNote& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_SET_NOTE [{}]: Target: {}, Note: {}, Public: {}",
                GetPlayerInfo(), packet.NoteeGUID.ToString(), std::string_view(packet.Note), packet.IsPublic);

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetMemberNote(this, packet.Note, packet.NoteeGUID, packet.IsPublic);
    }

    void HandleGuildGetRanks(WorldPackets::Guild::GuildGetRanks& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_GET_RANKS [{}]: Guild: {}",
                GetPlayerInfo(), packet.GuildGUID.ToString());

        if (Guild* guild = sGuildMgr->GetGuildByGuid(packet.GuildGUID))
            if (guild->IsMember(_player->GetGUID()))
                guild->SendGuildRankInfo(this);
    }

    void HandleGuildAddRank(WorldPackets::Guild::GuildAddRank& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_ADD_RANK [{}]: Rank: {}", GetPlayerInfo(), std::string_view(packet.Name));

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleAddNewRank(this, packet.Name);
    }

    void HandleGuildDeleteRank(WorldPackets::Guild::GuildDeleteRank& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_DELETE_RANK [{}]: Rank: {}", GetPlayerInfo(), packet.RankOrder);

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleRemoveRank(this, GuildRankOrder(packet.RankOrder));
    }

    void HandleGuildShiftRank(WorldPackets::Guild::GuildShiftRank& shiftRank)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_SHIFT_RANK [{}]: RankOrder: {}, ShiftUp: {}", GetPlayerInfo(), shiftRank.RankOrder, shiftRank.ShiftUp ? "true" : "false");

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleShiftRank(this, GuildRankOrder(shiftRank.RankOrder), shiftRank.ShiftUp);
    }

    void HandleGuildUpdateInfoText(WorldPackets::Guild::GuildUpdateInfoText& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_UPDATE_INFO_TEXT [{}]: {}", GetPlayerInfo(), std::string_view(packet.InfoText));

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetInfo(this, packet.InfoText);
    }

    void HandleSaveGuildEmblem(WorldPackets::Guild::SaveGuildEmblem& packet)
    {
        EmblemInfo emblemInfo;
        emblemInfo.ReadPacket(packet);

        TC_LOG_DEBUG("guild", "CMSG_SAVE_GUILD_EMBLEM [{}]: Guid: [{}] Style: {}, Color: {}, BorderStyle: {}, BorderColor: {}, BackgroundColor: {}"
                , GetPlayerInfo(), packet.Vendor.ToString(), emblemInfo.GetStyle()
                , emblemInfo.GetColor(), emblemInfo.GetBorderStyle()
                , emblemInfo.GetBorderColor(), emblemInfo.GetBackgroundColor());

        if (GetPlayer()->GetNPCIfCanInteractWith(packet.Vendor, UNIT_NPC_FLAG_TABARDDESIGNER, UNIT_NPC_FLAG_2_NONE))
        {
            // Remove fake death
            if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
            GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

            if (!emblemInfo.ValidateEmblemColors())
            {
                Guild::SendSaveEmblemResult(this, ERR_GUILDEMBLEM_INVALID_TABARD_COLORS);
                return;
            }

            if (Guild* guild = GetPlayer()->GetGuild())
            guild->HandleSetEmblem(this, emblemInfo);
        else
            Guild::SendSaveEmblemResult(this, ERR_GUILDEMBLEM_NOGUILD); // "You are not part of a guild!";
        }
    else
        Guild::SendSaveEmblemResult(this, ERR_GUILDEMBLEM_INVALIDVENDOR); // "That's not an emblem vendor!"
    }

    void HandleGuildEventLogQuery(WorldPackets::Guild::GuildEventLogQuery& /*packet*/)
    {
        TC_LOG_DEBUG("guild", "MSG_GUILD_EVENT_LOG_QUERY [{}]", GetPlayerInfo());

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendEventLog(this);
    }

    void HandleGuildBankMoneyWithdrawn(WorldPackets::Guild::GuildBankRemainingWithdrawMoneyQuery& /*packet*/)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendMoneyInfo(this);
    }

    void HandleGuildPermissionsQuery(WorldPackets::Guild::GuildPermissionsQuery& /* packet */)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendPermissions(this);
    }

    // Called when clicking on Guild bank gameobject
    void HandleGuildBankActivate(WorldPackets::Guild::GuildBankActivate& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_ACTIVATE [{}]: [{}] AllSlots: {}"
                , GetPlayerInfo(), packet.Banker.ToString(), packet.FullUpdate);

        GameObject const* const go = GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK);
        if (!go)
            return;

        Guild* const guild = GetPlayer()->GetGuild();
        if (!guild)
        {
            Guild::SendCommandResult(this, GUILD_COMMAND_VIEW_TAB, ERR_GUILD_PLAYER_NOT_IN_GUILD);
            return;
        }

        guild->SendBankList(this, 0, packet.FullUpdate);
    }

    // Called when opening guild bank tab only (first one)
    void HandleGuildBankQueryTab(WorldPackets::Guild::GuildBankQueryTab& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_QUERY_TAB [{}]: {}, TabId: {}, ShowTabs: {}"
                , GetPlayerInfo(), packet.Banker.ToString(), packet.Tab, packet.FullUpdate);

        if (GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendBankList(this, packet.Tab, packet.FullUpdate);
    }

    void HandleGuildBankDepositMoney(WorldPackets::Guild::GuildBankDepositMoney& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_DEPOSIT_MONEY [{}]: [{}], money: {}",
                GetPlayerInfo(), packet.Banker.ToString(), packet.Money);

        if (GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        if (packet.Money && GetPlayer()->HasEnoughMoney(packet.Money))
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleMemberDepositMoney(this, packet.Money);
    }

    void HandleGuildBankWithdrawMoney(WorldPackets::Guild::GuildBankWithdrawMoney& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_WITHDRAW_MONEY [{}]: [{}], money: {}",
                GetPlayerInfo(), packet.Banker.ToString(), packet.Money);

        if (packet.Money && GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleMemberWithdrawMoney(this, packet.Money);
    }

    void HandleAutoGuildBankItem(WorldPackets::Guild::AutoGuildBankItem& depositGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(depositGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(depositGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), depositGuildBankItem.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), false, depositGuildBankItem.BankTab, depositGuildBankItem.BankSlot,
                depositGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), depositGuildBankItem.ContainerItemSlot, 0);
    }

    void HandleStoreGuildBankItem(WorldPackets::Guild::StoreGuildBankItem& storeGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(storeGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(storeGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), storeGuildBankItem.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), true, storeGuildBankItem.BankTab, storeGuildBankItem.BankSlot,
                storeGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), storeGuildBankItem.ContainerItemSlot, 0);
    }

    void HandleSwapItemWithGuildBankItem(WorldPackets::Guild::SwapItemWithGuildBankItem& swapItemWithGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(swapItemWithGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(swapItemWithGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), swapItemWithGuildBankItem.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), false, swapItemWithGuildBankItem.BankTab, swapItemWithGuildBankItem.BankSlot,
                swapItemWithGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), swapItemWithGuildBankItem.ContainerItemSlot, 0);
    }

    void HandleSwapGuildBankItemWithGuildBankItem(WorldPackets::Guild::SwapGuildBankItemWithGuildBankItem& swapGuildBankItemWithGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(swapGuildBankItemWithGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        guild->SwapItems(GetPlayer(), swapGuildBankItemWithGuildBankItem.BankTab[0], swapGuildBankItemWithGuildBankItem.BankSlot[0],
                swapGuildBankItemWithGuildBankItem.BankTab[1], swapGuildBankItemWithGuildBankItem.BankSlot[1], 0);
    }

    void HandleMoveGuildBankItem(WorldPackets::Guild::MoveGuildBankItem& moveGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(moveGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        guild->SwapItems(GetPlayer(), moveGuildBankItem.BankTab, moveGuildBankItem.BankSlot, moveGuildBankItem.BankTab1, moveGuildBankItem.BankSlot1, 0);
    }

    void HandleMergeItemWithGuildBankItem(WorldPackets::Guild::MergeItemWithGuildBankItem& mergeItemWithGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(mergeItemWithGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(mergeItemWithGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), mergeItemWithGuildBankItem.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), false, mergeItemWithGuildBankItem.BankTab, mergeItemWithGuildBankItem.BankSlot,
                mergeItemWithGuildBankItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), mergeItemWithGuildBankItem.ContainerItemSlot, mergeItemWithGuildBankItem.StackCount);
    }

    void HandleSplitItemToGuildBank(WorldPackets::Guild::SplitItemToGuildBank& splitItemToGuildBank)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(splitItemToGuildBank.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(splitItemToGuildBank.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), splitItemToGuildBank.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), false, splitItemToGuildBank.BankTab, splitItemToGuildBank.BankSlot,
                splitItemToGuildBank.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), splitItemToGuildBank.ContainerItemSlot, splitItemToGuildBank.StackCount);
    }

    void HandleMergeGuildBankItemWithItem(WorldPackets::Guild::MergeGuildBankItemWithItem& mergeGuildBankItemWithItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(mergeGuildBankItemWithItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(mergeGuildBankItemWithItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), mergeGuildBankItemWithItem.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), true, mergeGuildBankItemWithItem.BankTab, mergeGuildBankItemWithItem.BankSlot,
                mergeGuildBankItemWithItem.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), mergeGuildBankItemWithItem.ContainerItemSlot, mergeGuildBankItemWithItem.StackCount);
    }

    void HandleSplitGuildBankItemToInventory(WorldPackets::Guild::SplitGuildBankItemToInventory& splitGuildBankItemToInventory)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(splitGuildBankItemToInventory.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        if (!Player::IsInventoryPos(splitGuildBankItemToInventory.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), splitGuildBankItemToInventory.ContainerItemSlot))
        GetPlayer()->SendEquipError(EQUIP_ERR_INTERNAL_BAG_ERROR, nullptr);
    else
        guild->SwapItemsWithInventory(GetPlayer(), true, splitGuildBankItemToInventory.BankTab, splitGuildBankItemToInventory.BankSlot,
                splitGuildBankItemToInventory.ContainerSlot.value_or(INVENTORY_SLOT_BAG_0), splitGuildBankItemToInventory.ContainerItemSlot, splitGuildBankItemToInventory.StackCount);
    }

    void HandleAutoStoreGuildBankItem(WorldPackets::Guild::AutoStoreGuildBankItem& autoStoreGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(autoStoreGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        guild->SwapItemsWithInventory(GetPlayer(), true, autoStoreGuildBankItem.BankTab, autoStoreGuildBankItem.BankSlot,
                INVENTORY_SLOT_BAG_0, NULL_SLOT, 0);
    }

    void HandleMergeGuildBankItemWithGuildBankItem(WorldPackets::Guild::MergeGuildBankItemWithGuildBankItem& mergeGuildBankItemWithGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(mergeGuildBankItemWithGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        guild->SwapItems(GetPlayer(), mergeGuildBankItemWithGuildBankItem.BankTab, mergeGuildBankItemWithGuildBankItem.BankSlot,
                mergeGuildBankItemWithGuildBankItem.BankTab1, mergeGuildBankItemWithGuildBankItem.BankSlot1, mergeGuildBankItemWithGuildBankItem.StackCount);
    }

    void HandleSplitGuildBankItem(WorldPackets::Guild::SplitGuildBankItem& splitGuildBankItem)
    {
        if (!GetPlayer()->GetGameObjectIfCanInteractWith(splitGuildBankItem.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        return;

        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        guild->SwapItems(GetPlayer(), splitGuildBankItem.BankTab, splitGuildBankItem.BankSlot,
                splitGuildBankItem.BankTab1, splitGuildBankItem.BankSlot1, splitGuildBankItem.StackCount);
    }

    void HandleGuildBankBuyTab(WorldPackets::Guild::GuildBankBuyTab& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_BUY_TAB [{}]: [{}[, TabId: {}", GetPlayerInfo(), packet.Banker.ToString(), packet.BankTab);

        if (!packet.Banker || GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleBuyBankTab(this, packet.BankTab);
    }

    void HandleGuildBankUpdateTab(WorldPackets::Guild::GuildBankUpdateTab& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_UPDATE_TAB [{}]: [{}], TabId: {}, Name: {}, Icon: {}"
                , GetPlayerInfo(), packet.Banker.ToString(), packet.BankTab, std::string_view(packet.Name), std::string_view(packet.Icon));

        if (!packet.Name.empty() && !packet.Icon.empty())
            if (GetPlayer()->GetGameObjectIfCanInteractWith(packet.Banker, GAMEOBJECT_TYPE_GUILD_BANK))
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetBankTabInfo(this, packet.BankTab, packet.Name, packet.Icon);
    }

    void HandleGuildBankLogQuery(WorldPackets::Guild::GuildBankLogQuery& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_LOG_QUERY [{}]: TabId: {}", GetPlayerInfo(), packet.Tab);

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendBankLog(this, packet.Tab);
    }

    void HandleGuildBankTextQuery(WorldPackets::Guild::GuildBankTextQuery& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_GUILD_BANK_QUERY_TEXT [{}]: TabId: {}", GetPlayerInfo(), packet.Tab);

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SendBankTabText(this, packet.Tab);
    }

    void HandleGuildBankSetTabText(WorldPackets::Guild::GuildBankSetTabText& packet)
    {
        TC_LOG_DEBUG("guild", "CMSG_SET_GUILD_BANK_TEXT [{}]: TabId: {}, Text: {}", GetPlayerInfo(), packet.Tab, std::string_view(packet.TabText));

        if (Guild* guild = GetPlayer()->GetGuild())
        guild->SetBankTabText(packet.Tab, packet.TabText);
    }

    void HandleGuildSetRankPermissions(WorldPackets::Guild::GuildSetRankPermissions& packet)
    {
        Guild* guild = GetPlayer()->GetGuild();
        if (!guild)
            return;

        std::array<GuildBankRightsAndSlots, GUILD_BANK_MAX_TABS> rightsAndSlots;
        for (uint8 tabId = 0; tabId < GUILD_BANK_MAX_TABS; ++tabId)
            rightsAndSlots[tabId] = GuildBankRightsAndSlots(tabId, uint8(packet.TabFlags[tabId]), uint32(packet.TabWithdrawItemLimit[tabId]));

        TC_LOG_DEBUG("guild", "CMSG_GUILD_SET_RANK_PERMISSIONS [{}]: Rank: {} ({})", GetPlayerInfo(), std::string_view(packet.RankName), packet.RankOrder);

        guild->HandleSetRankInfo(this, GuildRankId(packet.RankID), packet.RankName, packet.Flags, packet.WithdrawGoldLimit, rightsAndSlots);
    }

    void HandleGuildRequestPartyState(WorldPackets::Guild::RequestGuildPartyState& packet)
    {
        if (Guild* guild = sGuildMgr->GetGuildByGuid(packet.GuildGUID))
            guild->HandleGuildPartyRequest(this);
    }

    void HandleGuildChallengeUpdateRequest(WorldPackets::Guild::GuildChallengeUpdateRequest& /*packet*/)
    {
        if (Guild* guild = _player->GetGuild())
            guild->HandleGuildRequestChallengeUpdate(this);
    }

    void HandleDeclineGuildInvites(WorldPackets::Guild::DeclineGuildInvites& packet)
    {
        if (packet.Allow)
            GetPlayer()->SetPlayerFlag(PLAYER_FLAGS_AUTO_DECLINE_GUILD);
    else
        GetPlayer()->RemovePlayerFlag(PLAYER_FLAGS_AUTO_DECLINE_GUILD);
    }

    void HandleRequestGuildRewardsList(WorldPackets::Guild::RequestGuildRewardsList& /*packet*/)
    {
        if (sGuildMgr->GetGuildById(_player->GetGuildId()))
        {
            std::vector<GuildReward> const& rewards = sGuildMgr->GetGuildRewards();

            WorldPackets::Guild::GuildRewardList rewardList;
            rewardList.Version = GameTime::GetSystemTime();
            rewardList.RewardItems.reserve(rewards.size());

            for (uint32 i = 0; i < rewards.size(); i++)
            {
                WorldPackets::Guild::GuildRewardItem rewardItem;
                rewardItem.ItemID = rewards[i].ItemID;
                rewardItem.RaceMask = rewards[i].RaceMask;
                rewardItem.MinGuildLevel = 0;
                rewardItem.MinGuildRep = rewards[i].MinGuildRep;
                rewardItem.AchievementsRequired = rewards[i].AchievementsRequired;
                rewardItem.Cost = rewards[i].Cost;
                rewardList.RewardItems.push_back(rewardItem);
            }

            SendPacket(rewardList.Write());
        }
    }

    void HandleGuildQueryNews(WorldPackets::Guild::GuildQueryNews& newsQuery)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        if (guild->GetGUID() == newsQuery.GuildGUID)
            guild->SendNewsUpdate(this);
    }

    void HandleGuildNewsUpdateSticky(WorldPackets::Guild::GuildNewsUpdateSticky& packet)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleNewsSetSticky(this, packet.NewsID, packet.Sticky);
    }

    void HandleGuildReplaceGuildMaster(WorldPackets::Guild::GuildReplaceGuildMaster& /*replaceGuildMaster*/)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetNewGuildMaster(this, "", true);
    }

    void HandleGuildSetGuildMaster(WorldPackets::Guild::GuildSetGuildMaster& packet)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetNewGuildMaster(this, packet.NewMasterName, false);
    }

    void HandleGuildSetAchievementTracking(WorldPackets::Guild::GuildSetAchievementTracking& packet)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleSetAchievementTracking(this, packet.AchievementIDs.data(), packet.AchievementIDs.data() + packet.AchievementIDs.size());
    }

    void HandleGuildGetAchievementMembers(WorldPackets::Achievement::GuildGetAchievementMembers& getAchievementMembers)
    {
        if (Guild* guild = GetPlayer()->GetGuild())
        guild->HandleGetAchievementMembers(this, uint32(getAchievementMembers.AchievementID));
    }
}
