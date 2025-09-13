package com.github.azeroth.game.chat;


import java.util.Scanner;
import java.util.TreeMap;


public class CommandManager {
    private static final TreeMap<String, ChatCommandNode> COMMANDS = new TreeMap<String, ChatCommandNode>();

    static {
        for (var type : Assembly.GetExecutingAssembly().GetTypes()) {
            if (type.Attributes.hasFlag(TypeAttributes.NestedPrivate.getValue() | TypeAttributes.NestedPublic.getValue())) {
                continue;
            }

            var groupAttribute = type.<CommandGroupAttribute>GetCustomAttribute(true);

            if (groupAttribute != null) {
                ChatCommandNode command = new ChatCommandNode(groupAttribute);
                buildSubCommandsForCommand(command, type);
                COMMANDS.put(groupAttribute.name, command);
            }

            //This check for any command not part of that group,  but saves us from having to add them into a new class.
            for (var method : type.GetMethods(BindingFlags.Static.getValue() | BindingFlags.NonPublic.getValue())) {
                var commandAttribute = method.<CommandNonGroupAttribute>GetCustomAttribute(true);

                if (commandAttribute != null) {
                    COMMANDS.put(commandAttribute.name, new ChatCommandNode(commandAttribute, method));
                }
            }
        }

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_COMMANDS);
        var result = DB.World.query(stmt);

        if (!result.isEmpty()) {
            do {
                var name = result.<String>Read(0);
                var help = result.<String>Read(1);

                ChatCommandNode cmd = null;
                var map = COMMANDS;

                for (var key : name.split(' ', StringSplitOptions.RemoveEmptyEntries)) {
                    var it = map.get(key);

                    if (it != null) {
                        cmd = it;
                        map = cmd.subCommands;
                    } else {
                        Logs.SQL.error(String.format("Table `command` contains data for non-existant command '%1$s'. Skipped.", name));
                        cmd = null;

                        break;
                    }
                }

                if (cmd == null) {
                    continue;
                }

                if (!cmd.helpText.isEmpty()) {
                    Logs.SQL.error(String.format("Table `command` contains duplicate data for command '%1$s'. Skipped.", name));
                }

                if (cmd.helpString == 0) {
                    cmd.helpText = help;
                } else {
                    Logs.SQL.error(String.format("Table `command` contains legacy help text for command '%1$s', which uses `trinity_string`. Skipped.", name));
                }
            } while (result.NextRow());
        }


        for (var(name, cmd) : COMMANDS) {
            cmd.resolveNames(name);
        }
    }

    public static TreeMap<String, ChatCommandNode> getCommands() {
        return COMMANDS;
    }

    public static void initConsole() {
        if (ConfigMgr.GetDefaultValue("BeepAtStart", true)) {
            Console.Beep();
        }

        Console.ForegroundColor = ConsoleColor.Green;
        system.out.print("Forged>> ");

        var handler = new ConsoleHandler();

        while (!global.getWorldMgr().isStopped) {
            handler.parseCommands(new Scanner(system.in).nextLine());
            Console.ForegroundColor = ConsoleColor.Green;
            system.out.print("Forged>> ");
        }
    }

    private static void buildSubCommandsForCommand(ChatCommandNode command, Class type) {
        for (var nestedType : type.getClasses(BindingFlags.NonPublic)) {
            var groupAttribute = nestedType.<CommandGroupAttribute>GetCustomAttribute(true);

            if (groupAttribute == null) {
                continue;
            }

            ChatCommandNode subCommand = new ChatCommandNode(groupAttribute);
            buildSubCommandsForCommand(subCommand, nestedType);
            command.addSubCommand(subCommand);
        }

        for (var method : type.getMethods(BindingFlags.Static.getValue() | BindingFlags.NonPublic.getValue())) {
            var commandAttributes = method.<CommandAttribute>GetCustomAttributes(false).ToList();

            if (commandAttributes.isEmpty()) {
                continue;
            }

            for (var commandAttribute : commandAttributes) {
                if (commandAttribute.getClass() == CommandNonGroupAttribute.class) {
                    continue;
                }

                command.addSubCommand(new ChatCommandNode(commandAttribute, method));
            }
        }
    }
}
