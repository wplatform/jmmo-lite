package com.github.azeroth.game.ai;


public class AISpellInfoType {
    public AItarget target = AITarget.values()[0];
    public AIcondition condition = AICondition.values()[0];
    public duration cooldown = new duration();
    public Duration realCooldown = new duration();
    public float maxRange;

    public byte targets; // set of enum SelectTarget
    public byte effects; // set of enum SelectEffect

    public AISpellInfoType() {
        target = AITarget.Self;
        condition = AICondition.Combat;
        cooldown = duration.ofSeconds(SharedConst.AIDefaultCooldown);
    }
}
