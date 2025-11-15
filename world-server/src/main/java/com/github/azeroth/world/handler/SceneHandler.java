package com.github.azeroth.world.handler;

public class SceneHandler {

    void HandleSceneTriggerEvent(WorldPackets::Scenes::SceneTriggerEvent& sceneTriggerEvent)
    {
        TC_LOG_DEBUG("scenes", "HandleSceneTriggerEvent: SceneInstanceID: {} Event: {}", sceneTriggerEvent.SceneInstanceID, sceneTriggerEvent.Event);

        GetPlayer()->GetSceneMgr().OnSceneTrigger(sceneTriggerEvent.SceneInstanceID, sceneTriggerEvent.Event);
    }

    void HandleScenePlaybackComplete(WorldPackets::Scenes::ScenePlaybackComplete& scenePlaybackComplete)
    {
        TC_LOG_DEBUG("scenes", "HandleScenePlaybackComplete: SceneInstanceID: {}", scenePlaybackComplete.SceneInstanceID);

        GetPlayer()->GetSceneMgr().OnSceneComplete(scenePlaybackComplete.SceneInstanceID);
    }

    void HandleScenePlaybackCanceled(WorldPackets::Scenes::ScenePlaybackCanceled& scenePlaybackCanceled)
    {
        TC_LOG_DEBUG("scenes", "HandleScenePlaybackCanceled: SceneInstanceID: {}", scenePlaybackCanceled.SceneInstanceID);

        GetPlayer()->GetSceneMgr().OnSceneCancel(scenePlaybackCanceled.SceneInstanceID);
    }
}
