package com.github.azeroth.game.chat;


public final class CommandPermissions {
    public RBACPermissions requiredPermission = RBACPermissions.values()[0];
    public boolean allowConsole;

    public commandPermissions() {
    }

    public commandPermissions(RBACPermissions perm, boolean allowConsole) {
        requiredPermission = perm;
        allowConsole = allowConsole;
    }

    public CommandPermissions clone() {
        CommandPermissions varCopy = new commandPermissions();

        varCopy.requiredPermission = this.requiredPermission;
        varCopy.allowConsole = this.allowConsole;

        return varCopy;
    }
}
