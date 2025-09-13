package com.github.azeroth.game.map.grid.visitor;

import com.github.azeroth.common.YieldResult;
import com.github.azeroth.defines.GameObjectType;
import com.github.azeroth.defines.Team;
import com.github.azeroth.game.domain.map.Coordinate;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.NotifyFlag;
import com.github.azeroth.game.domain.unit.DeathState;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.FindCreatureOptions;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.object.update.UpdateData;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.map.grid.Cell;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GridVisitors {


    public static GridVisitor OBJECT_GRID_EVACUATOR = (source) -> {
        if (source.isWorldObject()) {
            return GridVisitorResult.CONTINUE;
        }
        if (source.isCreature()) {
            // creature in unloading grid can have respawn point in another grid
            // if it will be unloaded then it will not respawn in original grid until unload/load original grid
            // move to respawn point to prevent this case. For player view in respawn grid this will be normal respawn.
            source.toCreature().getMap().creatureRespawnRelocation(source.toCreature(), true);
        } else if (source.isGameObject()) {
            // GameObject in unloading grid can have respawn point in another grid
            // if it will be unloaded then it will not respawn in original grid until unload/load original grid
            // move to respawn point to prevent this case. For player view in respawn grid this will be normal respawn.
            source.toGameObject().getMap().gameObjectRespawnRelocation(source.toGameObject(), true);
        }
        return GridVisitorResult.CONTINUE;
    };


    public static GridVisitor OBJECT_GRID_CLEANER = (source) -> {
        if (!source.isWorldObject()) {
            source.setDestroyedObject(true);
            source.cleanupsBeforeDelete();
            return GridVisitorResult.CONTINUE;
        }
        return GridVisitorResult.CONTINUE;
    };


    public static GridVisitor OBJECT_GRID_UN_LOADER = (source) -> {
        //Some creatures may summon other temp summons in CleanupsBeforeDelete()
        //So we need this even after cleaner (maybe we can remove cleaner)
        //Example: Flame Leviathan Turret 33139 is summoned when a creature is deleted
        /// @todo Check if that script has the correct logic. Do we really need to summons something before deleting?
        if (!source.isWorldObject()) {
            source.cleanupsBeforeDelete();
        }
        return GridVisitorResult.CONTINUE;
    };


    public static GridVisitor objectUpdater(int delta) {
        return (source) -> {
            if (source.isPlayer() || source.isCorpse()) {
                return GridVisitorResult.CONTINUE;
            }

            if (source.isInWorld()) {
                source.update(delta);
            }
            return GridVisitorResult.CONTINUE;
        };
    }


    public static VisibleNotifier newPlayerRelocationNotifier(Player player) {
        boolean relocated_for_ai = (player == player.getSeer());
        return new VisibleNotifier(player) {
            @Override
            public GridVisitorResult visit(WorldObject source) {
                if (source.isPlayer()) {
                    var playerSource = source.toPlayer();

                    super.visit(source);

                    if (playerSource.getSeer().isNeedNotify(NotifyFlag.VISIBILITY_CHANGED))
                        return GridVisitorResult.CONTINUE;

                    playerSource.updateVisibilityOf(player);

                } else if (source.isCreature()) {
                    var creature = source.toCreature();
                    super.visit(source);
                    if (relocated_for_ai && !creature.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED))
                        creatureUnitRelocationWorker(creature, player);
                }

                return GridVisitorResult.CONTINUE;
            }
        };
    }


    public static GridVisitor newCreatureRelocationNotifier(Creature creature) {

        return source -> {
            if (source.isPlayer()) {
                var playerSource = source.toPlayer();

                if (!playerSource.getSeer().isNeedNotify(NotifyFlag.VISIBILITY_CHANGED))
                    playerSource.updateVisibilityOf(creature);

                creatureUnitRelocationWorker(creature, playerSource);

            } else if (source.isCreature()) {
                var c = source.toCreature();
                creatureUnitRelocationWorker(c, source.toCreature());

                if (!creature.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED))
                    creatureUnitRelocationWorker(c, creature);
            }

            return GridVisitorResult.CONTINUE;
        };

    }

    public static WorldObjectVisitor newVisibleChangesNotifier(WorldObject[] objects) {
        return source -> {
            switch (source.getObjectTypeId()) {
                case PLAYER:
                    Player sourcePlayer = source.toPlayer();
                    sourcePlayer.updateVisibilityOf(List.of(objects));
                    sourcePlayer.getSharedVisionList().forEach(visionPlayer -> {
                        if (visionPlayer.getSeer() == sourcePlayer)
                            visionPlayer.updateVisibilityOf(List.of(objects));
                    });
                case UNIT:

                    Creature sourceCreature = source.toCreature();
                    sourceCreature.getSharedVisionList().forEach(visionPlayer -> {
                        if (visionPlayer.getSeer() == sourceCreature)
                            visionPlayer.updateVisibilityOf(List.of(objects));
                    });

                case DYNAMIC_OBJECT:

                    DynamicObject sourceDynObject = source.toDynObject();
                    Unit caster = sourceDynObject.getCaster();
                    if (caster != null && caster.isPlayer() && caster.toPlayer().getSeer() == sourceDynObject) {
                        caster.toPlayer().updateVisibilityOf(List.of(objects));
                    }
                    break;
            }
            return GridVisitorResult.CONTINUE;
        };
    }

    public static WorldObjectVisitor newWorldObjectChangeAccumulator(WorldObject object, HashMap<Player, UpdateData> data) {

        Set<ObjectGuid> guidSet = new HashSet<>();
        Consumer<Player> buildPacket = player -> {
            if (!guidSet.contains(player.getGUID()) && player.haveAtClient(object)) {
                object.buildFieldsUpdate(player, data);
                guidSet.add(player.getGUID());
            }
        };

        return source -> {
            switch (source.getObjectTypeId()) {
                case PLAYER:
                    Player sourcePlayer = source.toPlayer();
                    buildPacket.accept(sourcePlayer);
                    sourcePlayer.getSharedVisionList().forEach(buildPacket);
                case UNIT:
                    Creature sourceCreature = source.toCreature();
                    sourceCreature.getSharedVisionList().forEach(buildPacket);
                case DYNAMIC_OBJECT:
                    DynamicObject sourceDynObject = source.toDynObject();
                    ObjectGuid guid = sourceDynObject.getCasterGUID();
                    if (guid.isPlayer()) {
                        //Caster may be nullptr if DynObj is in removelist
                        Player caster = object.getWorldContext().findPlayer(guid);
                        if(caster != null && caster.getActivePlayerData().getFarsightObject() == source.getGUID())
                            buildPacket.accept(caster);
                    }
                    break;
            }
            return GridVisitorResult.CONTINUE;
        };
    }

    public static GridVisitor newDelayedUnitRelocation(Cell c, Coordinate p, Map map, float radius) {
        return (source) -> {
            if (source.isPlayer()) {
                var player = source.toPlayer();
                var viewPoint = player.getSeer();

                if (!viewPoint.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED))
                    return GridVisitorResult.CONTINUE;

                if (player != viewPoint && !viewPoint.getLocation().isPositionValid())
                    return GridVisitorResult.CONTINUE;

                VisibleNotifier notifier = newPlayerRelocationNotifier(player);
                Cell.visitGrid(viewPoint, notifier, radius, false);
                notifier.sendToSelf();

            } else if (source.isCreature()) {
                var creature = source.toCreature();
                if (!creature.isNeedNotify(NotifyFlag.VISIBILITY_CHANGED)) {
                    return GridVisitorResult.CONTINUE;
                }
                GridVisitor gridVisitor = newCreatureRelocationNotifier(creature);

                c.visit(p, gridVisitor, map, creature, radius);
                return GridVisitorResult.CONTINUE;
            }
            return GridVisitorResult.CONTINUE;
        };
    }

    public static GridVisitor newResetVisitor() {
        return (source) -> {
            if (source.isPlayer() || source.isCreature()) {
                source.resetAllNotifies();

            }
            return GridVisitorResult.CONTINUE;
        };
    }


    public static GridVisitor newMessageDistDeliverer(WorldObject src, Consumer<Player> deliverer, float dist) {
        return newMessageDistDeliverer(src, deliverer, dist, false, null, false);
    }


    public static WorldObjectVisitor newMessageDistDeliverer(WorldObject src, Consumer<Player> deliverer, float dist,
                                                             boolean ownTeamOnly, Player skipped, boolean req3dDist) {
        Consumer<Player> playerConsumer = (player) -> {
            Optional<Team> team = Optional.empty();
            if (ownTeamOnly && src.toPlayer() != null) {
                team = Optional.of(src.toPlayer().getEffectiveTeam());
            }

            if (player == src || (team.isPresent() && player.getEffectiveTeam() != team.get()) || skipped == player)
                return;

            if (!player.haveAtClient(src))
                return;
            deliverer.accept(player);
        };

        return (source) -> {
            if (source.isPlayer()) {
                Player target = source.toPlayer();
                if (!target.inSamePhase(src.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if ((!req3dDist ? target.getLocation().getExactDist2DSq(src.getLocation()) : target.getLocation().getExactDistSq(src.getLocation())) > dist)
                    return GridVisitorResult.CONTINUE;

                // Send packet to all who are sharing the player's vision
                if (target.hasSharedVision()) {

                    for (Player item : target.getSharedVisionList()) {
                        if (item.getSeer() == target) {
                            playerConsumer.accept(item);
                        }
                    }
                }
                if (target.getSeer() == target || target.getVehicle() != null)
                    playerConsumer.accept(target);

            } else if (source.isCreature()) {
                Creature target = source.toCreature();

                if (!target.inSamePhase(src.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if ((!req3dDist ? target.getLocation().getExactDist2DSq(source.getLocation()) : target.getLocation().getExactDistSq(source.getLocation())) > dist)
                    return GridVisitorResult.CONTINUE;

                // Send packet to all who are sharing the creature's vision
                if (target.hasSharedVision()) {
                    for (Player item : target.getSharedVisionList()) {
                        if (item.getSeer() == target) {
                            playerConsumer.accept(item);
                        }
                    }
                }
            } else if (source.isDynObject()) {
                DynamicObject target = source.toDynObject();
                if (!target.inSamePhase(src.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if ((!req3dDist ? target.getLocation().getExactDist2DSq(source.getLocation()) : target.getLocation().getExactDistSq(source.getLocation())) > dist)
                    return GridVisitorResult.CONTINUE;

                Unit caster = target.getCaster();
                if (caster != null) {
                    // Send packet back to the caster if the caster has vision of dynamic object
                    Player player = caster.toPlayer();
                    if (player != null && player.getSeer() == target)
                        playerConsumer.accept(player);
                }
            }
            return GridVisitorResult.CONTINUE;
        };
    }


    public static GridVisitor newCreatureLastSearcher(WorldObject object, int entry, float range, boolean alive, YieldResult<Creature> yieldResult) {

        float[] updatableRange = new float[]{range};
        Predicate<Creature> checker = (creature) -> {
            if (creature.getDeathState() != DeathState.DEAD
                    && creature.getEntry() == entry
                    && creature.isAlive() == alive
                    && creature.getGUID() != object.getGUID()
                    && object.isWithinDist(creature, updatableRange[0])
                    && creature.checkPrivateObjectOwnerVisibility(object)) {
                updatableRange[0] = object.getDistance(creature);         // use found unit range as new range limit for next check
                return true;
            }
            return false;
        };

        return newCreatureLastSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newCreatureListSearcher(WorldObject object, int entry, float range, List<Creature> yieldResult) {
        Predicate<Creature> checker = creature -> {
            if (entry != 0 && creature.getEntry() != entry) {
                return false;
            }

            if (range != 0.0f) {
                if (range > 0.0f && !object.isWithinDist(creature, range))
                    return false;
                return !(range < 0.0f) || !object.isWithinDist(creature, -range);
            }
            return true;
        };
        return newCreatureListSearcher(object, checker, yieldResult);
    }

    public static GridVisitor newCreatureListSearcher(WorldObject object, int[] entry, float range, List<Creature> yieldResult) {
        Predicate<Creature> checker = creature -> {
            if (entry != null && Arrays.stream(entry).noneMatch(e -> e == creature.getEntry())) {
                return false;
            }

            if (range != 0.0f) {
                if (range > 0.0f && !object.isWithinDist(creature, range))
                    return false;
                return !(range < 0.0f) || !object.isWithinDist(creature, -range);
            }
            return true;
        };
        return newCreatureListSearcher(object, checker, yieldResult);
    }

    public static GridVisitor newCreatureListSearcher(WorldObject object, float range, FindCreatureOptions options, List<Creature> yieldResult) {
        Predicate<Creature> checker = newCreatureOptionsPredicate(object, options)
                .and((creature) -> object.isWithinDist(creature, range));
        return newCreatureListSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newCreatureLastSearcher(WorldObject object, float range, FindCreatureOptions options, YieldResult<Creature> yieldResult) {
        float[] updatableRange = new float[]{range};
        Predicate<Creature> checker = newCreatureOptionsPredicate(object, options)
                .and((creature) -> {
                    boolean withinDist = object.isWithinDist(creature, updatableRange[0]);
                    updatableRange[0] = object.getDistance(creature);
                    return withinDist;
                });
        return newCreatureLastSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newGameObjectLastSearcher(WorldObject object, int entry, float range, boolean spawnedOnly, YieldResult<GameObject> yieldResult) {
        float[] updatableRange = new float[]{range};
        Predicate<GameObject> checker = (go) -> {
            if ((!spawnedOnly || go.isSpawned()) && go.getEntry() == entry && go.getGUID() != object.getGUID() && object.isWithinDist(go, updatableRange[0])) {
                updatableRange[0] = object.getDistance(go);        // use found GO range as new range limit for next check
                return true;
            }
            return false;
        };

        return newGameObjectLastSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newGameObjectLastSearcher(WorldObject object, GameObjectType type, float range, YieldResult<GameObject> yieldResult) {
        float[] updatableRange = new float[]{range};
        Predicate<GameObject> checker = (go) -> {
            if (go.getGoType() == type && object.isWithinDist(go, updatableRange[0])) {
                updatableRange[0] = object.getDistance(go);        // use found GO range as new range limit for next check
                return true;
            }
            return false;
        };

        return newGameObjectLastSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newGameObjectLastSearcher(WorldObject object, int entry, float range, YieldResult<GameObject> yieldResult) {
        float[] updatableRange = new float[]{range};
        Predicate<GameObject> checker = (go) -> {
            if (!go.isSpawned() && go.getEntry() == entry && go.getGUID() != object.getGUID() && object.isWithinDist(go, updatableRange[0])) {
                updatableRange[0] = object.getDistance(go);        // use found GO range as new range limit for next check
                return true;
            }
            return false;
        };

        return newGameObjectLastSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newGameObjectListSearcher(WorldObject object, int entry, float range, List<GameObject> yieldResult) {
        Predicate<GameObject> checker = (go) -> (entry == 0 || go.getEntry() == entry) && object.isWithinDist(go, range, false);
        return newGameObjectListSearcher(object, checker, yieldResult);
    }


    public static GridVisitor newPlayerLastSearcher(WorldObject object, float range, YieldResult<Player> yieldResult) {
        float[] updatableRange = new float[]{range};
        Predicate<Player> checker = (player) -> {
            if (player.isAlive() && object.isWithinDist(player, updatableRange[0])) {
                updatableRange[0] = object.getDistance(player);
                return true;
            }
            return false;
        };

        return newPlayerLastSearcher(object, checker, yieldResult);
    }

    public static GridVisitor newPlayerListSearcher(WorldObject object, float range, boolean alive, List<Player> yieldResult) {
        Predicate<Player> checker = (player) -> alive == player.isAlive() && object.isWithinDist(player, range);
        return newPlayerListSearcher(object, checker, yieldResult);
    }


    private static Predicate<Creature> newCreatureOptionsPredicate(WorldObject object, FindCreatureOptions options) {
        return (creature) -> {
            if (creature.getDeathState() == DeathState.DEAD) // Despawned
                return false;

            if (creature.getGUID() == object.getGUID())
                return false;

            if (options.creatureId != null && creature.getEntry() != options.creatureId)
                return false;

            if (options.stringId != null && !creature.hasStringId(options.stringId))
                return false;

            if (options.isAlive != null && creature.isAlive() != options.isAlive)
                return false;

            if (options.isSummon != null && creature.isSummon() != options.isSummon)
                return false;

            if (options.isInCombat != null && creature.isInCombat() != options.isInCombat)
                return false;

            if ((options.ownerGuid != null && creature.getOwnerGUID() != options.ownerGuid)
                    || (options.charmerGuid != null && creature.getCharmerGUID() != options.charmerGuid)
                    || (options.creatorGuid != null && creature.getCreatorGUID() != options.creatorGuid)
                    || (options.demonCreatorGuid != null && creature.getDemonCreatorGUID() != options.demonCreatorGuid)
                    || (options.privateObjectOwnerGuid != null && creature.getPrivateObjectOwner() != options.privateObjectOwnerGuid))
                return false;

            if (options.ignorePrivateObjects && creature.isPrivateObject())
                return false;

            if (options.ignoreNotOwnedPrivateObjects && !creature.checkPrivateObjectOwnerVisibility(object))
                return false;

            return options.auraSpellId == null || creature.hasAura(options.auraSpellId);
        };
    }

    private static GridVisitor newCreatureLastSearcher(WorldObject object, Predicate<Creature> checker, YieldResult<Creature> yieldResult) {
        return source -> {
            if (source.isCreature()) {
                var c = source.toCreature();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {

                    yieldResult.set(c);
                    return GridVisitorResult.TERMINATE;
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }

    private static GridVisitor newCreatureListSearcher(WorldObject object, Predicate<Creature> checker, List<Creature> yieldResult) {
        return source -> {
            if (source.isCreature()) {
                var c = source.toCreature();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {
                    yieldResult.add(c);
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }


    private static GridObjectVisitor newGameObjectLastSearcher(WorldObject object, Predicate<GameObject> checker, YieldResult<GameObject> yieldResult) {
        return source -> {
            if (source.isGameObject()) {
                var c = source.toGameObject();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {

                    yieldResult.set(c);
                    return GridVisitorResult.TERMINATE;
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }


    private static GridObjectVisitor newGameObjectListSearcher(WorldObject object, Predicate<GameObject> checker, List<GameObject> yieldResult) {
        return source -> {
            if (source.isGameObject()) {
                var c = source.toGameObject();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {
                    yieldResult.add(c);
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }


    private static WorldObjectVisitor newPlayerLastSearcher(WorldObject object, Predicate<Player> checker, YieldResult<Player> yieldResult) {
        return source -> {
            if (source.isPlayer()) {
                var c = source.toPlayer();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {

                    yieldResult.set(c);
                    return GridVisitorResult.TERMINATE;
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }

    private static WorldObjectVisitor newPlayerListSearcher(WorldObject object, Predicate<Player> checker, List<Player> yieldResult) {
        return source -> {
            if (source.isPlayer()) {
                var c = source.toPlayer();
                if (!c.inSamePhase(object.getPhaseShift()))
                    return GridVisitorResult.CONTINUE;

                if (checker.test(c)) {
                    yieldResult.add(c);
                }
            }

            return GridVisitorResult.CONTINUE;
        };
    }

    private static void creatureUnitRelocationWorker(Creature c, Unit u) {
        if (!u.isAlive() || !c.isAlive() || c == u || u.isInFlight()) {
            return;
        }

        if (!c.hasUnitState(UnitState.SIGHTLESS)) {
            if (c.isAIEnabled() && c.canSeeOrDetect(u, false, true)) {
                c.getAI().moveInLineOfSight_Safe(u);
            } else {
                if (u.isPlayer() && u.getHasStealthAura() && c.isAIEnabled() && c.canSeeOrDetect(u, false, true, true)) {
                    c.getAI().triggerAlert(u);
                }
            }
        }
    }
}
