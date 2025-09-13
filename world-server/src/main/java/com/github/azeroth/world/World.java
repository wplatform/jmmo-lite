package com.github.azeroth.world;

import com.github.azeroth.common.Locale;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.world.entities.object.enums.HighGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class World {


    private final EnumMap<HighGuid, AtomicInteger> guidGenerators = new EnumMap<>(HighGuid.class);


    @Value("${worldserver.dbc.locale}")
    private Locale dbcLocale;

    @Autowired
    private DbcObjectManager dbcObjectManager;



    public static int maxLowValue(HighGuid high) {
        if (Objects.requireNonNull(high) == HighGuid.Transport) {
            return 0xFFFFF;
        }
        return 0xFFFFFFFF;
    }


    private final EntityManager em;

    private final ComponentManager cm;

    final BatchChangeProcessor batchProcessor;

    final Bag<BaseSystem> systemsBag;

    final AspectSubscriptionManager asm;

    SystemInvocationStrategy invocationStrategy;


    public float delta;
    final boolean alwaysDelayComponentRemoval;

    public World() {
        this(new WorldConfiguration());
    }


    public World(WorldConfiguration configuration) {
        partition = new com.github.azeroth.world.test.World.WorldSegment(configuration);
        systemsBag = configuration.systems;

        final ComponentManager lcm =
                (ComponentManager) systemsBag.get(COMPONENT_MANAGER_IDX);
        final EntityManager lem =
                (EntityManager) systemsBag.get(ENTITY_MANAGER_IDX);
        final AspectSubscriptionManager lasm =
                (AspectSubscriptionManager) systemsBag.get(ASPECT_SUBSCRIPTION_MANAGER_IDX);

        cm = lcm == null ? new ComponentManager(configuration.expectedEntityCount()) : lcm;
        em = lem == null ? new EntityManager(configuration.expectedEntityCount()) : lem;
        asm = lasm == null ? new AspectSubscriptionManager() : lasm;
        batchProcessor = new BatchChangeProcessor(this);
        alwaysDelayComponentRemoval = configuration.isAlwaysDelayComponentRemoval();

        configuration.initialize(this, partition.injector, asm);
    }


    public void dispose() {
        List<Throwable> exceptions = new ArrayList<>();

        for (BaseSystem system : systemsBag) {
            try {
                system.dispose();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (exceptions.size() > 0)
            throw new ArtemisMultiException(exceptions);
    }


    public EntityEdit edit(int entityId) {
        if (!em.isActive(entityId))
            throw new RuntimeException("Issued edit on deleted " + entityId);

        return batchProcessor.obtainEditor(entityId);
    }


    public int compositionId(int entityId) {
        return cm.getIdentity(entityId);
    }


    public EntityManager getEntityManager() {
        return em;
    }


    public ComponentManager getComponentManager() {
        return cm;
    }


    public AspectSubscriptionManager getAspectSubscriptionManager() {
        return asm;
    }




    public void deleteEntity(Entity e) {
        delete(e.id);
    }


    public void delete(int entityId) {
        batchProcessor.delete(entityId);
    }

    public Entity createEntity() {
        Entity e = em.createEntityInstance();
        batchProcessor.changed.unsafeSet(e.getId());
        return e;
    }


    public int create() {
        int entityId = em.create();
        batchProcessor.changed.unsafeSet(entityId);
        return entityId;
    }


    public Entity createEntity(Archetype archetype) {
        Entity e = em.createEntityInstance();

        int id = e.getId();
        archetype.transmuter.perform(id);
        cm.setIdentity(e.id, archetype.compositionId);

        batchProcessor.changed.unsafeSet(id);

        return e;
    }


    public int create(Archetype archetype) {
        int entityId = em.create();

        archetype.transmuter.perform(entityId);
        cm.setIdentity(entityId, archetype.compositionId);

        batchProcessor.changed.unsafeSet(entityId);

        return entityId;
    }


    public Entity getEntity(int entityId) {
        return em.getEntity(entityId);
    }


    public ImmutableBag<BaseSystem> getSystems() {
        return systemsBag;
    }


    public <T extends BaseSystem> T getSystem(Class<T> type) {
        return (T) partition.systems.get(type);
    }

    protected void setInvocationStrategy(SystemInvocationStrategy invocationStrategy) {
        this.invocationStrategy = invocationStrategy;
        invocationStrategy.setWorld(this);
        invocationStrategy.setSystems(systemsBag);
        invocationStrategy.initialize();
    }

    public void process() {
        invocationStrategy.process();

        IntBag pendingPurge = batchProcessor.getPendingPurge();
        if (!pendingPurge.isEmpty()) {
            cm.clean(pendingPurge);
            em.clean(pendingPurge);

            batchProcessor.purgeComponents();
        }
    }


    public <T extends com.github.azeroth.world.test.Component> ComponentMapper<T> getMapper(Class<T> type) {
        return cm.getMapper(type);
    }


    public <T extends SystemInvocationStrategy> T getInvocationStrategy() {
        return (T) invocationStrategy;
    }}
