package com.github.azeroth.game.text;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.defines.ChatMsg;
import com.github.azeroth.defines.Language;
import com.github.azeroth.defines.Team;
import com.github.azeroth.game.chat.MessageBuilder;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.PlayerDistWorker;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.packet.misc.PlayObjectSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static game.WardenActions.Log;

public final class CreatureTextManager {

    private final HashMap<Integer, MultiMap<Byte, CreatureTextEntry>> textMap = new HashMap<Integer, MultiMap<Byte, CreatureTextEntry>>();
    private final HashMap<CreatureTextId, CreatureTextLocale> localeTextMap = new HashMap<CreatureTextId, CreatureTextLocale>();

    private CreatureTextManager() {
    }

    public void loadCreatureTexts() {
        var oldMSTime = System.currentTimeMillis();

        textMap.clear(); // for reload case
        //all currently used temp texts are NOT reset

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_CREATURE_TEXT);
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 ceature texts. DB table `creature_texts` is empty.");

            return;
        }

        int textCount = 0;
        int creatureCount = 0;

        do {
            CreatureTextEntry temp = new CreatureTextEntry();

            temp.creatureId = result.<Integer>Read(0);
            temp.groupId = result.<Byte>Read(1);
            temp.id = result.<Byte>Read(2);
            temp.text = result.<String>Read(3);
            temp.type = ChatMsg.forValue(result.<Byte>Read(4));
            temp.lang = language.forValue(result.<Byte>Read(5));
            temp.probability = result.<Float>Read(6);
            temp.emote = emote.forValue(result.<Integer>Read(7));
            temp.duration = result.<Integer>Read(8);
            temp.sound = result.<Integer>Read(9);
            temp.soundPlayType = SoundKitPlayType.forValue(result.<Byte>Read(10));
            temp.broadcastTextId = result.<Integer>Read(11);
            temp.textRange = CreatureTextRange.forValue(result.<Byte>Read(12));

            if (temp.sound != 0) {
                if (!CliDB.SoundKitStorage.containsKey(temp.sound)) {
                    if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                        DB.World.execute(String.format("UPDATE creature_text SET Sound = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                    } else {
                        Logs.SQL.error(String.format("GossipManager: Entry %1$s, Group %2$s in table `creature_texts` has Sound %3$s but sound does not exist.", temp.creatureId, temp.groupId, temp.sound));
                    }

                    temp.sound = 0;
                }
            }

            if (temp.soundPlayType.getValue() >= SoundKitPlayType.max.getValue()) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("UPDATE creature_text SET soundPlayType = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                } else {
                    Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s in table `creature_text` has PlayType %3$s but does not exist.", temp.creatureId, temp.groupId, temp.soundPlayType));
                }

                temp.soundPlayType = SoundKitPlayType.NORMAL;
            }

            if (temp.lang != language.Universal && !global.getLanguageMgr().isLanguageExist(temp.lang)) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("UPDATE creature_text SET language = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                } else {
                    Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s in table `creature_texts` using Language %3$s but Language does not exist.", temp.creatureId, temp.groupId, temp.lang));
                }

                temp.lang = language.Universal;
            }

            if (temp.type.getValue() >= ChatMsg.max.getValue()) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("UPDATE creature_text SET type = %1$s WHERE creatureID = %2$s AND groupID = %3$s", ChatMsg.Say, temp.creatureId, temp.groupId));
                } else {
                    Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s in table `creature_texts` has Type %3$s but this Chat Type does not exist.", temp.creatureId, temp.groupId, temp.type));
                }

                temp.type = ChatMsg.Say;
            }

            if (temp.emote != 0) {
                if (!CliDB.EmotesStorage.containsKey((int) temp.emote.getValue())) {
                    if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                        DB.World.execute(String.format("UPDATE creature_text SET emote = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                    } else {
                        Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s in table `creature_texts` has Emote %3$s but emote does not exist.", temp.creatureId, temp.groupId, temp.emote));
                    }

                    temp.emote = emote.OneshotNone;
                }
            }

            if (temp.broadcastTextId != 0) {
                if (!CliDB.BroadcastTextStorage.containsKey(temp.broadcastTextId)) {
                    if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                        DB.World.execute(String.format("UPDATE creature_text SET broadcastTextId = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                    } else {
                        Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s, Id %3$s in table `creature_texts` has non-existing or incompatible BroadcastTextId %4$s.", temp.creatureId, temp.groupId, temp.id, temp.broadcastTextId));
                    }

                    temp.broadcastTextId = 0;
                }
            }

            if (temp.textRange.getValue() > CreatureTextRange.PERSONAL.getValue()) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("UPDATE creature_text SET textRange = 0 WHERE creatureID = %1$s AND groupID = %2$s", temp.creatureId, temp.groupId));
                } else {
                    Logs.SQL.error(String.format("CreatureTextMgr: Entry %1$s, Group %2$s, Id %3$s in table `creature_text` has incorrect TextRange %4$s.", temp.creatureId, temp.groupId, temp.id, temp.textRange));
                }

                temp.textRange = CreatureTextRange.NORMAL;
            }

            if (!textMap.containsKey(temp.creatureId)) {
                textMap.put(temp.creatureId, new MultiMap<Byte, CreatureTextEntry>());
                ++creatureCount;
            }

            textMap.get(temp.creatureId).add(temp.groupId, temp);
            ++textCount;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s creature texts for %2$s creatures in %3$s ms", textCount, creatureCount, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public void loadCreatureTextLocales() {
        var oldMSTime = System.currentTimeMillis();

        localeTextMap.clear(); // for reload case

        var result = DB.World.query("SELECT creatureId, groupId, ID, locale, Text FROM creature_text_locale");

        if (result.isEmpty()) {
            return;
        }

        do {
            var creatureId = result.<Integer>Read(0);
            int groupId = result.<Byte>Read(1);
            int id = result.<Byte>Read(2);
            var localeName = result.<String>Read(3);
            var locale = localeName.<locale>ToEnum();

            if (!SharedConst.IsValidLocale(locale) || locale == locale.enUS) {
                continue;
            }

            var key = new CreatureTextId(creatureId, groupId, id);

            if (!localeTextMap.containsKey(key)) {
                localeTextMap.put(key, new CreatureTextLocale());
            }

            var data = localeTextMap.get(key);
            ObjectManager.addLocaleString(result.<String>Read(4), locale, data.text);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} creature localized texts in {1} ms", localeTextMap.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }


    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range, int sound, SoundKitPlayType playType, Team team, boolean gmOnly) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, range, sound, playType, team, gmOnly, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range, int sound, SoundKitPlayType playType, Team team) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, range, sound, playType, team, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range, int sound, SoundKitPlayType playType) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, range, sound, playType, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range, int sound) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, range, sound, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, range, 0, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language) {
        return sendChat(source, textGroup, whisperTarget, msgType, language, CreatureTextRange.NORMAL, 0, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType) {
        return sendChat(source, textGroup, whisperTarget, msgType, language.Addon, CreatureTextRange.NORMAL, 0, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget) {
        return sendChat(source, textGroup, whisperTarget, ChatMsg.Addon, language.Addon, CreatureTextRange.NORMAL, 0, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup) {
        return sendChat(source, textGroup, null, ChatMsg.Addon, language.Addon, CreatureTextRange.NORMAL, 0, SoundKitPlayType.NORMAL, Team.other, false, null);
    }

    public int sendChat(Creature source, byte textGroup, WorldObject whisperTarget, ChatMsg msgType, Language language, CreatureTextRange range, int sound, SoundKitPlayType playType, Team team, boolean gmOnly, Player srcPlr) {
        if (source == null) {
            return 0;
        }

        var sList = textMap.get(source.getEntry());

        if (sList == null) {
            Logs.SQL.error("GossipManager: Could not find Text for CREATURE({0}) Entry {1} in 'creature_text' table. Ignoring.", source.getName(), source.getEntry());

            return 0;
        }

        var textGroupContainer = sList.get(textGroup);

        if (textGroupContainer.isEmpty()) {
            Log.outError(LogFilter.ChatSystem, "GossipManager: Could not find TextGroup {0} for CREATURE({1}) GuidLow {2} Entry {3}. Ignoring.", textGroup, source.getName(), source.getGUID().toString(), source.getEntry());

            return 0;
        }

        ArrayList<CreatureTextEntry> tempGroup = new ArrayList<>();
        var repeatGroup = source.getTextRepeatGroup(textGroup);

        for (var entry : textGroupContainer) {
            if (!repeatGroup.contains(entry.id)) {
                tempGroup.add(entry);
            }
        }

        if (tempGroup.isEmpty()) {
            source.clearTextRepeatGroup(textGroup);
            tempGroup = textGroupContainer;
        }

        var textEntry = tempGroup.SelectRandomElementByWeight(t -> t.probability);

        var finalType = (msgType == ChatMsg.Addon) ? textEntry.type : msgType;
        var finalLang = (language == language.Addon) ? textEntry.lang : language;
        var finalSound = textEntry.sound;
        var finalPlayType = textEntry.soundPlayType;

        if (sound != 0) {
            finalSound = sound;
            finalPlayType = playType;
        } else {
            var bct = CliDB.BroadcastTextStorage.get(textEntry.broadcastTextId);

            if (bct != null) {
                var broadcastTextSoundId = bct.SoundKitID[source.getGender() == gender.Female ? 1 : 0];

                if (broadcastTextSoundId != 0) {
                    finalSound = broadcastTextSoundId;
                }
            }
        }

        if (range == CreatureTextRange.NORMAL) {
            range = textEntry.textRange;
        }

        if (finalSound != 0) {
            sendSound(source, finalSound, finalType, whisperTarget, range, team, gmOnly, textEntry.broadcastTextId, finalPlayType);
        }

        Unit finalSource = source;

        if (srcPlr) {
            finalSource = srcPlr;
        }

        if (textEntry.emote != 0) {
            sendEmote(finalSource, textEntry.emote);
        }

        if (srcPlr) {
            PlayerTextBuilder builder = new PlayerTextBuilder(source, finalSource, finalSource.getGender(), finalType, textEntry.groupId, textEntry.id, finalLang, whisperTarget);
            sendChatPacket(finalSource, builder, finalType, whisperTarget, range, team, gmOnly);
        } else {
            CreatureTextBuilder builder = new CreatureTextBuilder(finalSource, finalSource.getGender(), finalType, textEntry.groupId, textEntry.id, finalLang, whisperTarget);
            sendChatPacket(finalSource, builder, finalType, whisperTarget, range, team, gmOnly);
        }

        source.setTextRepeatId(textGroup, textEntry.id);

        return textEntry.duration;
    }

    public float getRangeForChatType(ChatMsg msgType) {
        var dist = WorldConfig.getFloatValue(WorldCfg.ListenRangeSay);

        switch (msgType) {
            case MonsterYell:
                dist = WorldConfig.getFloatValue(WorldCfg.ListenRangeYell);

                break;
            case MonsterEmote:
            case RaidBossEmote:
                dist = WorldConfig.getFloatValue(WorldCfg.ListenRangeTextemote);

                break;
            default:
                break;
        }

        return dist;
    }


    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team, boolean gmOnly, int keyBroadcastTextId) {
        sendSound(source, sound, msgType, whisperTarget, range, team, gmOnly, keyBroadcastTextId, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team, boolean gmOnly) {
        sendSound(source, sound, msgType, whisperTarget, range, team, gmOnly, 0, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team) {
        sendSound(source, sound, msgType, whisperTarget, range, team, false, 0, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range) {
        sendSound(source, sound, msgType, whisperTarget, range, Team.other, false, 0, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget) {
        sendSound(source, sound, msgType, whisperTarget, CreatureTextRange.NORMAL, Team.other, false, 0, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType) {
        sendSound(source, sound, msgType, null, CreatureTextRange.NORMAL, Team.other, false, 0, SoundKitPlayType.NORMAL);
    }

    public void sendSound(Creature source, int sound, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team, boolean gmOnly, int keyBroadcastTextId, SoundKitPlayType playType) {
        if (sound == 0 || !source) {
            return;
        }

        if (playType == SoundKitPlayType.ObjectSound) {
            PlayObjectSound pkt = new PlayObjectSound();
            pkt.targetObjectGUID = whisperTarget.getGUID();
            pkt.sourceObjectGUID = source.getGUID();
            pkt.soundKitID = sound;
            WorldLocation location = whisperTarget.getLocation();
            pkt.position = new Vector3(location.getX(), location.getY(), location.getZ());
            pkt.broadcastTextID = keyBroadcastTextId;
            sendNonChatPacket(source, pkt, msgType, whisperTarget, range, team, gmOnly);
        } else if (playType == SoundKitPlayType.NORMAL) {
            sendNonChatPacket(source, new playSound(source.getGUID(), sound, keyBroadcastTextId), msgType, whisperTarget, range, team, gmOnly);
        }
    }


    public boolean textExist(int sourceEntry, byte textGroup) {
        if (sourceEntry == 0) {
            return false;
        }

        var textHolder = textMap.get(sourceEntry);

        if (textHolder == null) {
            Log.outDebug(LogFilter.unit, "CreatureTextMgr.TextExist: Could not find Text for CREATURE (entry {0}) in 'creature_text' table.", sourceEntry);

            return false;
        }

        var textEntryList = textHolder.get(textGroup);

        if (textEntryList.isEmpty()) {
            Log.outDebug(LogFilter.unit, "CreatureTextMgr.TextExist: Could not find TextGroup {0} for CREATURE (entry {1}).", textGroup, sourceEntry);

            return false;
        }

        return true;
    }


    public String getLocalizedChatString(int entry, Gender gender, byte textGroup, int id) {
        return getLocalizedChatString(entry, gender, textGroup, id, locale.enUS);
    }

    public String getLocalizedChatString(int entry, Gender gender, byte textGroup, int id, Locale locale) {
        var multiMap = textMap.get(entry);

        if (multiMap == null) {
            return "";
        }

        var creatureTextEntryList = multiMap.get(textGroup);

        if (creatureTextEntryList.isEmpty()) {
            return "";
        }

        CreatureTextEntry creatureTextEntry = null;

        for (var i = 0; i != creatureTextEntryList.count; ++i) {
            creatureTextEntry = creatureTextEntryList[i];

            if (creatureTextEntry.id == id) {
                break;
            }
        }

        if (creatureTextEntry == null) {
            return "";
        }

        if (locale.getValue() >= locale.Total.getValue()) {
            locale = locale.enUS;
        }

        String baseText;
        var bct = CliDB.BroadcastTextStorage.get(creatureTextEntry.broadcastTextId);

        if (bct != null) {
            baseText = global.getDB2Mgr().GetBroadcastTextValue(bct, locale, gender);
        } else {
            baseText = creatureTextEntry.text;
        }

        if (locale != locale.enUS && bct == null) {
            var creatureTextLocale = localeTextMap.get(new CreatureTextId(entry, textGroup, id));

            if (creatureTextLocale != null) {
                tangible.RefObject<String> tempRef_baseText = new tangible.RefObject<String>(baseText);
                ObjectManager.getLocaleString(creatureTextLocale.text, locale, tempRef_baseText);
                baseText = tempRef_baseText.refArgValue;
            }
        }

        return baseText;
    }


    public void sendChatPacket(WorldObject source, MessageBuilder builder, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team) {
        sendChatPacket(source, builder, msgType, whisperTarget, range, team, false);
    }

    public void sendChatPacket(WorldObject source, MessageBuilder builder, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range) {
        sendChatPacket(source, builder, msgType, whisperTarget, range, Team.other, false);
    }

    public void sendChatPacket(WorldObject source, MessageBuilder builder, ChatMsg msgType, WorldObject whisperTarget) {
        sendChatPacket(source, builder, msgType, whisperTarget, CreatureTextRange.NORMAL, Team.other, false);
    }

    public void sendChatPacket(WorldObject source, MessageBuilder builder, ChatMsg msgType) {
        sendChatPacket(source, builder, msgType, null, CreatureTextRange.NORMAL, Team.other, false);
    }

    public void sendChatPacket(WorldObject source, MessageBuilder builder, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team, boolean gmOnly) {
        if (source == null) {
            return;
        }

        var localizer = new CreatureTextLocalizer(builder, msgType);

        switch (msgType) {
            case MonsterWhisper:
            case RaidBossWhisper: {
                if (range == CreatureTextRange.NORMAL) //ignores team and gmOnly
                {
                    if (!whisperTarget || !whisperTarget.isTypeId(TypeId.PLAYER)) {
                        return;
                    }

                    localizer.invoke(whisperTarget.toPlayer());

                    return;
                }

                break;
            }
            default:
                break;
        }

        switch (range) {
            case Area: {
                var areaId = source.getAreaId();
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if (pl.getAreaId() == areaId && (team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        localizer.invoke(pl);
                    }
                }

                return;
            }
            case Zone: {
                var zoneId = source.getZoneId();
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if (pl.getZoneId() == zoneId && (team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        localizer.invoke(pl);
                    }
                }

                return;
            }
            case Map: {
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if ((team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        localizer.invoke(pl);
                    }
                }

                return;
            }
            case World: {
                var smap = global.getWorldMgr().getAllSessions();

                for (var session : smap) {
                    var player = session.getPlayer();

                    if (player != null) {
                        if ((team == 0 || player.getTeam() == team) && (!gmOnly || player.isGameMaster())) {
                            localizer.invoke(player);
                        }
                    }
                }

                return;
            }
            case Personal:
                if (whisperTarget == null || !whisperTarget.isPlayer()) {
                    return;
                }

                localizer.invoke(whisperTarget.toPlayer());

                return;
            case Normal:
            default:
                break;
        }

        var dist = getRangeForChatType(msgType);
        var worker = new PlayerDistWorker(source, dist, localizer, gridType.World);
        Cell.visitGrid(source, worker, dist);
    }

    private void sendNonChatPacket(WorldObject source, ServerPacket data, ChatMsg msgType, WorldObject whisperTarget, CreatureTextRange range, Team team, boolean gmOnly) {
        var dist = getRangeForChatType(msgType);

        switch (msgType) {
            case MONSTER_PARTY:
                if (!whisperTarget) {
                    return;
                }

                var whisperPlayer = whisperTarget.toPlayer();

                if (whisperPlayer) {
                    var group = whisperPlayer.getGroup();

                    if (group) {
                        group.broadcastWorker(player -> player.sendPacket(data));
                    }
                }

                return;
            case MONSTER_WHISPER:
            case RAID_BOSS_WHISPER: {
                if (range == CreatureTextRange.NORMAL) //ignores team and gmOnly
                {
                    if (!whisperTarget || !whisperTarget.isTypeId(TypeId.PLAYER)) {
                        return;
                    }

                    whisperTarget.toPlayer().sendPacket(data);

                    return;
                }

                break;
            }
            default:
                break;
        }

        switch (range) {
            case Area: {
                var areaId = source.getAreaId();
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if (pl.getAreaId() == areaId && (team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        pl.sendPacket(data);
                    }
                }

                return;
            }
            case Zone: {
                var zoneId = source.getZoneId();
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if (pl.getZoneId() == zoneId && (team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        pl.sendPacket(data);
                    }
                }

                return;
            }
            case Map: {
                var players = source.getMap().getPlayers();

                for (var pl : players) {
                    if ((team == 0 || pl.getEffectiveTeam() == team) && (!gmOnly || pl.isGameMaster())) {
                        pl.sendPacket(data);
                    }
                }

                return;
            }
            case World: {
                var smap = global.getWorldMgr().getAllSessions();

                for (var session : smap) {
                    var player = session.getPlayer();

                    if (player != null) {
                        if ((team == 0 || player.getTeam() == team) && (!gmOnly || player.isGameMaster())) {
                            player.sendPacket(data);
                        }
                    }
                }

                return;
            }
            case Personal:
                if (whisperTarget == null || !whisperTarget.isPlayer()) {
                    return;
                }

                whisperTarget.toPlayer().sendPacket(data);

                return;
            case Normal:
            default:
                break;
        }

        source.sendMessageToSetInRange(data, dist, true);
    }

    private void sendEmote(Unit source, Emote emote) {
        if (!source) {
            return;
        }

        source.handleEmoteCommand(emote);
    }
}
