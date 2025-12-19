package com.github.azeroth.game.ai;


import java.time.Duration;

public class AISpellInfoType {

    int AI_DEFAULT_COOLDOWN = 5000;


    public AITarget target;
    public AICondition condition;
    public Duration cooldown;
    public Duration realCooldown;
    public float maxRange;
    public byte targets;
    public byte effects;

    public AISpellInfoType() {
        target = AITarget.SELF;
        condition = AICondition.COMBAT;
        cooldown = Duration.ofMillis(AI_DEFAULT_COOLDOWN);
    }
}