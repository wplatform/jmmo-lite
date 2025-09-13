package com.github.azeroth.game.entity.object;

import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.Builder;

@Builder
public final class FindCreatureOptions {
    public Integer creatureId = null;
    public String stringId;
    public Boolean isAlive = null;
    public Boolean isInCombat = null;
    public Boolean isSummon = null;
    public boolean ignorePhases;
    public boolean ignoreNotOwnedPrivateObjects;
    public boolean ignorePrivateObjects;
    public Integer auraSpellId = null;
    public ObjectGuid ownerGuid = null;
    public ObjectGuid charmerGuid = null;
    public ObjectGuid creatorGuid = null;
    public ObjectGuid demonCreatorGuid = null;
    public ObjectGuid privateObjectOwnerGuid = null;

}
