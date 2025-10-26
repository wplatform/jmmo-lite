package com.github.azeroth.game.entity.player;


import com.github.azeroth.defines.UnitClass;
import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendInfo {
    public ObjectGuid wowAccountGuid = ObjectGuid.EMPTY;
    public FriendStatus status = FriendStatus.values()[0];
    public SocialFlag flags = SocialFlag.values()[0];
    public int area;
    public int level;
    public UnitClass playerClass;
    public String note;

}
