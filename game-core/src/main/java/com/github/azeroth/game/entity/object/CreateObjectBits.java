package com.github.azeroth.game.entity.object;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateObjectBits {
    public boolean noBirthAnim;
    public boolean enablePortals;
    public boolean playHoverAnim;
    public boolean movementUpdate;
    public boolean movementTransport;
    public boolean stationary;
    public boolean combatVictim;
    public boolean serverTime;
    public boolean vehicle;
    public boolean animKit;
    public boolean rotation;
    public boolean areaTrigger;
    public boolean gameObject;
    public boolean smoothPhasing;
    public boolean thisIsYou;
    public boolean sceneObject;
    public boolean activePlayer;
    public boolean conversation;

    public CreateObjectBits(CreateObjectBits bits) {
        this.noBirthAnim = bits.noBirthAnim;
        this.enablePortals = bits.enablePortals;
        this.playHoverAnim = bits.playHoverAnim;
        this.movementUpdate = bits.movementUpdate;
        this.movementTransport = bits.movementTransport;
        this.stationary = bits.stationary;
        this.combatVictim = bits.combatVictim;
        this.serverTime = bits.serverTime;
        this.vehicle = bits.vehicle;
        this.animKit = bits.animKit;
        this.rotation = bits.rotation;
        this.areaTrigger = bits.areaTrigger;
        this.gameObject = bits.gameObject;
        this.smoothPhasing = bits.smoothPhasing;
        this.thisIsYou = bits.thisIsYou;
        this.sceneObject = bits.sceneObject;
        this.activePlayer = bits.activePlayer;
        this.conversation = bits.conversation;
    }
}
