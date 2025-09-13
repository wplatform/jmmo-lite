package com.github.azeroth.game.entity.scene;


import com.github.azeroth.game.domain.scene.SceneTemplate;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.CancelScene;
import com.github.azeroth.game.networking.packet.PlayScene;
import com.github.azeroth.game.listener.interfaces.iscene.ISceneOnSceneChancel;
import com.github.azeroth.game.listener.interfaces.iscene.ISceneOnSceneComplete;
import com.github.azeroth.game.listener.interfaces.iscene.ISceneOnSceneStart;
import com.github.azeroth.game.listener.interfaces.iscene.ISceneOnSceneTrigger;

import java.util.ArrayList;
import java.util.HashMap;


public class SceneMgr {
    private final Player player;
    private final HashMap<Integer, SceneTemplate> scenesByInstance = new HashMap<Integer, SceneTemplate>();
    private final ArrayList<ServerPacket> delayedScenes = new ArrayList<>();
    private int standaloneSceneInstanceId;
    private boolean isDebuggingScenes;

    public SceneMgr(Player player) {
        player = player;
        standaloneSceneInstanceId = 0;
        isDebuggingScenes = false;
    }

    private Player getPlayer() {
        return player;
    }

    public final int playScene(int sceneId) {
        return playScene(sceneId, null);
    }

    public final int playScene(int sceneId, Position position) {
        var sceneTemplate = global.getObjectMgr().getSceneTemplate(sceneId);

        return playSceneByTemplate(sceneTemplate, position);
    }


    public final int playSceneByTemplate(SceneTemplate sceneTemplate) {
        return playSceneByTemplate(sceneTemplate, null);
    }

    public final int playSceneByTemplate(SceneTemplate sceneTemplate, Position position) {
        if (sceneTemplate == null) {
            return 0;
        }

        var entry = CliDB.SceneScriptPackageStorage.get(sceneTemplate.scenePackageId);

        if (entry == null) {
            return 0;
        }

        // By default, take player position
        if (position == null) {
            position = getPlayer().getLocation();
        }

        var sceneInstanceId = getNewStandaloneSceneInstanceId();

        if (isDebuggingScenes) {
            getPlayer().sendSysMessage(SysMessage.CommandSceneDebugPlay, sceneInstanceId, sceneTemplate.scenePackageId, sceneTemplate.playbackFlags);
        }

        PlayScene playScene = new playScene();
        playScene.sceneID = sceneTemplate.sceneId;
        playScene.playbackFlags = (int) sceneTemplate.playbackFlags.getValue();
        playScene.sceneInstanceID = sceneInstanceId;
        playScene.sceneScriptPackageID = sceneTemplate.scenePackageId;
        playScene.location = position;
        playScene.transportGUID = getPlayer().getTransGUID();
        playScene.encrypted = sceneTemplate.encrypted;
        playScene.write();

        if (getPlayer().isInWorld()) {
            getPlayer().sendPacket(playScene);
        } else {
            delayedScenes.add(playScene);
        }

        addInstanceIdToSceneMap(sceneInstanceId, sceneTemplate);

        global.getScriptMgr().<ISceneOnSceneStart>RunScript(script -> script.OnSceneStart(getPlayer(), sceneInstanceId, sceneTemplate), sceneTemplate.scriptId);

        return sceneInstanceId;
    }


    public final int playSceneByPackageId(int sceneScriptPackageId, SceneFlags playbackflags) {
        return playSceneByPackageId(sceneScriptPackageId, playbackflags, null);
    }

    public final int playSceneByPackageId(int sceneScriptPackageId, SceneFlags playbackflags, Position position) {
        SceneTemplate sceneTemplate = new SceneTemplate();
        sceneTemplate.sceneId = 0;
        sceneTemplate.scenePackageId = sceneScriptPackageId;
        sceneTemplate.playbackFlags = playbackflags;
        sceneTemplate.encrypted = false;
        sceneTemplate.scriptId = 0;

        return playSceneByTemplate(sceneTemplate, position);
    }

    public final void onSceneTrigger(int sceneInstanceId, String triggerName) {
        if (!hasScene(sceneInstanceId)) {
            return;
        }

        if (isDebuggingScenes) {
            getPlayer().sendSysMessage(SysMessage.CommandSceneDebugTrigger, sceneInstanceId, triggerName);
        }

        var sceneTemplate = getSceneTemplateFromInstanceId(sceneInstanceId);
        global.getScriptMgr().<ISceneOnSceneTrigger>RunScript(script -> script.OnSceneTriggerEvent(getPlayer(), sceneInstanceId, sceneTemplate, triggerName), sceneTemplate.scriptId);
    }

    public final void onSceneCancel(int sceneInstanceId) {
        if (!hasScene(sceneInstanceId)) {
            return;
        }

        if (isDebuggingScenes) {
            getPlayer().sendSysMessage(SysMessage.CommandSceneDebugCancel, sceneInstanceId);
        }

        var sceneTemplate = getSceneTemplateFromInstanceId(sceneInstanceId);

        if (sceneTemplate.playbackFlags.hasFlag(SceneFlags.NotCancelable)) {
            return;
        }

        // Must be done before removing aura
        removeSceneInstanceId(sceneInstanceId);

        if (sceneTemplate.sceneId != 0) {
            removeAurasDueToSceneId(sceneTemplate.sceneId);
        }

        global.getScriptMgr().<ISceneOnSceneChancel>RunScript(script -> script.onSceneCancel(getPlayer(), sceneInstanceId, sceneTemplate), sceneTemplate.scriptId);

        if (sceneTemplate.playbackFlags.hasFlag(SceneFlags.FadeToBlackscreenOnCancel)) {
            cancelScene(sceneInstanceId, false);
        }
    }

    public final void onSceneComplete(int sceneInstanceId) {
        if (!hasScene(sceneInstanceId)) {
            return;
        }

        if (isDebuggingScenes) {
            getPlayer().sendSysMessage(SysMessage.CommandSceneDebugComplete, sceneInstanceId);
        }

        var sceneTemplate = getSceneTemplateFromInstanceId(sceneInstanceId);

        // Must be done before removing aura
        removeSceneInstanceId(sceneInstanceId);

        if (sceneTemplate.sceneId != 0) {
            removeAurasDueToSceneId(sceneTemplate.sceneId);
        }

        global.getScriptMgr().<ISceneOnSceneComplete>RunScript(script -> script.onSceneComplete(getPlayer(), sceneInstanceId, sceneTemplate), sceneTemplate.scriptId);

        if (sceneTemplate.playbackFlags.hasFlag(SceneFlags.FadeToBlackscreenOnComplete)) {
            cancelScene(sceneInstanceId, false);
        }
    }

    public final void cancelSceneBySceneId(int sceneId) {
        ArrayList<Integer> instancesIds = new ArrayList<>();

        for (var pair : scenesByInstance.entrySet()) {
            if (pair.getValue().sceneId == sceneId) {
                instancesIds.add(pair.getKey());
            }
        }

        for (var sceneInstanceId : instancesIds) {
            cancelScene(sceneInstanceId);
        }
    }

    public final void cancelSceneByPackageId(int sceneScriptPackageId) {
        ArrayList<Integer> instancesIds = new ArrayList<>();

        for (var sceneTemplate : scenesByInstance.entrySet()) {
            if (sceneTemplate.getValue().scenePackageId == sceneScriptPackageId) {
                instancesIds.add(sceneTemplate.getKey());
            }
        }

        for (var sceneInstanceId : instancesIds) {
            cancelScene(sceneInstanceId);
        }
    }


    public final int getActiveSceneCount() {
        return getActiveSceneCount(0);
    }

    public final int getActiveSceneCount(int sceneScriptPackageId) {
        int activeSceneCount = 0;

        for (var sceneTemplate : scenesByInstance.values()) {
            if (sceneScriptPackageId == 0 || sceneTemplate.scenePackageId == sceneScriptPackageId) {
                ++activeSceneCount;
            }
        }

        return activeSceneCount;
    }

    public final void triggerDelayedScenes() {
        for (var playScene : delayedScenes) {
            getPlayer().sendPacket(playScene);
        }

        delayedScenes.clear();
    }

    public final HashMap<Integer, SceneTemplate> getSceneTemplateByInstanceMap() {
        return scenesByInstance;
    }

    public final void toggleDebugSceneMode() {
        isDebuggingScenes = !isDebuggingScenes;
    }

    public final boolean isInDebugSceneMode() {
        return isDebuggingScenes;
    }


    private void cancelScene(int sceneInstanceId) {
        cancelScene(sceneInstanceId, true);
    }

    private void cancelScene(int sceneInstanceId, boolean removeFromMap) {
        if (removeFromMap) {
            removeSceneInstanceId(sceneInstanceId);
        }

        CancelScene cancelScene = new cancelScene();
        cancelScene.sceneInstanceID = sceneInstanceId;
        getPlayer().sendPacket(cancelScene);
    }


    private boolean hasScene(int sceneInstanceId) {
        return hasScene(sceneInstanceId, 0);
    }

    private boolean hasScene(int sceneInstanceId, int sceneScriptPackageId) {
        var sceneTempalte = scenesByInstance.get(sceneInstanceId);

        if (sceneTempalte != null) {
            return sceneScriptPackageId == 0 || sceneScriptPackageId == sceneTempalte.scenePackageId;
        }

        return false;
    }

    private void addInstanceIdToSceneMap(int sceneInstanceId, SceneTemplate sceneTemplate) {
        scenesByInstance.put(sceneInstanceId, sceneTemplate);
    }

    private void removeSceneInstanceId(int sceneInstanceId) {
        scenesByInstance.remove(sceneInstanceId);
    }

    private void removeAurasDueToSceneId(int sceneId) {
        var scenePlayAuras = getPlayer().getAuraEffectsByType(AuraType.PlayScene);

        for (var scenePlayAura : scenePlayAuras) {
            if (scenePlayAura.getMiscValue() == sceneId) {
                getPlayer().removeAura(scenePlayAura.getBase());

                break;
            }
        }
    }

    private SceneTemplate getSceneTemplateFromInstanceId(int sceneInstanceId) {
        return scenesByInstance.get(sceneInstanceId);
    }


    private void recreateScene(int sceneScriptPackageId, SceneFlags playbackflags) {
        recreateScene(sceneScriptPackageId, playbackflags, null);
    }

    private void recreateScene(int sceneScriptPackageId, SceneFlags playbackflags, Position position) {
        cancelSceneByPackageId(sceneScriptPackageId);
        playSceneByPackageId(sceneScriptPackageId, playbackflags, position);
    }

    private int getNewStandaloneSceneInstanceId() {
        return ++standaloneSceneInstanceId;
    }
}
