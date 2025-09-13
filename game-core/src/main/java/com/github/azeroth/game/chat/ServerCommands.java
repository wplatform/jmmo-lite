package com.github.azeroth.game.chat;


import java.util.Objects;



class ServerCommands {

    private static boolean handleServerCorpsesCommand(CommandHandler handler) {
        global.getWorldMgr().removeOldCorpses();

        return true;
    }


    private static boolean handleServerDebugCommand(CommandHandler handler) {
        return false; //todo fix me
    }


    private static boolean handleServerExitCommand(CommandHandler handler) {
        handler.sendSysMessage(SysMessage.CommandExit);
        global.getWorldMgr().stopNow(ShutdownExitCode.Shutdown);

        return true;
    }


    private static boolean handleServerInfoCommand(CommandHandler handler) {
        var playersNum = global.getWorldMgr().getPlayerCount();
        var maxPlayersNum = global.getWorldMgr().getMaxPlayerCount();
        var activeClientsNum = global.getWorldMgr().getActiveSessionCount();
        var queuedClientsNum = global.getWorldMgr().getQueuedSessionCount();
        var maxActiveClientsNum = global.getWorldMgr().getMaxActiveSessionCount();
        var maxQueuedClientsNum = global.getWorldMgr().getMaxQueuedSessionCount();
        var uptime = time.secsToTimeString(gameTime.GetUptime(), 0, false);
        var updateTime = global.getWorldMgr().getWorldUpdateTime().getLastUpdateTime();

        handler.sendSysMessage(SysMessage.ConnectedPlayers, playersNum, maxPlayersNum);
        handler.sendSysMessage(SysMessage.ConnectedUsers, activeClientsNum, maxActiveClientsNum, queuedClientsNum, maxQueuedClientsNum);
        handler.sendSysMessage(SysMessage.Uptime, uptime);
        handler.sendSysMessage(SysMessage.UpdateDiff, updateTime);

        // Can't use global.WorldMgr.ShutdownMsg here in case of console command
        if (global.getWorldMgr().isShuttingDown()) {
            handler.sendSysMessage(SysMessage.ShutdownTimeleft, time.secsToTimeString(global.getWorldMgr().getShutDownTimeLeft(), 0, false));
        }

        return true;
    }


    private static boolean handleServerMotdCommand(CommandHandler handler) {
        var motd = "";

        for (var line : global.getWorldMgr().getMotd()) {
            motd += line;
        }

        handler.sendSysMessage(SysMessage.MotdCurrent, motd);

        return true;
    }


    private static boolean handleServerPLimitCommand(CommandHandler handler, StringArguments args) {
        if (!args.isEmpty()) {
            var paramStr = args.NextString(" ");

            if (StringUtil.isEmpty(paramStr)) {
                return false;
            }

            switch (paramStr.toLowerCase()) {
                case "player":
                    global.getWorldMgr().setPlayerSecurityLimit(AccountTypes.player);

                    break;
                case "moderator":
                    global.getWorldMgr().setPlayerSecurityLimit(AccountTypes.moderator);

                    break;
                case "gamemaster":
                    global.getWorldMgr().setPlayerSecurityLimit(AccountTypes.GameMaster);

                    break;
                case "administrator":
                    global.getWorldMgr().setPlayerSecurityLimit(AccountTypes.Administrator);

                    break;
                case "reset":
                    global.getWorldMgr().setPlayerAmountLimit(ConfigMgr.<Integer>GetDefaultValue("PlayerLimit", 100));
                    global.getWorldMgr().loadDBAllowedSecurityLevel();

                    break;
                default:
                    int value;
                    tangible.OutObject<Integer> tempOut_value = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(paramStr, tempOut_value)) {
                        value = tempOut_value.outArgValue;
                        return false;
                    } else {
                        value = tempOut_value.outArgValue;
                    }

                    if (value < 0) {
                        global.getWorldMgr().setPlayerSecurityLimit(AccountTypes.forValue(-value));
                    } else {
                        global.getWorldMgr().setPlayerAmountLimit((int) value);
                    }

                    break;
            }
        }

        var playerAmountLimit = global.getWorldMgr().getPlayerAmountLimit();
        var allowedAccountType = global.getWorldMgr().getPlayerSecurityLimit();
        String secName;

        switch (allowedAccountType) {
            case Player:
                secName = "Player";

                break;
            case Moderator:
                secName = "Moderator";

                break;
            case GameMaster:
                secName = "Gamemaster";

                break;
            case Administrator:
                secName = "Administrator";

                break;
            default:
                secName = "<unknown>";

                break;
        }

        handler.sendSysMessage("Player limits: amount {0}, min. security level {1}.", playerAmountLimit, secName);

        return true;
    }

    private static boolean isOnlyUser(WorldSession mySession) {
        // check if there is any session connected from a different address
        var myAddr = mySession ? mySession.getRemoteAddress() : "";
        var sessions = global.getWorldMgr().getAllSessions();

        for (var session : sessions) {
            if (session && !Objects.equals(myAddr, session.getRemoteAddress())) {
                return false;
            }
        }

        return true;
    }

    private static boolean parseExitCode(String exitCodeStr, tangible.OutObject<Integer> exitCode) {
        if (!tangible.TryParseHelper.tryParseInt(exitCodeStr, exitCode)) {
            return false;
        }

        // Handle atoi() errors
        if (exitCode.outArgValue == 0 && (exitCodeStr.charAt(0) != '0' || (exitCodeStr.length() > 1 && exitCodeStr.charAt(1) != '\0'))) {
            return false;
        }

        // Exit code should be in range of 0-125, 126-255 is used
        // in many shells for their own return codes and code > 255
        // is not supported in many others
        if (exitCode.outArgValue < 0 || exitCode.outArgValue > 125) {
            return false;
        }

        return true;
    }

    private static boolean shutdownServer(StringArguments args, CommandHandler handler, ShutdownMask shutdownMask, ShutdownExitCode defaultExitCode) {
        if (args.isEmpty()) {
            return false;
        }

        var delayStr = args.NextString(" ");

        if (delayStr.isEmpty()) {
            return false;
        }

        int delay;
        tangible.OutObject<Integer> tempOut_delay = new tangible.OutObject<Integer>();
        if (tangible.TryParseHelper.tryParseInt(delayStr, tempOut_delay)) {
            delay = tempOut_delay.outArgValue;
            //  Prevent interpret wrong arg second as 0 secs shutdown time
            if ((delay == 0 && (delayStr.charAt(0) != '0' || delayStr.length() > 1 && delayStr.charAt(1) != '\0')) || delay < 0) {
                return false;
            }
        } else {
            delay = tempOut_delay.outArgValue;
            delay = (int) time.TimeStringToSecs(delayStr);

            if (delay == 0) {
                return false;
            }
        }

        var reason = "";
        var exitCodeStr = "";
        String nextToken;

        while (!(nextToken = args.NextString(" ")).isEmpty()) {
            if (nextToken.IsNumber()) {
                exitCodeStr = nextToken;
            } else {
                reason = nextToken;
                reason += args.NextString("\0");

                break;
            }
        }

        var exitCode = defaultExitCode.getValue();

        if (!exitCodeStr.isEmpty()) {
            tangible.OutObject<Integer> tempOut_exitCode = new tangible.OutObject<Integer>();
            if (!parseExitCode(exitCodeStr, tempOut_exitCode)) {
                exitCode = tempOut_exitCode.outArgValue;
                return false;
            } else {
                exitCode = tempOut_exitCode.outArgValue;
            }
        }

        // Override parameter "delay" with the configuration second if there are still players connected and "force" parameter was not specified
        if (delay < WorldConfig.getIntValue(WorldCfg.ForceShutdownThreshold) && !shutdownMask.hasFlag(ShutdownMask.FORCE) && !isOnlyUser(handler.getSession())) {
            delay = WorldConfig.getIntValue(WorldCfg.ForceShutdownThreshold);
            handler.sendSysMessage(SysMessage.ShutdownDelayed, delay);
        }

        global.getWorldMgr().shutdownServ((int) delay, shutdownMask, ShutdownExitCode.forValue(exitCode), reason);

        return true;
    }


    private static class IdleRestartCommands {

        private static boolean handleServerIdleRestartCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, ShutdownMask.RESTART.getValue() | ShutdownMask.IDLE.getValue(), ShutdownExitCode.RESTART);
        }


        private static boolean handleServerShutDownCancelCommand(CommandHandler handler) {
            var timer = global.getWorldMgr().shutdownCancel();

            if (timer != 0) {
                handler.sendSysMessage(SysMessage.ShutdownCancelled, timer);
            }

            return true;
        }
    }


    private static class IdleshutdownCommands {

        private static boolean handleServerIdleShutDownCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, ShutdownMask.IDLE, ShutdownExitCode.Shutdown);
        }


        private static boolean handleServerShutDownCancelCommand(CommandHandler handler) {
            var timer = global.getWorldMgr().shutdownCancel();

            if (timer != 0) {
                handler.sendSysMessage(SysMessage.ShutdownCancelled, timer);
            }

            return true;
        }
    }


    private static class RestartCommands {

        private static boolean handleServerRestartCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, ShutdownMask.RESTART, ShutdownExitCode.RESTART);
        }


        private static boolean handleServerShutDownCancelCommand(CommandHandler handler) {
            var timer = global.getWorldMgr().shutdownCancel();

            if (timer != 0) {
                handler.sendSysMessage(SysMessage.ShutdownCancelled, timer);
            }

            return true;
        }


        private static boolean handleServerForceRestartCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, ShutdownMask.FORCE.getValue() | ShutdownMask.RESTART.getValue(), ShutdownExitCode.RESTART);
        }
    }


    private static class ShutdownCommands {

        private static boolean handleServerShutDownCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, 0, ShutdownExitCode.Shutdown);
        }


        private static boolean handleServerShutDownCancelCommand(CommandHandler handler) {
            var timer = global.getWorldMgr().shutdownCancel();

            if (timer != 0) {
                handler.sendSysMessage(SysMessage.ShutdownCancelled, timer);
            }

            return true;
        }


        private static boolean handleServerForceShutDownCommand(CommandHandler handler, StringArguments args) {
            return shutdownServer(args, handler, ShutdownMask.FORCE, ShutdownExitCode.Shutdown);
        }
    }


    private static class SetCommands {

        private static boolean handleServerSetDiffTimeCommand(CommandHandler handler, StringArguments args) {
            if (args.isEmpty()) {
                return false;
            }

            var newTimeStr = args.NextString(" ");

            if (newTimeStr.isEmpty()) {
                return false;
            }

            int newTime;
            tangible.OutObject<Integer> tempOut_newTime = new tangible.OutObject<Integer>();
            if (!tangible.TryParseHelper.tryParseInt(newTimeStr, tempOut_newTime) || newTime < 0) {
                newTime = tempOut_newTime.outArgValue;
                return false;
            } else {
                newTime = tempOut_newTime.outArgValue;
            }

            //Global.WorldMgr.SetRecordDiffInterval(newTime);
            //printf("Record diff every %i ms\n", newTime);

            return true;
        }


        private static boolean handleServerSetLogLevelCommand(CommandHandler handler, String type, String name, int level) {
            if (name.isEmpty() || level < 0 || (!Objects.equals(type, "a") && !Objects.equals(type, "l"))) {
                return false;
            }

            return Log.SetLogLevel(name, level, Objects.equals(type, "l"));
        }


        private static boolean handleServerSetMotdCommand(CommandHandler handler, StringArguments args) {
            global.getWorldMgr().setMotd(args.NextString(""));
            handler.sendSysMessage(SysMessage.MotdNew, args.getString());

            return true;
        }


        private static boolean handleServerSetClosedCommand(CommandHandler handler, StringArguments args) {
            var arg1 = args.NextString(" ");

            if (arg1.equalsIgnoreCase("on")) {
                handler.sendSysMessage(SysMessage.WorldClosed);
                global.getWorldMgr().setClosed(true);

                return true;
            } else if (arg1.equalsIgnoreCase("off")) {
                handler.sendSysMessage(SysMessage.WorldOpened);
                global.getWorldMgr().setClosed(false);

                return true;
            }

            handler.sendSysMessage(SysMessage.UseBol);

            return false;
        }
    }
}
