package com.github.azeroth.world.handler;

public class TaxiHandler {

    void HandleEnableTaxiNodeOpcode(WorldPackets::Taxi::EnableTaxiNode& enableTaxiNode)
    {
        if (Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(enableTaxiNode.Unit, UNIT_NPC_FLAG_FLIGHTMASTER, UNIT_NPC_FLAG_2_NONE))
        SendLearnNewTaxiNode(unit);
    }

    void HandleTaxiNodeStatusQueryOpcode(WorldPackets::Taxi::TaxiNodeStatusQuery& taxiNodeStatusQuery)
    {
        SendTaxiStatus(taxiNodeStatusQuery.UnitGUID);
    }

    void SendTaxiStatus(ObjectGuid guid)
    {
        Player* const player = GetPlayer();
        Creature* unit = ObjectAccessor::GetCreature(*player, guid);
        if (!unit || unit->IsHostileTo(player) || !unit->HasNpcFlag(UNIT_NPC_FLAG_FLIGHTMASTER))
        {
            TC_LOG_DEBUG("network", "SendTaxiStatus - {} not found or you can't interact with him.", guid.ToString());
            return;
        }

        // find taxi node
        uint32 nearest = sObjectMgr->GetNearestTaxiNode(unit->GetPositionX(), unit->GetPositionY(), unit->GetPositionZ(), unit->GetMapId(), player->GetTeam());

        WorldPackets::Taxi::TaxiNodeStatus data;
        data.Unit = guid;

        if (!nearest)
            data.Status = TAXISTATUS_NONE;
        else if (unit->GetReactionTo(GetPlayer()) >= REP_NEUTRAL)
            data.Status = GetPlayer()->m_taxi.IsTaximaskNodeKnown(nearest) ? TAXISTATUS_LEARNED : TAXISTATUS_UNLEARNED;
    else
        data.Status = TAXISTATUS_NOT_ELIGIBLE;

        SendPacket(data.Write());
    }

    void HandleTaxiQueryAvailableNodesOpcode(WorldPackets::Taxi::TaxiQueryAvailableNodes& taxiQueryAvailableNodes)
    {
        // cheating checks
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(taxiQueryAvailableNodes.Unit, UNIT_NPC_FLAG_FLIGHTMASTER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleTaxiQueryAvailableNodes - {} not found or you can't interact with him.", taxiQueryAvailableNodes.Unit.ToString());
            return;
        }
        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        // unknown taxi node case
        if (SendLearnNewTaxiNode(unit))
            return;

        // known taxi node case
        SendTaxiMenu(unit);
    }

    void SendTaxiMenu(Creature* unit)
    {
        // find current node
        uint32 curloc = sObjectMgr->GetNearestTaxiNode(unit->GetPositionX(), unit->GetPositionY(), unit->GetPositionZ(), unit->GetMapId(), GetPlayer()->GetTeam());
        if (!curloc)
            return;

        bool lastTaxiCheaterState = GetPlayer()->isTaxiCheater();
        if (unit->GetEntry() == 29480)
            GetPlayer()->SetTaxiCheater(true); // Grimwing in Ebon Hold, special case. NOTE: Not perfect, Zul'Aman should not be included according to WoWhead, and I think taxicheat includes it.

        TC_LOG_DEBUG("network", "WORLD: CMSG_TAXINODE_STATUS_QUERY {} ", curloc);

        WorldPackets::Taxi::ShowTaxiNodes data;
        data.WindowInfo.emplace();
        data.WindowInfo->UnitGUID = unit->GetGUID();
        data.WindowInfo->CurrentNode = curloc;

        GetPlayer()->m_taxi.AppendTaximaskTo(data, lastTaxiCheaterState);

        TaxiMask reachableNodes;
        TaxiPathGraph::GetReachableNodesMask(sTaxiNodesStore.LookupEntry(curloc), &reachableNodes);

        for (std::size_t i = 0; i < reachableNodes.size(); ++i)
        {
            data.CanLandNodes[i] &= reachableNodes[i];
            data.CanUseNodes[i] &= reachableNodes[i];
        }

        SendPacket(data.Write());

        GetPlayer()->SetTaxiCheater(lastTaxiCheaterState);
    }

    bool SendLearnNewTaxiNode(Creature* unit)
    {
        // find current node
        uint32 curloc = sObjectMgr->GetNearestTaxiNode(unit->GetPositionX(), unit->GetPositionY(), unit->GetPositionZ(), unit->GetMapId(), GetPlayer()->GetTeam());

        if (curloc == 0)
            return true;                                        // `true` send to avoid SendTaxiMenu call with one more curlock seartch with same false result.

        if (GetPlayer()->m_taxi.SetTaximaskNode(curloc))
        {
            SendPacket(WorldPackets::Taxi::NewTaxiPath(curloc).Write());

            WorldPackets::Taxi::TaxiNodeStatus data;
            data.Unit = unit->GetGUID();
            data.Status = TAXISTATUS_LEARNED;
            SendPacket(data.Write());

            return true;
        }
    else
        return false;
    }

    void SendDiscoverNewTaxiNode(uint32 nodeid)
    {
        if (GetPlayer()->m_taxi.SetTaximaskNode(nodeid))
        SendPacket(WorldPackets::Taxi::NewTaxiPath(nodeid).Write());
    }

    void HandleActivateTaxiOpcode(WorldPackets::Taxi::ActivateTaxi& activateTaxi)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(activateTaxi.Vendor, UNIT_NPC_FLAG_FLIGHTMASTER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleActivateTaxiOpcode - {} not found or you can't interact with it.", activateTaxi.Vendor.ToString());
            SendActivateTaxiReply(ERR_TAXITOOFARAWAY);
            return;
        }

        uint32 curloc = sObjectMgr->GetNearestTaxiNode(unit->GetPositionX(), unit->GetPositionY(), unit->GetPositionZ(), unit->GetMapId(), GetPlayer()->GetTeam());
        if (!curloc)
            return;

        TaxiNodesEntry const* from = sTaxiNodesStore.LookupEntry(curloc);
        TaxiNodesEntry const* to = sTaxiNodesStore.LookupEntry(activateTaxi.Node);
        if (!to)
            return;

        if (!GetPlayer()->isTaxiCheater())
        {
            if (!GetPlayer()->m_taxi.IsTaximaskNodeKnown(curloc) || !GetPlayer()->m_taxi.IsTaximaskNodeKnown(activateTaxi.Node))
            {
                SendActivateTaxiReply(ERR_TAXINOTVISITED);
                return;
            }
        }

        uint32 preferredMountDisplay = 0;
        if (MountEntry const* mount = sMountStore.LookupEntry(activateTaxi.FlyingMountID))
        {
            if (GetPlayer()->HasSpell(mount->SourceSpellID))
            {
                if (DB2Manager::MountXDisplayContainer const* mountDisplays = sDB2Manager.GetMountDisplays(mount->ID))
                {
                    DB2Manager::MountXDisplayContainer usableDisplays;
                    std::copy_if(mountDisplays->begin(), mountDisplays->end(), std::back_inserter(usableDisplays), [this](MountXDisplayEntry const* mountDisplay)
                    {
                        return ConditionMgr::IsPlayerMeetingCondition(GetPlayer(), mountDisplay->PlayerConditionID);
                    });

                    if (!usableDisplays.empty())
                        preferredMountDisplay = Trinity::Containers::SelectRandomContainerElement(usableDisplays)->CreatureDisplayInfoID;
                }
            }
        }

        std::vector<uint32> nodes;
        TaxiPathGraph::GetCompleteNodeRoute(from, to, GetPlayer(), nodes);
        GetPlayer()->ActivateTaxiPathTo(nodes, unit, 0, preferredMountDisplay);
    }

    void SendActivateTaxiReply(ActivateTaxiReply reply)
    {
        WorldPackets::Taxi::ActivateTaxiReply data;
        data.Reply = reply;
        SendPacket(data.Write());
    }

    void HandleTaxiRequestEarlyLanding(WorldPackets::Taxi::TaxiRequestEarlyLanding& /*taxiRequestEarlyLanding*/)
    {
        if (FlightPathMovementGenerator* flight = dynamic_cast<FlightPathMovementGenerator*>(GetPlayer()->GetMotionMaster()->GetCurrentMovementGenerator()))
        {
            if (GetPlayer()->m_taxi.RequestEarlyLanding())
            {
                flight->LoadPath(GetPlayer(), flight->GetPath()[flight->GetCurrentNode()]->NodeIndex);
                flight->Reset(GetPlayer());
            }
        }
    }

}
