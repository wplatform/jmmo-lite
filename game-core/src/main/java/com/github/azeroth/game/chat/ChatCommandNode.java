package com.github.azeroth.game.chat;


import game.*;

import java.util.TreeMap;


public class ChatCommandNode {
    public final TreeMap<String, ChatCommandNode> subCommands = new TreeMap<String, ChatCommandNode>();
    public String name;
    public Commandpermissions permission = new commandPermissions();
    public String helpText;
    public SysMessage helpString = SysMessage.values()[0];

    private java.lang.reflect.Method methodInfo;
    private ParameterInfo[] parameters;

    public ChatCommandNode(CommandAttribute attribute) {
        name = attribute.getName();
        permission = new commandPermissions(attribute.getRBAC(), attribute.getAllowConsole());
        helpString = attribute.getHelp();
    }

    public ChatCommandNode(CommandAttribute attribute, java.lang.reflect.Method methodInfo) {
        this(attribute);
        methodInfo = methodInfo;
        parameters = methodInfo.GetParameters();
    }

    public static boolean tryExecuteCommand(CommandHandler handler, String cmdStr) {
        ChatCommandNode cmd = null;
        var map = CommandManager.getCommands();

        cmdStr = tangible.StringHelper.trim(cmdStr, ' ');

        var oldTail = cmdStr;

        while (!oldTail.isEmpty()) {
            /* oldTail = token DELIMITER newTail */

            var(token, newTail) = oldTail.Tokenize();

            if (token.isEmpty()) {
                return false;
            }

            var listOfPossibleCommands = map.stream().filter(p -> p.key.startsWith(token) && p.value.isVisible(handler)).collect(Collectors.toList());

            if (listOfPossibleCommands.isEmpty()) {
                break; // no matching subcommands found
            }

            if (!listOfPossibleCommands.get(0).key.Equals(token, StringComparison.OrdinalIgnoreCase)) {
                /* ok, so it1 points at a partially matching subcommand - let's see if there are others */
                if (listOfPossibleCommands.size() > 1) {
                    /* there are multiple matching subcommands - print possibilities and return */
                    if (cmd != null) {
                        handler.sendSysMessage(SysMessage.SubcmdAmbiguous, cmd.name, ' ', token);
                    } else {
                        handler.sendSysMessage(SysMessage.CmdAmbiguous, token);
                    }

                    handler.sendSysMessage(listOfPossibleCommands.get(0).value.hasVisibleSubCommands(handler) ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, listOfPossibleCommands.get(0).key);


                    for (var(name, command) : listOfPossibleCommands) {
                        handler.sendSysMessage(command.hasVisibleSubCommands(handler) ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, name);
                    }

                    return true;
                }
            }

            /* now we matched exactly one subcommand, and it1 points to it; go down the rabbit hole */
            cmd = listOfPossibleCommands.get(0).value;
            map = cmd.subCommands;

            oldTail = newTail;
        }

        if (cmd != null) {
            /* if we matched a command at some point, invoke it */
            handler.setSentErrorMessage(false);

            if (cmd.isInvokerVisible(handler) && cmd.invoke(handler, oldTail)) {
                /* invocation succeeded, log this */
                if (!handler.isConsole()) {
                    logCommandUsage(handler.getSession(), (int) cmd.permission.requiredPermission.getValue(), cmdStr);
                }
            } else if (!handler.getHasSentErrorMessage()) {
                /* invocation failed, we should show usage */
                cmd.sendCommandHelp(handler);
            }

            return true;
        }

        return false;
    }

    public static void sendCommandHelpFor(CommandHandler handler, String cmdStr) {
        ChatCommandNode cmd = null;
        var map = CommandManager.getCommands();

        for (var token : cmdStr.split(' ', StringSplitOptions.RemoveEmptyEntries)) {
            var listOfPossibleCommands = map.stream().filter(p -> p.key.startsWith(token) && p.value.isVisible(handler)).collect(Collectors.toList());

            if (listOfPossibleCommands.isEmpty()) {
                /* no matching subcommands found */
                if (cmd != null) {
                    cmd.sendCommandHelp(handler);
                    handler.sendSysMessage(SysMessage.SubcmdInvalid, cmd.name, ' ', token);
                } else {
                    handler.sendSysMessage(SysMessage.CmdInvalid, token);
                }

                return;
            }

            if (!listOfPossibleCommands.get(0).key.Equals(token, StringComparison.OrdinalIgnoreCase)) {
                /* ok, so it1 points at a partially matching subcommand - let's see if there are others */
                if (listOfPossibleCommands.size() > 1) {
                    /* there are multiple matching subcommands - print possibilities and return */
                    if (cmd != null) {
                        handler.sendSysMessage(SysMessage.SubcmdAmbiguous, cmd.name, ' ', token);
                    } else {
                        handler.sendSysMessage(SysMessage.CmdAmbiguous, token);
                    }

                    handler.sendSysMessage(listOfPossibleCommands.get(0).value.hasVisibleSubCommands(handler) ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, listOfPossibleCommands.get(0).key);


                    for (var(name, command) : listOfPossibleCommands) {
                        handler.sendSysMessage(command.hasVisibleSubCommands(handler) ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, name);
                    }

                    return;
                }
            }

            cmd = listOfPossibleCommands.get(0).value;
            map = cmd.subCommands;
        }

        if (cmd != null) {
            cmd.sendCommandHelp(handler);
        } else if (cmdStr.isEmpty()) {
            handler.sendSysMessage(SysMessage.AvailableCmds);


            for (var(name, command) : map) {
                if (!command.isVisible(handler)) {
                    continue;
                }

                handler.sendSysMessage(command.hasVisibleSubCommands(handler) ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, name);
            }
        } else {
            handler.sendSysMessage(SysMessage.CmdInvalid, cmdStr);
        }
    }

    private static void logCommandUsage(WorldSession session, int permission, String cmdStr) {
        if (global.getAccountMgr().isPlayerAccount(session.getSecurity())) {
            return;
        }

        if (global.getAccountMgr().getRBACPermission(new integer(RBACPermissions.RolePlayer)).getLinkedPermissions().contains(permission)) {
            return;
        }

        var player = session.getPlayer();
        var targetGuid = player.getTarget();
        var areaId = player.getArea();
        var areaName = "Unknown";
        var zoneName = "Unknown";

        var area = CliDB.AreaTableStorage.get(areaId);

        if (area != null) {
            var locale = session.getSessionDbcLocale();
            areaName = area.AreaName[locale];
            var zone = CliDB.AreaTableStorage.get(area.ParentAreaID);

            if (zone != null) {
                zoneName = zone.AreaName[locale];
            }
        }

        Log.outCommand(session.getAccountId(), String.format("Command: %1$s [Player: %2$s (%3$s) (Account: %4$s) ", cmdStr, player.getName(), player.getGUID(), session.getAccountId()) + String.format("X: %1$s Y: %2$s Z: %3$s Map: %4$s (%5$s) ", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getMapId(), (player.getMap() ? player.getMap().getMapName() : "Unknown")) + String.format("Area: %1$s (%2$s) Zone: %3$s Selected: %4$s (%5$s)]", areaId, areaName, zoneName, (player.getSelectedUnit() ? player.getSelectedUnit().getName() : ""), targetGuid));
    }

    public final void resolveNames(String name) {
        if (methodInfo != null && (helpText.isEmpty() && helpString == 0)) {
            Log.outWarn(LogFilter.Sql, String.format("Table `command` is missing help text for command '%1$s'.", name));
        }

        name = name;


        for (var(subToken, cmd) : subCommands) {
            cmd.resolveNames(String.format("%1$s %2$s", name, subToken));
        }
    }

    public final void sendCommandHelp(CommandHandler handler) {
        var hasInvoker = isInvokerVisible(handler);

        if (hasInvoker) {
            if (helpString != 0) {
                handler.sendSysMessage(helpString);
            } else if (!helpText.isEmpty()) {
                handler.sendSysMessage(helpText);
            } else {
                handler.sendSysMessage(SysMessage.CmdHelpGeneric, name);
                handler.sendSysMessage(SysMessage.CmdNoHelpAvailable, name);
            }
        }

        var header = false;


        for (var(_, command) : subCommands) {
            var subCommandHasSubCommand = command.hasVisibleSubCommands(handler);

            if (!subCommandHasSubCommand && !command.isInvokerVisible(handler)) {
                continue;
            }

            if (!header) {
                if (!hasInvoker) {
                    handler.sendSysMessage(SysMessage.CmdHelpGeneric, name);
                }

                handler.sendSysMessage(SysMessage.SubcmdsList);
                header = true;
            }

            handler.sendSysMessage(subCommandHasSubCommand ? SysMessage.SubcmdsListEntryEllipsis : SysMessage.SubcmdsListEntry, command.name);
        }
    }

    public final void addSubCommand(ChatCommandNode command) {
        if (command.name.isEmpty()) {
            permission = command.permission;
            helpText = command.helpText;
            helpString = command.helpString;
            methodInfo = command.methodInfo;
            parameters = command.parameters;
        } else {
            if (!subCommands.TryAdd(command.name, command)) {
                Log.outError(LogFilter.Commands, String.format("Error trying to add subcommand, Already exists Command: %1$s SubCommand: %2$s", name, command.name));
            }
        }
    }

    public final boolean invoke(CommandHandler handler, String args) {
        if (parameters.Any(p -> p.ParameterType == StringArguments.class)) //Old system, can remove once all commands are changed.
        {
            return (Boolean) methodInfo.invoke(null, new Object[]{handler, new StringArguments(args)});
        } else {

            var parseArgs = new dynamic[_parameters.length];
            parseArgs[0] = handler;
            var result = CommandArgs.consumeFromOffset(parseArgs, 1, parameters, handler, args);

            if (result.isSuccessful()) {
                return (Boolean) methodInfo.invoke(null, parseArgs);
            } else {
                if (result.getHasErrorMessage()) {
                    handler.sendSysMessage(result.getErrorMessage());
                    handler.setSentErrorMessage(true);
                }

                return false;
            }
        }
    }

    private boolean isInvokerVisible(CommandHandler who) {
        if (methodInfo == null) {
            return false;
        }

        if (who.isConsole() && !permission.allowConsole) {
            return false;
        }

        return who.hasPermission(permission.requiredPermission);
    }

    private boolean hasVisibleSubCommands(CommandHandler who) {

        for (var(_, command) : subCommands) {
            if (command.isVisible(who)) {
                return true;
            }
        }

        return false;
    }

    private boolean isVisible(CommandHandler who) {
        return isInvokerVisible(who) || hasVisibleSubCommands(who);
    }
}
