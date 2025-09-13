package com.github.azeroth.game.chat;


class AddonChannelCommandHandler extends CommandHandler {
    public static String PREFIX = "ForgedCore";

    private String echo;
    private boolean hadAck;
    private boolean humanReadable;

    public AddonChannelCommandHandler(WorldSession session) {
        super(session);
    }

    @Override
    public boolean parseCommands(String str) {
        if (str.length() < 5) {
            return false;
        }

        var opcode = str.charAt(0);
        echo = str.substring(1);

        switch (opcode) {
            case 'p': // p Ping
                sendAck();

                return true;
            case 'h': // h Issue human-readable command
            case 'i': // i Issue command
                if (str.length() < 6) {
                    return false;
                }

                humanReadable = opcode == 'h';
                var cmd = str.substring(5);

                if (_ParseCommands(cmd)) // actual command starts at str[5]
                {
                    if (!hadAck) {
                        sendAck();
                    }

                    if (getHasSentErrorMessage()) {
                        sendFailed();
                    } else {
                        sendOK();
                    }
                } else {
                    sendSysMessage(SysMessage.CmdInvalid, cmd);
                    sendFailed();
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    public void sendSysMessage(String str, boolean escapeCharacters) {
        if (!hadAck) {
            sendAck();
        }

        StringBuilder msg = new StringBuilder("m");
        msg.append(echo, 0, 4);
        var body = str;

        if (escapeCharacters) {
            body.replace("|", "||");
        }

        int pos, lastpos;

        for (lastpos = 0, pos = body.indexOf('\n', lastpos); pos != -1; lastpos = pos + 1, pos = body.indexOf('\n', lastpos)) {
            var line = msg;
            line.append(body, lastpos, pos - lastpos);
            send(line.toString());
        }

        msg.append(body, lastpos, pos - lastpos);
        send(msg.toString());
    }

    @Override
    public boolean isHumanReadable() {
        return humanReadable;
    }

    private void send(String msg) {
        ChatPkt chat = new ChatPkt();
        chat.initialize(ChatMsg.Whisper, language.Addon, getSession().getPlayer(), getSession().getPlayer(), msg, 0, "", locale.enUS, PREFIX);
        getSession().sendPacket(chat);
    }

    private void sendAck() // a Command acknowledged, no body
    {

        send(String.format("a{0:4}\0", echo));
        hadAck = true;
    }

    private void sendOK() // o Command OK, no body
    {

        send(String.format("o{0:4}\0", echo));
    }

    private void sendFailed() // f Command failed, no body
    {

        send(String.format("f{0:4}\0", echo));
    }
}
