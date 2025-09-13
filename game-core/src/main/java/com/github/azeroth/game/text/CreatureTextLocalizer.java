package com.github.azeroth.game.text;


import com.github.azeroth.game.chat.ChatPacketSender;
import com.github.azeroth.game.chat.MessageBuilder;
import com.github.azeroth.game.entity.player.Player;

import java.util.HashMap;
import java.util.locale;

public class CreatureTextLocalizer implements IDoWork<Player> {
    private final HashMap<locale, ChatPacketSender> packetCache = new HashMap<locale, ChatPacketSender>();
    private final MessageBuilder builder;
    private final ChatMsg msgType;

    public CreatureTextLocalizer(MessageBuilder builder, ChatMsg msgType) {
        builder = builder;
        msgType = msgType;
    }

    public final void invoke(Player player) {
        var loc_idx = player.getSession().getSessionDbLocaleIndex();
        ChatPacketSender sender;

        // create if not cached yet
        if (!packetCache.containsKey(loc_idx)) {
            sender = builder.invoke(loc_idx);
            packetCache.put(loc_idx, sender);
        } else {
            sender = packetCache.get(loc_idx);
        }

        switch (msgType) {
            case MonsterWhisper:
            case RaidBossWhisper:
                var message = sender.untranslatedPacket;
                message.setReceiver(player, loc_idx);
                player.sendPacket(message);

                break;
            default:
                break;
        }

        sender.invoke(player);
    }
}
