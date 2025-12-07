package game.ai;

import Framework.Constants.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class AISpellInfoType {
    public AITarget target = AITarget.values()[0];
    public AICondition condition = AICondition.values()[0];
    public TimeSpan cooldown = new TimeSpan();
    public TimeSpan realCooldown = new TimeSpan();
    public float maxRange;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte Targets;
    public byte targets; // set of enum SelectTarget
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte Effects;
    public byte effects; // set of enum SelectEffect

    public AISpellInfoType() {
        target = AITarget.Self;
        condition = AICondition.Combat;
        cooldown = TimeSpan.FromMilliseconds(SharedConst.AIDefaultCooldown);
    }
}