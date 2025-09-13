package com.github.azeroth.game.chat;


@FunctionalInterface
public interface HandleCommandDelegate {
    boolean invoke(CommandHandler handler, StringArguments args);
}
