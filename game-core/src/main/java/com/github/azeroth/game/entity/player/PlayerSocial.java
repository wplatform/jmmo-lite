package com.github.azeroth.game.entity.player;


import java.util.ArrayList;
import java.util.HashMap;


public class PlayerSocial {
    public HashMap<ObjectGuid, FriendInfo> playerSocialMap = new HashMap<ObjectGuid, FriendInfo>();
    public ArrayList<ObjectGuid> ignoredAccounts = new ArrayList<>();
    private ObjectGuid m_playerGUID = ObjectGuid.EMPTY;

    public final boolean addToSocialList(ObjectGuid friendGuid, ObjectGuid accountGuid, SocialFlag flag) {
        // check client limits
        if (getNumberOfSocialsWithFlag(flag) >= (((flag.getValue() & SocialFlag.Friend.getValue()) != 0) ? SocialManager.FRIEND_LIMIT_MAX : SocialManager.IGNORE_LIMIT)) {
            return false;
        }

        var friendInfo = playerSocialMap.get(friendGuid);

        if (friendInfo != null) {
            friendInfo.flags |= flag;
            friendInfo.wowAccountGuid = accountGuid;

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHARACTER_SOCIAL_FLAGS);
            stmt.AddValue(0, (byte) friendInfo.flags);
            stmt.AddValue(1, getPlayerGUID().getCounter());
            stmt.AddValue(2, friendGuid.getCounter());
            DB.characters.execute(stmt);
        } else {
            FriendInfo fi = new FriendInfo();
            fi.flags = SocialFlag.forValue(fi.flags.getValue() | flag.getValue());
            fi.wowAccountGuid = accountGuid;
            playerSocialMap.put(friendGuid, fi);

            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_SOCIAL);
            stmt.AddValue(0, getPlayerGUID().getCounter());
            stmt.AddValue(1, friendGuid.getCounter());
            stmt.AddValue(2, (byte) flag.getValue());
            DB.characters.execute(stmt);
        }

        if (flag.hasFlag(SocialFlag.Ignored)) {
            ignoredAccounts.add(accountGuid);
        }

        return true;
    }

    public final void removeFromSocialList(ObjectGuid friendGuid, SocialFlag flag) {
        var friendInfo = playerSocialMap.get(friendGuid);

        if (friendInfo == null) // not exist
        {
            return;
        }

        friendInfo.flags &= ~flag;

        if (friendInfo.flags == 0) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_SOCIAL);
            stmt.AddValue(0, getPlayerGUID().getCounter());
            stmt.AddValue(1, friendGuid.getCounter());
            DB.characters.execute(stmt);

            var accountGuid = friendInfo.wowAccountGuid;

            playerSocialMap.remove(friendGuid);

            if (flag.hasFlag(SocialFlag.Ignored)) {
                var otherIgnoreForAccount = playerSocialMap.Any(social -> social.value.flags.hasFlag(SocialFlag.Ignored) && social.value.wowAccountGuid == accountGuid);

                if (!otherIgnoreForAccount) {
                    ignoredAccounts.remove(accountGuid);
                }
            }
        } else {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHARACTER_SOCIAL_FLAGS);
            stmt.AddValue(0, (byte) friendInfo.flags);
            stmt.AddValue(1, getPlayerGUID().getCounter());
            stmt.AddValue(2, friendGuid.getCounter());
            DB.characters.execute(stmt);
        }
    }

    public final void setFriendNote(ObjectGuid friendGuid, String note) {
        if (!playerSocialMap.containsKey(friendGuid)) // not exist
        {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHARACTER_SOCIAL_NOTE);
        stmt.AddValue(0, note);
        stmt.AddValue(1, getPlayerGUID().getCounter());
        stmt.AddValue(2, friendGuid.getCounter());
        DB.characters.execute(stmt);

        playerSocialMap.get(friendGuid).note = note;
    }

    public final void sendSocialList(Player player, SocialFlag flags) {
        if (!player) {
            return;
        }

        int friendsCount = 0;
        int ignoredCount = 0;

        ContactList contactList = new ContactList();
        contactList.flags = flags;

        for (var v : playerSocialMap.entrySet()) {
            var contactFlags = v.getValue().flags;

            if (!contactFlags.hasFlag(flags)) {
                continue;
            }

            // Check client limit for friends list
            if (contactFlags.hasFlag(SocialFlag.Friend)) {
                if (++friendsCount > SocialManager.FRIEND_LIMIT_MAX) {
                    continue;
                }
            }

            // Check client limit for ignore list
            if (contactFlags.hasFlag(SocialFlag.Ignored)) {
                if (++ignoredCount > SocialManager.IGNORE_LIMIT) {
                    continue;
                }
            }

            SocialManager.getFriendInfo(player, v.getKey(), v.getValue());

            contactList.contacts.add(new ContactInfo(v.getKey(), v.getValue()));
        }

        player.sendPacket(contactList);
    }

    public final boolean hasFriend(ObjectGuid friendGuid) {
        return _HasContact(friendGuid, SocialFlag.Friend);
    }

    public final boolean hasIgnore(ObjectGuid ignoreGuid, ObjectGuid ignoreAccountGuid) {
        return _HasContact(ignoreGuid, SocialFlag.Ignored) || ignoredAccounts.contains(ignoreAccountGuid);
    }

    private int getNumberOfSocialsWithFlag(SocialFlag flag) {
        int counter = 0;

        for (var pair : playerSocialMap.entrySet()) {
            if (pair.getValue().flags.hasFlag(flag)) {
                ++counter;
            }
        }

        return counter;
    }

    private boolean _HasContact(ObjectGuid guid, SocialFlag flags) {
        var friendInfo = playerSocialMap.get(guid);

        if (friendInfo != null) {
            return friendInfo.flags.hasFlag(flags);
        }

        return false;
    }

    private ObjectGuid getPlayerGUID() {
        return m_playerGUID;
    }

    public final void setPlayerGUID(ObjectGuid guid) {
        m_playerGUID = guid;
    }
}
