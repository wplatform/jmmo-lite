package com.github.azeroth.game.chat;



public class CommandNonGroupAttribute extends CommandAttribute {

    public CommandNonGroupAttribute(String command, SysMessage help, RBACPermissions rbac) {
        this(command, help, rbac, false);
    }

    public CommandNonGroupAttribute(String command, SysMessage help, RBACPermissions rbac, boolean allowConsole) {
        super(command, help, rbac, allowConsole);
    }

    public CommandNonGroupAttribute(String command, RBACPermissions rbac) {
        this(command, rbac, false);
    }

    public CommandNonGroupAttribute(String command, RBACPermissions rbac, boolean allowConsole) {
        super(command, rbac, allowConsole);
    }
}
