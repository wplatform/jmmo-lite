package com.github.azeroth.game.entity.player;


import java.util.HashMap;


public class SocialManager {
    public static final int FRIEND_LIMIT_MAX = 50;
    public static final int IGNORE_LIMIT = 50;
    private final HashMap<ObjectGuid, PlayerSocial> socialMap = new HashMap<ObjectGuid, PlayerSocial>();

    private SocialManager() {
    }

    public static void getFriendInfo(Player player, ObjectGuid friendGuid, FriendInfo friendInfo) {
        if (!player) {
            return;
        }

        friendInfo.status = FriendStatus.Offline;
        friendInfo.area = 0;
        friendInfo.level = 0;
        friendInfo.class = playerClass.forValue(0);

        var target = global.getObjAccessor().findPlayer(friendGuid);

        if (!target) {
            return;
        }

        var playerFriendInfo = player.getSocial().playerSocialMap.get(friendGuid);

        if (playerFriendInfo != null) {
            friendInfo.note = playerFriendInfo.note;
        }

        // PLAYER see his team only and PLAYER can't see MODERATOR, GAME MASTER, ADMINISTRATOR character
        // MODERATOR, GAME MASTER, ADMINISTRATOR can see all

        if (!player.getSession().hasPermission(RBACPermissions.WhoSeeAllSecLevels) && target.getSession().getSecurity().getValue() > AccountTypes.forValue(WorldConfig.getIntValue(WorldCfg.GmLevelInWhoList))) {
            return;
        }

        // player can see member of other team only if CONFIG_ALLOW_TWO_SIDE_WHO_LIST
        if (target.getTeam() != player.getTeam() && !player.getSession().hasPermission(RBACPermissions.TwoSideWhoList)) {
            return;
        }

        if (target.isVisibleGloballyFor(player)) {
            if (target.isDND()) {
                friendInfo.status = FriendStatus.DND;
            } else if (target.isAFK()) {
                friendInfo.status = FriendStatus.AFK;
            } else {
                friendInfo.status = FriendStatus.online;

                if (target.getSession().getRecruiterId() == player.getSession().getAccountId() || target.getSession().getAccountId() == player.getSession().getRecruiterId()) {
                    friendInfo.status = FriendStatus.forValue(friendInfo.status.getValue() | FriendStatus.RAF.getValue());
                }
            }

            friendInfo.area = target.getZone();
            friendInfo.level = target.getLevel();
            friendInfo.class = target.getClass();
        }
    }


    public final void sendFriendStatus(Player player, FriendsResult result, ObjectGuid friendGuid) {
        sendFriendStatus(player, result, friendGuid, false);
    }

    public final void sendFriendStatus(Player player, FriendsResult result, ObjectGuid friendGuid, boolean broadcast) {
        FriendInfo fi = new FriendInfo();
        getFriendInfo(player, friendGuid, fi);

        FriendStatusPkt friendStatus = new FriendStatusPkt();
        friendStatus.initialize(friendGuid, result, fi);

        if (broadcast) {
            broadcastToFriendListers(player, friendStatus);
        } else {
            player.sendPacket(friendStatus);
        }
    }

    public final PlayerSocial loadFromDB(SQLResult result, ObjectGuid guid) {
        PlayerSocial social = new PlayerSocial();
        social.setPlayerGUID(guid);

        if (!result.isEmpty()) {
            do {
                var friendGuid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(0));
                var friendAccountGuid = ObjectGuid.create(HighGuid.wowAccount, result.<Integer>Read(1));
                var flags = SocialFlag.forValue(result.<Byte>Read(2));

                social.playerSocialMap.put(friendGuid, new FriendInfo(friendAccountGuid, flags, result.<String>Read(3)));

                if (flags.hasFlag(SocialFlag.Ignored)) {
                    social.ignoredAccounts.add(friendAccountGuid);
                }
            } while (result.NextRow());
        }

        socialMap.put(guid, social);

        return social;
    }

    public final void removePlayerSocial(ObjectGuid guid) {
        socialMap.remove(guid);
    }

    private void broadcastToFriendListers(Player player, ServerPacket packet) {
        if (!player) {
            return;
        }

        var gmSecLevel = AccountTypes.forValue(WorldConfig.getIntValue(WorldCfg.GmLevelInWhoList));

        for (var pair : socialMap.entrySet()) {
            var info = pair.getValue().playerSocialMap.get(player.getGUID());

            if (info != null && info.flags.hasFlag(SocialFlag.Friend)) {
                var target = global.getObjAccessor().findPlayer(pair.getKey());

                if (!target || !target.isInWorld()) {
                    continue;
                }

                var session = target.getSession();

                if (!session.hasPermission(RBACPermissions.WhoSeeAllSecLevels) && player.getSession().getSecurity().getValue() > gmSecLevel.getValue()) {
                    continue;
                }

                if (target.getTeam() != player.getTeam() && !session.hasPermission(RBACPermissions.TwoSideWhoList)) {
                    continue;
                }

                if (player.isVisibleGloballyFor(target)) {
                    session.sendPacket(packet);
                }
            }
        }
    }
}
