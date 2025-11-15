package com.github.azeroth.world.handler;

public class SkillHandler {

    void HandleLearnTalentOpcode(WorldPackets::Talent::LearnTalent& packet)
    {
        if (_player->LearnTalent(packet.TalentID, packet.Rank))
            _player->SendTalentsInfoData();
    }

    void HandleLearnPreviewTalentsOpcode(WorldPackets::Talent::LearnPreviewTalents& packet)
    {
        if (!_player->GetPrimaryTalentTree() && packet.TabIndex >= 0)
            if (TalentTabEntry const* talentTab = sDB2Manager.GetTalentTabByIndex(_player->GetClass(), packet.TabIndex))
        _player->SetPrimaryTalentTree(talentTab->ID, true);

        for (auto const& talentInfo : packet.Talents)
        if (!_player->LearnTalent(talentInfo.TalentID, talentInfo.Rank))
            break;

        _player->SendTalentsInfoData();
    }

    void HandleSetPrimaryTalentTreeOpcode(WorldPackets::Talent::SetPrimaryTalentTree& packet)
    {
        if (_player->GetPrimaryTalentTree() != 0 || packet.TabIndex < 0)
            return;

        if (TalentTabEntry const* talentTab = sDB2Manager.GetTalentTabByIndex(_player->GetClass(), packet.TabIndex))
        _player->SetPrimaryTalentTree(talentTab->ID, true);
    }

    void HandleConfirmRespecWipeOpcode(WorldPackets::Talent::ConfirmRespecWipe& confirmRespecWipe)
    {
        Creature* unit = GetPlayer()->GetNPCIfCanInteractWith(confirmRespecWipe.RespecMaster, UNIT_NPC_FLAG_TRAINER, UNIT_NPC_FLAG_2_NONE);
        if (!unit)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleConfirmRespecWipeOpcode - {} not found or you can't interact with him.", confirmRespecWipe.RespecMaster.ToString());
            return;
        }

        if (confirmRespecWipe.RespecType != SPEC_RESET_TALENTS)
        {
            TC_LOG_DEBUG("network", "WORLD: HandleConfirmRespecWipeOpcode - reset type {} is not implemented.", confirmRespecWipe.RespecType);
            return;
        }

        if (!unit->CanResetTalents(_player))
            return;

        // remove fake death
        if (GetPlayer()->HasUnitState(UNIT_STATE_DIED))
        GetPlayer()->RemoveAurasByType(SPELL_AURA_FEIGN_DEATH);

        if (!_player->ResetTalents())
            return;

        unit->CastSpell(_player, 14867, true);                  //spell: "Untalent Visual Effect"
    }

    void HandleUnlearnSkillOpcode(WorldPackets::Spells::UnlearnSkill& packet)
    {
        SkillRaceClassInfoEntry const* rcEntry = sDB2Manager.GetSkillRaceClassInfo(packet.SkillLine, GetPlayer()->GetRace(), GetPlayer()->GetClass());
        if (!rcEntry || !(rcEntry->Flags & SKILL_FLAG_UNLEARNABLE))
            return;

        GetPlayer()->SetSkill(packet.SkillLine, 0, 0, 0);
    }

}
