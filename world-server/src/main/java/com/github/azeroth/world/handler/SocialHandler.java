package com.github.azeroth.world.handler;

public class SocialHandler {

    void HandleContactListOpcode(WorldPackets::Social::SendContactList& packet)
    {
        TC_LOG_DEBUG("network", "HandleContactListOpcode: Flags: {}", packet.Flags);
        _player->GetSocial()->SendSocialList(_player, packet.Flags);
    }

    void HandleAddFriendOpcode(WorldPackets::Social::AddFriend& packet)
    {
        if (!normalizePlayerName(packet.Name))
            return;

        TC_LOG_DEBUG("network", "HandleAddFriendOpcode: {} asked to add friend: {}",
                GetPlayerInfo(), packet.Name);

        CharacterCacheEntry const* friendCharacterInfo = sCharacterCache->GetCharacterCacheByName(packet.Name);
        if (!friendCharacterInfo)
        {
            sSocialMgr->SendFriendStatus(GetPlayer(), FRIEND_NOT_FOUND, ObjectGuid::Empty);
            return;
        }

        auto processFriendRequest = [this,
            playerGuid = _player->GetGUID(),
            friendGuid = friendCharacterInfo->Guid,
            friendAccountGuid = ObjectGuid::Create<HighGuid::WowAccount>(friendCharacterInfo->AccountId),
            team = Player::TeamForRace(friendCharacterInfo->Race),
            friendNote = std::move(packet.Notes)]()
        {
            if (playerGuid.GetCounter() != m_GUIDLow)
                return; // not the player initiating request, do nothing

            FriendsResult friendResult = FRIEND_NOT_FOUND;
            if (friendGuid == GetPlayer()->GetGUID())
            friendResult = FRIEND_SELF;
        else if (GetPlayer()->GetTeam() != team && !HasPermission(rbac::RBAC_PERM_TWO_SIDE_ADD_FRIEND))
            friendResult = FRIEND_ENEMY;
        else if (GetPlayer()->GetSocial()->HasFriend(friendGuid))
            friendResult = FRIEND_ALREADY;
        else
            {
                Player* pFriend = ObjectAccessor::FindPlayer(friendGuid);
                if (pFriend && pFriend->IsVisibleGloballyFor(GetPlayer()))
                    friendResult = FRIEND_ADDED_ONLINE;
                else
                    friendResult = FRIEND_ADDED_OFFLINE;
                if (GetPlayer()->GetSocial()->AddToSocialList(friendGuid, friendAccountGuid, SOCIAL_FLAG_FRIEND))
                GetPlayer()->GetSocial()->SetFriendNote(friendGuid, friendNote);
            else
                friendResult = FRIEND_LIST_FULL;
            }

            sSocialMgr->SendFriendStatus(GetPlayer(), friendResult, friendGuid);
        };

        if (HasPermission(rbac::RBAC_PERM_ALLOW_GM_FRIEND))
        {
            processFriendRequest();
            return;
        }

        // First try looking up friend candidate security from online object
        if (Player* friendPlayer = ObjectAccessor::FindPlayer(friendCharacterInfo->Guid))
        {
            if (!AccountMgr::IsPlayerAccount(friendPlayer->GetSession()->GetSecurity()))
            {
                sSocialMgr->SendFriendStatus(GetPlayer(), FRIEND_NOT_FOUND, ObjectGuid::Empty);
                return;
            }

            processFriendRequest();
            return;
        }

        // When not found, consult database
        GetQueryProcessor().AddCallback(AccountMgr::GetSecurityAsync(friendCharacterInfo->AccountId, sRealmList->GetCurrentRealmId().Realm,
        [this, continuation = std::move(processFriendRequest)](uint32 friendSecurity)
        {
            if (!AccountMgr::IsPlayerAccount(friendSecurity))
            {
                sSocialMgr->SendFriendStatus(GetPlayer(), FRIEND_NOT_FOUND, ObjectGuid::Empty);
                return;
            }

            continuation();
        }));
    }

    void HandleDelFriendOpcode(WorldPackets::Social::DelFriend& packet)
    {
        /// @todo: handle VirtualRealmAddress
        TC_LOG_DEBUG("network", "HandleDelFriendOpcode: {}", packet.Player.Guid.ToString());

        GetPlayer()->GetSocial()->RemoveFromSocialList(packet.Player.Guid, SOCIAL_FLAG_FRIEND);

        sSocialMgr->SendFriendStatus(GetPlayer(), FRIEND_REMOVED, packet.Player.Guid);
    }

    void HandleAddIgnoreOpcode(WorldPackets::Social::AddIgnore& packet)
    {
        if (!normalizePlayerName(packet.Name))
            return;

        TC_LOG_DEBUG("network", "HandleAddIgnoreOpcode: {} asked to Ignore: {}",
                GetPlayer()->GetName(), packet.Name);

        ObjectGuid ignoreGuid;
        FriendsResult ignoreResult = FRIEND_IGNORE_NOT_FOUND;

        if (CharacterCacheEntry const* characterInfo = sCharacterCache->GetCharacterCacheByName(packet.Name))
        {
            ignoreGuid = characterInfo->Guid;
            ObjectGuid ignoreAccountGuid = ObjectGuid::Create<HighGuid::WowAccount>(characterInfo->AccountId);
            if (ignoreGuid == GetPlayer()->GetGUID())              //not add yourself
            ignoreResult = FRIEND_IGNORE_SELF;
        else if (GetPlayer()->GetSocial()->HasIgnore(ignoreGuid, ignoreAccountGuid))
            ignoreResult = FRIEND_IGNORE_ALREADY;
        else
            {
                ignoreResult = FRIEND_IGNORE_ADDED;

                // ignore list full
                if (!GetPlayer()->GetSocial()->AddToSocialList(ignoreGuid, ignoreAccountGuid, SOCIAL_FLAG_IGNORED))
                ignoreResult = FRIEND_IGNORE_FULL;
            }
        }

        sSocialMgr->SendFriendStatus(GetPlayer(), ignoreResult, ignoreGuid);
    }

    void HandleDelIgnoreOpcode(WorldPackets::Social::DelIgnore& packet)
    {
        /// @todo: handle VirtualRealmAddress
        TC_LOG_DEBUG("network", "HandleDelIgnoreOpcode: {}", packet.Player.Guid.ToString());

        GetPlayer()->GetSocial()->RemoveFromSocialList(packet.Player.Guid, SOCIAL_FLAG_IGNORED);

        sSocialMgr->SendFriendStatus(GetPlayer(), FRIEND_IGNORE_REMOVED, packet.Player.Guid);
    }

    void HandleSetContactNotesOpcode(WorldPackets::Social::SetContactNotes& packet)
    {
        /// @todo: handle VirtualRealmAddress
        TC_LOG_DEBUG("network", "HandleSetContactNotesOpcode: Contact: {}, Notes: {}", packet.Player.Guid.ToString(), packet.Notes);
        _player->GetSocial()->SetFriendNote(packet.Player.Guid, packet.Notes);
    }

    void HandleSocialContractRequest(WorldPackets::Social::SocialContractRequest& /*socialContractRequest*/)
    {
        WorldPackets::Social::SocialContractRequestResponse response;
        response.ShowSocialContract = false;
        SendPacket(response.Write());
    }
}
