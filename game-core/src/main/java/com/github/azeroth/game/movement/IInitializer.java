package com.github.azeroth.game.movement;


public interface IInitializer<T> {
    int invoke(spline<T> s, int i);
}
