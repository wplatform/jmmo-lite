package com.github.azeroth.game.group;


import com.github.azeroth.defines.Race;
import com.github.azeroth.game.domain.object.ObjectGuid;

public class MemberSlot {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public String name;
    public Race race = Framework.Constants.race.values()[0];
    public byte class;
    public byte group;
    public GroupMemberflags flags = GroupMemberFlags.values()[0];
    public Lfgroles roles = LfgRoles.values()[0];
    public boolean readyChecked;
}
