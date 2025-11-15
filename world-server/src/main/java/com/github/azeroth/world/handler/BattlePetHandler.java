package com.github.azeroth.world.handler;

public class BattlePetHandler {

    void HandleBattlePetRequestJournal(WorldPackets::BattlePet::BattlePetRequestJournal& /*battlePetRequestJournal*/)
    {
        GetBattlePetMgr()->SendJournal();
    }

    void HandleBattlePetRequestJournalLock(WorldPackets::BattlePet::BattlePetRequestJournalLock& /*battlePetRequestJournalLock*/)
    {
        GetBattlePetMgr()->SendJournalLockStatus();

        if (GetBattlePetMgr()->HasJournalLock())
        GetBattlePetMgr()->SendJournal();
    }

    void HandleBattlePetSetBattleSlot(WorldPackets::BattlePet::BattlePetSetBattleSlot& battlePetSetBattleSlot)
    {
        if (BattlePets::BattlePet* pet = GetBattlePetMgr()->GetPet(battlePetSetBattleSlot.PetGuid))
        if (WorldPackets::BattlePet::BattlePetSlot* slot = GetBattlePetMgr()->GetSlot(BattlePets::BattlePetSlot(battlePetSetBattleSlot.Slot)))
        slot->Pet = pet->PacketInfo;
    }

    void HandleQueryBattlePetName(WorldPackets::BattlePet::QueryBattlePetName& queryBattlePetName)
    {
        WorldPackets::BattlePet::QueryBattlePetNameResponse response;
        response.BattlePetID = queryBattlePetName.BattlePetID;

        Creature* summonedBattlePet = ObjectAccessor::GetCreatureOrPetOrVehicle(*_player, queryBattlePetName.UnitGUID);
        if (!summonedBattlePet || !summonedBattlePet->IsSummon())
        {
            SendPacket(response.Write());
            return;
        }

        response.CreatureID = summonedBattlePet->GetEntry();
        response.Timestamp = summonedBattlePet->GetBattlePetCompanionNameTimestamp();

        Unit* petOwner = summonedBattlePet->ToTempSummon()->GetSummonerUnit();
        if (!petOwner->IsPlayer())
        {
            SendPacket(response.Write());
            return;
        }

        BattlePets::BattlePet const* battlePet = petOwner->ToPlayer()->GetSession()->GetBattlePetMgr()->GetPet(queryBattlePetName.BattlePetID);
        if (!battlePet)
        {
            SendPacket(response.Write());
            return;
        }

        response.Name = battlePet->PacketInfo.Name;
        if (battlePet->DeclinedName)
        {
            response.HasDeclined = true;
            response.DeclinedNames = *battlePet->DeclinedName;
        }

        response.Allow = !response.Name.empty();

        SendPacket(response.Write());
    }

    void HandleBattlePetSetFlags(WorldPackets::BattlePet::BattlePetSetFlags& battlePetSetFlags)
    {
        if (!GetBattlePetMgr()->HasJournalLock())
        return;

        if (BattlePets::BattlePet* pet = GetBattlePetMgr()->GetPet(battlePetSetFlags.PetGuid))
        {
            if (battlePetSetFlags.ControlType == BattlePets::FLAGS_CONTROL_TYPE_APPLY)
                pet->PacketInfo.Flags |= battlePetSetFlags.Flags;
            else // FLAGS_CONTROL_TYPE_REMOVE
                pet->PacketInfo.Flags &= ~battlePetSetFlags.Flags;

            if (pet->SaveInfo != BattlePets::BATTLE_PET_NEW)
                pet->SaveInfo = BattlePets::BATTLE_PET_CHANGED;
        }
    }

    void HandleBattlePetClearFanfare(WorldPackets::BattlePet::BattlePetClearFanfare& battlePetClearFanfare)
    {
        GetBattlePetMgr()->ClearFanfare(battlePetClearFanfare.PetGuid);
    }

    void HandleBattlePetSummon(WorldPackets::BattlePet::BattlePetSummon& battlePetSummon)
    {
        if (*_player->m_activePlayerData->SummonedBattlePetGUID != battlePetSummon.PetGuid)
        GetBattlePetMgr()->SummonPet(battlePetSummon.PetGuid);
    else
        GetBattlePetMgr()->DismissPet();
    }

    void HandleBattlePetUpdateNotify(WorldPackets::BattlePet::BattlePetUpdateNotify& battlePetUpdateNotify)
    {
        GetBattlePetMgr()->UpdateBattlePetData(battlePetUpdateNotify.PetGuid);
    }

}
