package com.github.azeroth.world.handler;

public class CombatHandler {


    void HandleAttackSwingOpcode(WorldPackets::Combat::AttackSwing& packet)
    {
        Unit* enemy = ObjectAccessor::GetUnit(*_player, packet.Victim);

        if (!enemy)
        {
            // stop attack state at client
            SendAttackStop(nullptr);
            return;
        }

        if (!_player->IsValidAttackTarget(enemy))
        {
            // stop attack state at client
            SendAttackStop(enemy);
            return;
        }

        //! Client explicitly checks the following before sending CMSG_ATTACK_SWING packet,
        //! so we'll place the same check here. Note that it might be possible to reuse this snippet
        //! in other places as well.
        if (Vehicle* vehicle = _player->GetVehicle())
        {
            VehicleSeatEntry const* seat = vehicle->GetSeatForPassenger(_player);
            ASSERT(seat);
            if (!(seat->Flags & VEHICLE_SEAT_FLAG_CAN_ATTACK))
            {
                SendAttackStop(enemy);
                return;
            }
        }

        _player->Attack(enemy, true);
    }

    void HandleAttackStopOpcode(WorldPackets::Combat::AttackStop& /*packet*/)
    {
        GetPlayer()->AttackStop();
    }

    void HandleSetSheathedOpcode(WorldPackets::Combat::SetSheathed& packet)
    {
        if (packet.CurrentSheathState >= MAX_SHEATH_STATE)
        {
            TC_LOG_ERROR("network", "Unknown sheath state {} ??", packet.CurrentSheathState);
            return;
        }

        GetPlayer()->SetSheath(SheathState(packet.CurrentSheathState));
    }

    void SendAttackStop(Unit const* enemy)
    {
        SendPacket(WorldPackets::Combat::SAttackStop(GetPlayer(), enemy).Write());
    }

}
