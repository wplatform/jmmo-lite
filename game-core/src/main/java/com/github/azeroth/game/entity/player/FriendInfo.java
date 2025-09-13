package com.github.azeroth.game.entity.player;


public class FriendInfo {
    public ObjectGuid wowAccountGuid = ObjectGuid.EMPTY;
    public Friendstatus status = FriendStatus.values()[0];
    public SocialFlag flags = SocialFlag.values()[0];
    public int area;
    public int level;
    public Playerclass class =playerClass.values()[0];
    public String note;

    public FriendInfo() {
        status = FriendStatus.Offline;
        note = "";
    }

    public FriendInfo(ObjectGuid accountGuid, SocialFlag flags, String note) {
        wowAccountGuid = accountGuid;
        status = FriendStatus.Offline;
        flags = flags;
        note = note;
    }
}
