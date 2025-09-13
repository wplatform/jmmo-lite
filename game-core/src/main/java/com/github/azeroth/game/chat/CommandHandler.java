package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.map.NearestGameObjectCheck;
import com.github.azeroth.game.map.grid.Cell;

import java.util.Objects;

public class CommandHandler {
    private static final String[] spellKeys = {"Hspell", "Htalent", "Henchant", "Htrade", "Hglyph"};

    private final WorldSession session;

    private boolean sentErrorMessage;

    public commandHandler() {
        this(null);
    }

    public commandHandler(WorldSession session) {
        session = session;
    }

    public final Player getSelectedPlayer() {
        if (session == null) {
            return null;
        }

        var selected = session.getPlayer().getTarget();

        if (selected.isEmpty()) {
            return session.getPlayer();
        }

        return global.getObjAccessor().findConnectedPlayer(selected);
    }

    public final Unit getSelectedUnit() {
        if (session == null) {
            return null;
        }

        var selected = session.getPlayer().getSelectedUnit();

        if (selected) {
            return selected;
        }

        return session.getPlayer();
    }

    public final WorldObject getSelectedObject() {
        if (session == null) {
            return null;
        }

        var selected = session.getPlayer().getTarget();

        if (selected.isEmpty()) {
            return getNearbyGameObject();
        }

        return global.getObjAccessor().GetUnit(session.getPlayer(), selected);
    }

    public final Creature getSelectedCreature() {
        if (session == null) {
            return null;
        }

        return ObjectAccessor.GetCreatureOrPetOrVehicle(session.getPlayer(), session.getPlayer().getTarget());
    }

    public final Player getSelectedPlayerOrSelf() {
        if (session == null) {
            return null;
        }

        var selected = session.getPlayer().getTarget();

        if (selected.isEmpty()) {
            return session.getPlayer();
        }

        // first try with selected target
        var targetPlayer = global.getObjAccessor().findConnectedPlayer(selected);

        // if the target is not a player, then return self
        if (!targetPlayer) {
            targetPlayer = session.getPlayer();
        }

        return targetPlayer;
    }

    private GameObject getNearbyGameObject() {
        if (session == null) {
            return null;
        }

        var pl = session.getPlayer();
        NearestGameObjectCheck check = new NearestGameObjectCheck(pl);
        GameObjectLastSearcher searcher = new GameObjectLastSearcher(pl, check, gridType.Grid);
        Cell.visitGrid(pl, searcher, MapDefine.SizeofGrids);

        return searcher.getTarget();
    }

    public String getNameLink() {
        return getNameLink(session.getPlayer());
    }

    public final boolean isConsole() {
        return session == null;
    }

    public final WorldSession getSession() {
        return session;
    }

    public final Player getPlayer() {
        return (session == null ? null : session.getPlayer());
    }

    public Locale getSessionDbcLocale() {
        return session.getSessionDbcLocale();
    }


    public byte getSessionDbLocaleIndex() {
        return (byte) session.getSessionDbLocaleIndex().getValue();
    }

    public final boolean getHasSentErrorMessage() {
        return sentErrorMessage;
    }

    public boolean parseCommands(String text) {
        if (text.isEmpty()) {
            return false;
        }

        // chat case (.command or !command format)
        if (text.charAt(0) != '!' && text.charAt(0) != '.') {
            return false;
        }

        /** ignore single . and ! in line
         */
        if (text.length() < 2) {
            return false;
        }

        // ignore messages staring from many dots.
        if (text.charAt(1) == text.charAt(0)) {
            return false;
        }

        if (text.charAt(1) == ' ') {
            return false;
        }

        return _ParseCommands(text.substring(1));
    }

    public final boolean _ParseCommands(String text) {
        if (ChatCommandNode.tryExecuteCommand(this, text)) {
            return true;
        }

        // Pretend commands don't exist for regular players
        if (session != null && !session.hasPermission(RBACPermissions.CommandsNotifyCommandNotFoundError)) {
            return false;
        }

        // Send error message for GMs
        sendSysMessage(SysMessage.CmdInvalid, text);

        return true;
    }

    public boolean isAvailable(ChatCommandNode cmd) {
        return hasPermission(cmd.permission.requiredPermission);
    }

    public boolean isHumanReadable() {
        return true;
    }

    public boolean hasPermission(RBACPermissions permission) {
        return session.hasPermission(permission);
    }

    public final String extractKeyFromLink(StringArguments args, String... linkType) {
        tangible.OutObject<Integer> tempOut__ = new tangible.OutObject<Integer>();
        var tempVar = extractKeyFromLink(args, linkType, tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final String extractKeyFromLink(StringArguments args, String[] linkType, tangible.OutObject<Integer> found_idx) {
        tangible.OutObject<String> tempOut__ = new tangible.OutObject<String>();
        var tempVar = extractKeyFromLink(args, linkType, found_idx, tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final String extractKeyFromLink(StringArguments args, String[] linkType, tangible.OutObject<Integer> found_idx, tangible.OutObject<String> something1) {
        found_idx.outArgValue = 0;
        something1.outArgValue = null;

        // skip empty
        if (args.isEmpty()) {
            return null;
        }

        // return non link case
        if (args.get(0) != '|') {
            return args.NextString(" ");
        }

        if (args.get(1) == 'c') {
            var check = args.NextString("|");

            if (StringUtil.isEmpty(check)) {
                return null;
            }
        } else {
            args.NextChar(" ");
        }

        var cLinkType = args.NextString(":");

        if (StringUtil.isEmpty(cLinkType)) {
            return null;
        }

        for (var i = 0; i < linkType.length; ++i) {
            if (Objects.equals(cLinkType, linkType[i])) {
                var cKey = args.NextString(":|"); // extract key

                something1.outArgValue = args.NextString(":|"); // extract something

                args.NextString("]"); // restart scan tail and skip name with possible spaces
                args.NextString(" "); // skip link tail (to allow continue strtok(NULL, s) use after return from function
                found_idx.outArgValue = i;

                return cKey;
            }
        }

        args.NextString(" ");
        sendSysMessage(SysMessage.WrongLinkType);

        return null;
    }

    public final String extractQuotedArg(String str) {
        if (StringUtil.isEmpty(str)) {
            return null;
        }

        if (!str.contains("\"")) {
            return str;
        }

        return str.replace("\"", "");
    }

    public final boolean extractPlayerTarget(StringArguments args, tangible.OutObject<Player> player) {
        tangible.OutObject<ObjectGuid> tempOut__ = new tangible.OutObject<ObjectGuid>();
        tangible.OutObject<String> tempOut__2 = new tangible.OutObject<String>();
        var tempVar = extractPlayerTarget(args, player, tempOut__, tempOut__2);
        _ = tempOut__2.outArgValue;
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final boolean extractPlayerTarget(StringArguments args, tangible.OutObject<Player> player, tangible.OutObject<ObjectGuid> playerGuid) {
        tangible.OutObject<String> tempOut__ = new tangible.OutObject<String>();
        var tempVar = extractPlayerTarget(args, player, playerGuid, tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final boolean extractPlayerTarget(StringArguments args, tangible.OutObject<Player> player, tangible.OutObject<ObjectGuid> playerGuid, tangible.OutObject<String> playerName) {
        player.outArgValue = null;
        playerGuid.outArgValue = ObjectGuid.Empty;
        playerName.outArgValue = "";

        if (args != null && !args.isEmpty()) {
            var name = extractPlayerNameFromLink(args);

            if (StringUtil.isEmpty(name)) {
                sendSysMessage(SysMessage.PlayerNotFound);
                sentErrorMessage = true;

                return false;
            }

            player.outArgValue = global.getObjAccessor().FindPlayerByName(name);
            var guid = player.outArgValue == null ? global.getCharacterCacheStorage().getCharacterGuidByName(name) : ObjectGuid.Empty;

            playerGuid.outArgValue = player.outArgValue != null ? player.outArgValue.getGUID() : guid;
            playerName.outArgValue = player.outArgValue != null || !guid.isEmpty() ? name : "";
        } else {
            player.outArgValue = getSelectedPlayer();
            playerGuid.outArgValue = player.outArgValue != null ? player.outArgValue.getGUID() : ObjectGuid.Empty;
            playerName.outArgValue = player.outArgValue != null ? player.outArgValue.getName() : "";
        }

        if (player.outArgValue == null && playerGuid.outArgValue.isEmpty() && StringUtil.isEmpty(playerName.outArgValue)) {
            sendSysMessage(SysMessage.PlayerNotFound);
            sentErrorMessage = true;

            return false;
        }

        return true;
    }


    public final long extractLowGuidFromLink(StringArguments args, tangible.RefObject<HighGuid> guidHigh) {
        String[] guidKeys = {"Hplayer", "Hcreature", "Hgameobject"};

        // |color|Hcreature:creature_guid|h[name]|h|r
        // |color|Hgameobject:go_guid|h[name]|h|r
        // |color|Hplayer:name|h[name]|h|r
        int type;
        tangible.OutObject<Integer> tempOut_type = new tangible.OutObject<Integer>();
        var idS = extractKeyFromLink(args, guidKeys, tempOut_type);
        type = tempOut_type.outArgValue;

        if (StringUtil.isEmpty(idS)) {
            return 0;
        }

        switch (type) {
            case 0: {
                guidHigh.refArgValue = HighGuid.Player;

                tangible.RefObject<String> tempRef_idS = new tangible.RefObject<String>(idS);
                if (!ObjectManager.normalizePlayerName(tempRef_idS)) {
                    idS = tempRef_idS.refArgValue;
                    return 0;
                } else {
                    idS = tempRef_idS.refArgValue;
                }

                var player = global.getObjAccessor().FindPlayerByName(idS);

                if (player) {
                    return player.getGUID().getCounter();
                }

                var guid = global.getCharacterCacheStorage().getCharacterGuidByName(idS);

                if (guid.isEmpty()) {
                    return 0;
                }

                return guid.getCounter();
            }
            case 1: {
                guidHigh.refArgValue = HighGuid.Creature;

                long lowguid;
                tangible.OutObject<Long> tempOut_lowguid = new tangible.OutObject<Long>();
                if (!tangible.TryParseHelper.tryParseLong(idS, tempOut_lowguid)) {
                    lowguid = tempOut_lowguid.outArgValue;
                    return 0;
                } else {
                    lowguid = tempOut_lowguid.outArgValue;
                }

                return lowguid;
            }
            case 2: {
                guidHigh.refArgValue = HighGuid.GameObject;

                long lowguid;
                tangible.OutObject<Long> tempOut_lowguid2 = new tangible.OutObject<Long>();
                if (!tangible.TryParseHelper.tryParseLong(idS, tempOut_lowguid2)) {
                    lowguid = tempOut_lowguid2.outArgValue;
                    return 0;
                } else {
                    lowguid = tempOut_lowguid2.outArgValue;
                }

                return lowguid;
            }
        }

        // unknown type?
        return 0;
    }


    public final int extractSpellIdFromLink(StringArguments args) {
        // number or [name] Shift-click form |color|Henchant:recipe_spell_id|h[prof_name: recipe_name]|h|r
        // number or [name] Shift-click form |color|Hglyph:glyph_slot_id:glyph_prop_id|h[value]|h|r
        // number or [name] Shift-click form |color|Hspell:spell_id|h[name]|h|r
        // number or [name] Shift-click form |color|Htalent:talent_id, rank|h[name]|h|r
        // number or [name] Shift-click form |color|Htrade:spell_id, skill_id, max_value, cur_value|h[name]|h|r
        int type;
        tangible.OutObject<Integer> tempOut_type = new tangible.OutObject<Integer>();
        String param1Str;
        tangible.OutObject<String> tempOut_param1Str = new tangible.OutObject<String>();
        var idS = extractKeyFromLink(args, spellKeys, tempOut_type, tempOut_param1Str);
        param1Str = tempOut_param1Str.outArgValue;
        type = tempOut_type.outArgValue;

        if (StringUtil.isEmpty(idS)) {
            return 0;
        }

        int id;
        tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(idS, tempOut_id)) {
            id = tempOut_id.outArgValue;
            return 0;
        } else {
            id = tempOut_id.outArgValue;
        }

        switch (type) {
            case 0:
                return id;
            case 1: {
                // talent
                var talentEntry = CliDB.TalentStorage.get(id);

                if (talentEntry == null) {
                    return 0;
                }

                return talentEntry.spellID;
            }
            case 2:
            case 3:
                return id;
            case 4: {
                int glyph_prop_id;
                tangible.OutObject<Integer> tempOut_glyph_prop_id = new tangible.OutObject<Integer>();
                if (!tangible.TryParseHelper.tryParseInt(param1Str, tempOut_glyph_prop_id)) {
                    glyph_prop_id = tempOut_glyph_prop_id.outArgValue;
                    glyph_prop_id = 0;
                } else {
                    glyph_prop_id = tempOut_glyph_prop_id.outArgValue;
                }

                var glyphPropEntry = CliDB.GlyphPropertiesStorage.get(glyph_prop_id);

                if (glyphPropEntry == null) {
                    return 0;
                }

                return glyphPropEntry.spellID;
            }
        }

        // unknown type?
        return 0;
    }


    public final GameObject getObjectFromPlayerMapByDbGuid(long lowguid) {
        if (session == null) {
            return null;
        }

        var bounds = session.getPlayer().getMap().getGameObjectBySpawnIdStore().get(lowguid);

        if (!bounds.isEmpty()) {
            return bounds.get(0);
        }

        return null;
    }


    public final Creature getCreatureFromPlayerMapByDbGuid(long lowguid) {
        if (!session) {
            return null;
        }

        // Select the first alive creature or a dead one if not found
        Creature creature = null;
        var bounds = session.getPlayer().getMap().getCreatureBySpawnIdStore().get(lowguid);

        for (var it : bounds) {
            creature = it;

            if (it.isAlive()) {
                break;
            }
        }

        return creature;
    }

    public final String playerLink(String name) {
        return session != null ? "|cffffffff|Hplayer:" + name + "|h[" + name + "]|h|r" : name;
    }

    public final String getNameLink(Player obj) {
        return playerLink(obj.getName());
    }

    public boolean needReportToTarget(Player chr) {
        var pl = session.getPlayer();

        return pl != chr && pl.isVisibleGloballyFor(chr);
    }


    public final boolean hasLowerSecurity(Player target, ObjectGuid guid) {
        return hasLowerSecurity(target, guid, false);
    }

    public final boolean hasLowerSecurity(Player target, ObjectGuid guid, boolean strong) {
        WorldSession target_session = null;
        int target_account = 0;

        if (target != null) {
            target_session = target.getSession();
        } else if (!guid.isEmpty()) {
            target_account = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(guid);
        }

        if (target_session == null && target_account == 0) {
            sendSysMessage(SysMessage.PlayerNotFound);
            sentErrorMessage = true;

            return true;
        }

        return hasLowerSecurityAccount(target_session, target_account, strong);
    }


    public final boolean hasLowerSecurityAccount(WorldSession target, int target_account) {
        return hasLowerSecurityAccount(target, target_account, false);
    }

    public final boolean hasLowerSecurityAccount(WorldSession target, int target_account, boolean strong) {
        AccountTypes target_ac_sec;

        // allow everything from console and RA console
        if (session == null) {
            return false;
        }

        // ignore only for non-players for non strong checks (when allow apply command at least to same sec level)
        if (!global.getAccountMgr().isPlayerAccount(session.getSecurity()) && !strong && !WorldConfig.getBoolValue(WorldCfg.GmLowerSecurity)) {
            return false;
        }

        if (target != null) {
            target_ac_sec = target.getSecurity();
        } else if (target_account != 0) {
            target_ac_sec = global.getAccountMgr().getSecurity(target_account, (int) global.getWorldMgr().getRealmId().index);
        } else {
            return true; // caller must report error for (target == NULL && target_account == 0)
        }

        if (session.getSecurity().getValue() < target_ac_sec.getValue() || (strong && session.getSecurity().getValue() <= target_ac_sec.getValue())) {
            sendSysMessage(SysMessage.YoursSecurityIsLow);
            sentErrorMessage = true;

            return true;
        }

        return false;
    }

    public final String getSysMessage(SysMessage str) {
        return global.getObjectMgr().getSysMessage(str);
    }

    public final String getParsedString(SysMessage cypherString, object... args) {
        return String.format(global.getObjectMgr().getSysMessage(cypherString), args);
    }

    public final void sendSysMessage(String str, object... args) {
        sendSysMessage(String.format(str, args));
    }

    public final void sendSysMessage(SysMessage cypherString, object... args) {
        sendSysMessage(String.format(global.getObjectMgr().getSysMessage(cypherString), args));
    }


    public void sendSysMessage(String str) {
        sendSysMessage(str, false);
    }

    public void sendSysMessage(String str, boolean escapeCharacters) {
        sentErrorMessage = true;

        if (escapeCharacters) {
            str.replace("|", "||");
        }

        ChatPkt messageChat = new ChatPkt();

        var lines = new LocalizedString();

        for (var i = 0; i < lines.length; ++i) {
            messageChat.initialize(ChatMsg.System, language.Universal, null, null, lines.get(i));
            session.sendPacket(messageChat);
        }
    }

    public final void sendNotification(SysMessage str, object... args) {
        session.sendNotification(str, args);
    }

    public final void sendGlobalSysMessage(String str) {
        // Chat output
        ChatPkt data = new ChatPkt();
        data.initialize(ChatMsg.System, language.Universal, null, null, str);
        global.getWorldMgr().sendGlobalMessage(data);
    }

    public final void sendGlobalGMSysMessage(String str) {
        // Chat output
        ChatPkt data = new ChatPkt();
        data.initialize(ChatMsg.System, language.Universal, null, null, str);
        global.getWorldMgr().sendGlobalGMMessage(data);
    }


    public final boolean getPlayerGroupAndGUIDByName(String name, tangible.OutObject<Player> player, tangible.OutObject<PlayerGroup> group, tangible.OutObject<ObjectGuid> guid) {
        return getPlayerGroupAndGUIDByName(name, player, group, guid, false);
    }

    public final boolean getPlayerGroupAndGUIDByName(String name, tangible.OutObject<Player> player, tangible.OutObject<PlayerGroup> group, tangible.OutObject<ObjectGuid> guid, boolean offline) {
        player.outArgValue = null;
        guid.outArgValue = ObjectGuid.Empty;
        group.outArgValue = null;

        if (!name.isEmpty()) {
            tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
            if (!ObjectManager.normalizePlayerName(tempRef_name)) {
                name = tempRef_name.refArgValue;
                sendSysMessage(SysMessage.PlayerNotFound);

                return false;
            } else {
                name = tempRef_name.refArgValue;
            }

            player.outArgValue = global.getObjAccessor().FindPlayerByName(name);

            if (offline) {
                guid.outArgValue = global.getCharacterCacheStorage().getCharacterGuidByName(name);
            }
        }

        if (player.outArgValue) {
            group.outArgValue = player.outArgValue.getGroup();

            if (guid.outArgValue.isEmpty() || !offline) {
                guid.outArgValue = player.outArgValue.getGUID();
            }
        } else {
            if (getSelectedPlayer()) {
                player.outArgValue = getSelectedPlayer();
            } else {
                player.outArgValue = session.getPlayer();
            }

            if (guid.outArgValue.isEmpty() || !offline) {
                guid.outArgValue = player.outArgValue.getGUID();
            }

            group.outArgValue = player.outArgValue.getGroup();
        }

        return true;
    }

    public final void setSentErrorMessage(boolean val) {
        sentErrorMessage = val;
    }

    private String extractPlayerNameFromLink(StringArguments args) {
        // |color|Hplayer:name|h[name]|h|r
        var name = extractKeyFromLink(args, "Hplayer");

        if (name.isEmpty()) {
            return "";
        }

        tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
        if (!ObjectManager.normalizePlayerName(tempRef_name)) {
            name = tempRef_name.refArgValue;
            return "";
        } else {
            name = tempRef_name.refArgValue;
        }

        return name;
    }

    private boolean hasStringAbbr(String name, String part) {
        // non "" command
        if (!name.isEmpty()) {
            // "" part from non-"" command
            if (part.isEmpty()) {
                return false;
            }

            var partIndex = 0;

            while (true) {
                if (partIndex >= part.length() || part.charAt(partIndex) == ' ') {
                    return true;
                } else if (partIndex >= name.length()) {
                    return false;
                } else if (Character.toLowerCase(name.charAt(partIndex)) != Character.toLowerCase(part.charAt(partIndex))) {
                    return false;
                }

                ++partIndex;
            }
        }
        // allow with any for ""

        return true;
    }
}
