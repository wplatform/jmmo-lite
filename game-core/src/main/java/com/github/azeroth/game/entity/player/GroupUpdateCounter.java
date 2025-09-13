package com.github.azeroth.game.entity.player;

final class GroupUpdateCounter {
    public ObjectGuid groupGuid = ObjectGuid.EMPTY;
    public int updateSequenceNumber;

    public GroupUpdateCounter clone() {
        GroupUpdateCounter varCopy = new GroupUpdateCounter();

        varCopy.groupGuid = this.groupGuid;
        varCopy.updateSequenceNumber = this.updateSequenceNumber;

        return varCopy;
    }
}
