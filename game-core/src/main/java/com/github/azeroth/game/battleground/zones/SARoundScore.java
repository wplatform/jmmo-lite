package com.github.azeroth.game.battleground.zones;


final class SARoundScore {

    public int winner;

    public int time;

    public SARoundScore clone() {
        SARoundScore varCopy = new SARoundScore();

        varCopy.winner = this.winner;
        varCopy.time = this.time;

        return varCopy;
    }
}
