package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.player.Player;

public class RemoteAccessHandler extends CommandHandler {
    private final tangible.Action1Param<String> reportToRA;

    public RemoteAccessHandler(tangible.Action1Param<String> reportToRA) {
        super();
        reportToRA = reportToRA;
    }

    @Override
    public String getNameLink() {
        return getSysMessage(SysMessage.ConsoleCommand);
    }

    @Override
    public Locale getSessionDbcLocale() {
        return global.getWorldMgr().getDefaultDbcLocale();
    }


    @Override
    public byte getSessionDbLocaleIndex() {
        return (byte) global.getWorldMgr().getDefaultDbcLocale().getValue();
    }

    @Override
    public boolean isAvailable(ChatCommandNode cmd) {
        return cmd.permission.allowConsole;
    }

    @Override
    public boolean hasPermission(RBACPermissions permission) {
        return true;
    }

    @Override
    public void sendSysMessage(String str, boolean escapeCharacters) {
        setSentErrorMessage(true);
        reportToRA.invoke(str);
    }

    @Override
    public boolean parseCommands(String str) {
        if (str.isEmpty()) {
            return false;
        }

        // Console allows using commands both with and without leading indicator
        if (str.charAt(0) == '.' || str.charAt(0) == '!') {
            str = str.substring(1);
        }

        return _ParseCommands(str);
    }

    @Override
    public boolean needReportToTarget(Player chr) {
        return true;
    }
}
