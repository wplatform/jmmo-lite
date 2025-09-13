package com.github.azeroth.game.networking.packet.combatlog;

public final class UnkAttackerState {
    public int state1;
    public float state2;
    public float state3;
    public float state4;
    public float state5;
    public float state6;
    public float state7;
    public float state8;
    public float state9;
    public float state10;
    public float state11;
    public int state12;

    public UnkAttackerState clone() {
        UnkAttackerState varCopy = new unkAttackerState();

        varCopy.state1 = this.state1;
        varCopy.state2 = this.state2;
        varCopy.state3 = this.state3;
        varCopy.state4 = this.state4;
        varCopy.state5 = this.state5;
        varCopy.state6 = this.state6;
        varCopy.state7 = this.state7;
        varCopy.state8 = this.state8;
        varCopy.state9 = this.state9;
        varCopy.state10 = this.state10;
        varCopy.state11 = this.state11;
        varCopy.state12 = this.state12;

        return varCopy;
    }
}
