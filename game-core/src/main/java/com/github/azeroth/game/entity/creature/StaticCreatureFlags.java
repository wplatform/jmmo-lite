package com.github.azeroth.game.entity.creature;

import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.creature.*;

public class StaticCreatureFlags {

    private EnumFlag<CreatureStaticFlag> flags;
    private EnumFlag<CreatureStaticFlag2> flags2;
    private EnumFlag<CreatureStaticFlag3> flags3;
    private EnumFlag<CreatureStaticFlag4> flags4;
    private EnumFlag<CreatureStaticFlag5> flags5;
    private EnumFlag<CreatureStaticFlag6> flags6;
    private EnumFlag<CreatureStaticFlag7> flags7;
    private EnumFlag<CreatureStaticFlag8> flags8;

    boolean hasFlag(CreatureStaticFlag flag) { return flags.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag2 flag) { return flags2.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag3 flag) { return flags3.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag4 flag) { return flags4.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag5 flag) { return flags5.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag6 flag) { return flags6.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag7 flag) { return flags7.hasFlag(flag); }
    boolean hasFlag(CreatureStaticFlag8 flag) { return flags8.hasFlag(flag); }

    void applyFlag(CreatureStaticFlag flag, boolean apply) { if (apply) flags.addFlag(flag); else flags.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag2 flag, boolean apply) { if (apply) flags2.addFlag(flag); else flags2.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag3 flag, boolean apply) { if (apply) flags3.addFlag(flag); else flags3.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag4 flag, boolean apply) { if (apply) flags4.addFlag(flag); else flags4.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag5 flag, boolean apply) { if (apply) flags5.addFlag(flag); else flags5.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag6 flag, boolean apply) { if (apply) flags6.addFlag(flag); else flags6.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag7 flag, boolean apply) { if (apply) flags7.addFlag(flag); else flags7.removeFlag(flag); }
    void applyFlag(CreatureStaticFlag8 flag, boolean apply) { if (apply) flags8.addFlag(flag); else flags8.removeFlag(flag); }
}
