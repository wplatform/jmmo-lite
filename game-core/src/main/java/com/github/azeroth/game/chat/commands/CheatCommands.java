package com.github.azeroth.game.chat.commands;


import com.github.azeroth.defines.Power;
import com.github.azeroth.game.chat.CommandHandler;


class CheatCommands {

    private static boolean handleCasttimeCheatCommand(CommandHandler handler, Boolean enableArg) {
        var enable = !handler.getSession().getPlayer().getCommandStatus(PlayerCommandStates.Casttime);

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            handler.getSession().getPlayer().setCommandStatusOn(PlayerCommandStates.Casttime);
            handler.sendSysMessage("CastTime Cheat is ON. Your spells won't have a casttime.");
        } else {
            handler.getSession().getPlayer().setCommandStatusOff(PlayerCommandStates.Casttime);
            handler.sendSysMessage("CastTime Cheat is OFF. Your spells will have a casttime.");
        }

        return true;
    }


    private static boolean handleCoolDownCheatCommand(CommandHandler handler, Boolean enableArg) {
        var enable = !handler.getSession().getPlayer().getCommandStatus(PlayerCommandStates.cooldown);

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            handler.getSession().getPlayer().setCommandStatusOn(PlayerCommandStates.cooldown);
            handler.sendSysMessage("Cooldown Cheat is ON. You are not on the global cooldown.");
        } else {
            handler.getSession().getPlayer().setCommandStatusOff(PlayerCommandStates.cooldown);
            handler.sendSysMessage("Cooldown Cheat is OFF. You are on the global cooldown.");
        }

        return true;
    }


    private static boolean handleExploreCheatCommand(CommandHandler handler, boolean reveal) {
        var chr = handler.getSelectedPlayer();

        if (!chr) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        if (reveal) {
            handler.sendSysMessage(SysMessage.YouSetExploreAll, handler.getNameLink(chr));

            if (handler.needReportToTarget(chr)) {
                chr.sendSysMessage(SysMessage.YoursExploreSetAll, handler.getNameLink());
            }
        } else {
            handler.sendSysMessage(SysMessage.YouSetExploreNothing, handler.getNameLink(chr));

            if (handler.needReportToTarget(chr)) {
                chr.sendSysMessage(SysMessage.YoursExploreSetNothing, handler.getNameLink());
            }
        }

        for (short i = 0; i < PlayerConst.EXPLOREDZONESSIZE; ++i) {
            if (reveal) {
                handler.getSession().getPlayer().addExploredZones(i, (long) 0xFFFFFFFFFFFFFFFF);
            } else {
                handler.getSession().getPlayer().removeExploredZones(i, (long) 0xFFFFFFFFFFFFFFFF);
            }
        }

        return true;
    }


    private static boolean handleGodModeCheatCommand(CommandHandler handler, Boolean enableArg) {
        var enable = !handler.getSession().getPlayer().getCommandStatus(PlayerCommandStates.God);

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            handler.getSession().getPlayer().setCommandStatusOn(PlayerCommandStates.God);
            handler.sendSysMessage("Godmode is ON. You won't take damage.");
        } else {
            handler.getSession().getPlayer().setCommandStatusOff(PlayerCommandStates.God);
            handler.sendSysMessage("Godmode is OFF. You can take damage.");
        }

        return true;
    }


    private static boolean handlePowerCheatCommand(CommandHandler handler, Boolean enableArg) {
        var enable = !handler.getSession().getPlayer().getCommandStatus(PlayerCommandStates.power);

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            var player = handler.getSession().getPlayer();

            // Set max power to all powers
            for (Power powerType = 0; powerType.getValue() < powerType.max.getValue(); ++powerType) {
                player.setPower(powerType, player.getMaxPower(powerType));
            }

            player.setCommandStatusOn(PlayerCommandStates.power);
            handler.sendSysMessage("Power Cheat is ON. You don't need mana/rage/energy to use spells.");
        } else {
            handler.getSession().getPlayer().setCommandStatusOff(PlayerCommandStates.power);
            handler.sendSysMessage("Power Cheat is OFF. You need mana/rage/energy to use spells.");
        }

        return true;
    }


    private static boolean handleCheatStatusCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();

        var enabled = "ON";
        var disabled = "OFF";

        handler.sendSysMessage(SysMessage.CommandCheatStatus);
        handler.sendSysMessage(SysMessage.CommandCheatGod, player.getCommandStatus(PlayerCommandStates.God) ? enabled : disabled);
        handler.sendSysMessage(SysMessage.CommandCheatCd, player.getCommandStatus(PlayerCommandStates.cooldown) ? enabled : disabled);
        handler.sendSysMessage(SysMessage.CommandCheatCt, player.getCommandStatus(PlayerCommandStates.Casttime) ? enabled : disabled);
        handler.sendSysMessage(SysMessage.CommandCheatPower, player.getCommandStatus(PlayerCommandStates.power) ? enabled : disabled);
        handler.sendSysMessage(SysMessage.CommandCheatWw, player.getCommandStatus(PlayerCommandStates.Waterwalk) ? enabled : disabled);
        handler.sendSysMessage(SysMessage.CommandCheatTaxinodes, player.isTaxiCheater() ? enabled : disabled);

        return true;
    }


    private static boolean handleTaxiCheatCommand(CommandHandler handler, Boolean enableArg) {
        var chr = handler.getSelectedPlayer();

        if (!chr) {
            chr = handler.getSession().getPlayer();
        } else if (handler.hasLowerSecurity(chr, ObjectGuid.Empty)) // check online security
        {
            return false;
        }

        var enable = !chr.isTaxiCheater();

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            chr.setTaxiCheater(true);
            handler.sendSysMessage(SysMessage.YouGiveTaxis, handler.getNameLink(chr));

            if (handler.needReportToTarget(chr)) {
                chr.sendSysMessage(SysMessage.YoursTaxisAdded, handler.getNameLink());
            }
        } else {
            chr.setTaxiCheater(false);
            handler.sendSysMessage(SysMessage.YouRemoveTaxis, handler.getNameLink(chr));

            if (handler.needReportToTarget(chr)) {
                chr.sendSysMessage(SysMessage.YoursTaxisRemoved, handler.getNameLink());
            }
        }

        return true;
    }


    private static boolean handleWaterWalkCheatCommand(CommandHandler handler, Boolean enableArg) {
        var enable = !handler.getSession().getPlayer().getCommandStatus(PlayerCommandStates.Waterwalk);

        if (enableArg != null) {
            enable = enableArg.booleanValue();
        }

        if (enable) {
            handler.getSession().getPlayer().setCommandStatusOn(PlayerCommandStates.Waterwalk);
            handler.getSession().getPlayer().setWaterWalking(true);
            handler.sendSysMessage("Waterwalking is ON. You can walk on water.");
        } else {
            handler.getSession().getPlayer().setCommandStatusOff(PlayerCommandStates.Waterwalk);
            handler.getSession().getPlayer().setWaterWalking(false);
            handler.sendSysMessage("Waterwalking is OFF. You can't walk on water.");
        }

        return true;
    }
}
