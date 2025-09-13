package com.github.azeroth.game;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.Map;

class GameEvents {
    public static void trigger(int gameEventId, WorldObject source, WorldObject target) {
        var refForMapAndZoneScript = source != null ? source : target;

        var zoneScript = refForMapAndZoneScript.getZoneScript();

        if (zoneScript == null && refForMapAndZoneScript.isPlayer()) {
            zoneScript = refForMapAndZoneScript.findZoneScript();
        }

        if (zoneScript != null) {
            zoneScript.processEvent(target, gameEventId, source);
        }

        var map = refForMapAndZoneScript.getMap();
        var goTarget = target == null ? null : target.toGameObject();

        if (goTarget != null) {
            var goAI = goTarget.getAI();

            if (goAI != null) {
                goAI.eventInform(gameEventId);
            }
        }

        var sourcePlayer = source == null ? null : source.toPlayer();

        if (sourcePlayer != null) {
            triggerForPlayer(gameEventId, sourcePlayer);
        }

        triggerForMap(gameEventId, map, source, target);
    }

    public static void triggerForPlayer(int gameEventId, Player source) {
        var map = source.getMap();

        if (map.isInstanceable()) {
            source.startCriteriaTimer(CriteriaStartEvent.SendEvent, gameEventId);
            source.resetCriteria(CriteriaFailEvent.SendEvent, gameEventId);
        }

        source.updateCriteria(CriteriaType.PlayerTriggerGameEvent, gameEventId, 0, 0, source);

        if (map.isScenario()) {
            source.updateCriteria(CriteriaType.AnyoneTriggerGameEventScenario, gameEventId, 0, 0, source);
        }
    }


    public static void triggerForMap(int gameEventId, Map map, WorldObject source) {
        triggerForMap(gameEventId, map, source, null);
    }

    public static void triggerForMap(int gameEventId, Map map) {
        triggerForMap(gameEventId, map, null, null);
    }

    public static void triggerForMap(int gameEventId, Map map, WorldObject source, WorldObject target) {
        map.scriptsStart(ScriptsType.event, gameEventId, source, target);
    }
}
