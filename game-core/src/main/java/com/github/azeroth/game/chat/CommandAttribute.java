package com.github.azeroth.game.chat;



public class CommandAttribute extends Attribute {
    /**
     * Command's name.
     */
    private String name;
    /**
     * Help String for command.
     */
    private SysMessage help = SysMessage.values()[0];
    /**
     * Allow Console?
     */
    private boolean allowConsole;
    /**
     * Minimum user level required to invoke the command.
     */
    private RBACPermissions RBAC = RBACPermissions.values()[0];

    public CommandAttribute(String command) {
        setName(command.toLowerCase());
    }

    public CommandAttribute(String command, RBACPermissions rbac) {
        this(command, rbac, false);
    }

    public CommandAttribute(String command, RBACPermissions rbac, boolean allowConsole) {
        setName(command.toLowerCase());
        setRBAC(rbac);
        setAllowConsole(allowConsole);
    }

    public CommandAttribute(String command, SysMessage help, RBACPermissions rbac) {
        this(command, help, rbac, false);
    }

    public CommandAttribute(String command, SysMessage help, RBACPermissions rbac, boolean allowConsole) {
        setName(command.toLowerCase());
        setHelp(help);
        setRBAC(rbac);
        setAllowConsole(allowConsole);
    }

    public final String getName() {
        return name;
    }

    private void setName(String value) {
        name = value;
    }

    public final SysMessage getHelp() {
        return help;
    }

    public final void setHelp(SysMessage value) {
        help = value;
    }

    public final boolean getAllowConsole() {
        return allowConsole;
    }

    private void setAllowConsole(boolean value) {
        allowConsole = value;
    }

    public final RBACPermissions getRBAC() {
        return RBAC;
    }

    public final void setRBAC(RBACPermissions value) {
        RBAC = value;
    }
}
