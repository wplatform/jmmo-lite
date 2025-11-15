package com.github.azeroth.world.handler;

public class DuelHandler {

#define SPELL_DUEL  7266
            #define SPELL_MOUNTED_DUEL  62875

    void HandleCanDuel(WorldPackets::Duel::CanDuel& packet)
    {
        Player* player = ObjectAccessor::FindPlayer(packet.TargetGUID);

        if (!player)
            return;

        WorldPackets::Duel::CanDuelResult response;
        response.TargetGUID = packet.TargetGUID;
        response.Result = !player->duel;
        SendPacket(response.Write());

        if (response.Result)
        {
            if (_player->IsMounted())
                _player->CastSpell(player, SPELL_MOUNTED_DUEL);
            else
                _player->CastSpell(player, SPELL_DUEL);
        }
    }

    void HandleDuelResponseOpcode(WorldPackets::Duel::DuelResponse& duelResponse)
    {
        if (duelResponse.Accepted && !duelResponse.Forfeited)
            HandleDuelAccepted(duelResponse.ArbiterGUID);
        else
            HandleDuelCancelled();
    }

    void HandleDuelAccepted(ObjectGuid arbiterGuid)
    {
        Player* player = GetPlayer();
        if (!player->duel || player == player->duel->Initiator || player->duel->State != DUEL_STATE_CHALLENGED)
            return;

        Player* target = player->duel->Opponent;
        if (*target->m_playerData->DuelArbiter != arbiterGuid)
        return;

        TC_LOG_DEBUG("network", "Player 1 is: {} ({})", player->GetGUID().ToString(), player->GetName());
        TC_LOG_DEBUG("network", "Player 2 is: {} ({})", target->GetGUID().ToString(), target->GetName());

        time_t now = GameTime::GetGameTime();
        player->duel->StartTime = now + 3;
        target->duel->StartTime = now + 3;

        player->duel->State = DUEL_STATE_COUNTDOWN;
        target->duel->State = DUEL_STATE_COUNTDOWN;

        WorldPackets::Duel::DuelCountdown packet(3000); // milliseconds
        WorldPacket const* worldPacket = packet.Write();
        player->GetSession()->SendPacket(worldPacket);
        target->GetSession()->SendPacket(worldPacket);
    }

    void HandleDuelCancelled()
    {
        Player* player = GetPlayer();

        // no duel requested
        if (!player->duel || player->duel->State == DUEL_STATE_COMPLETED)
            return;

        // player surrendered in a duel using /forfeit
        if (GetPlayer()->duel->State == DUEL_STATE_IN_PROGRESS)
        {
            GetPlayer()->CombatStopWithPets(true);
            GetPlayer()->duel->Opponent->CombatStopWithPets(true);

            GetPlayer()->CastSpell(GetPlayer(), 7267, true);    // beg
            GetPlayer()->DuelComplete(DUEL_WON);
            return;
        }

        GetPlayer()->DuelComplete(DUEL_INTERRUPTED);
    }
}
