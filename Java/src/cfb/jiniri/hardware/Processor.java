package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.model.Singularity;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;
import cfb.jiniri.util.Converter;

import java.util.*;
import java.util.concurrent.*;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private static final Singlet CREATION = Singlet.MINUS_ONE;
    private static final Singlet EVOLUTION = Singlet.ZERO;
    private static final Singlet DESTRUCTION = Singlet.PLUS_ONE;

    private static final class EntityEnvelope {

        final Entity entity;

        final Set<Environment> environments;
        final SortedSet<Effect> effects;

        EntityEnvelope(final Entity entity, final Singlet[] data) {

            this.entity = entity;

            environments = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();

            if (data != null) {

                effects.add(new Effect(data));
            }
        }

        EntityEnvelope(final Entity entity) {

            this(entity, null);
        }
    }

    private final BlockingQueue<Core> cores;

    private final Singlet time;

    private Singularity singularity;

    private final Map<Nonet, EntityEnvelope> entityEnvelopes;
    private final Map<Nonet, Environment> environments;

    private final ExecutorService processorExecutor;
    private final ExecutorService coreExecutor;

    private final Queue<Runnable> immediateCalls;
    private final Queue<Runnable> deferredCalls;

    private boolean isShuttingDown;
    private Storage storage;

    public Processor(final int numberOfCores, final int coreMemoryCapacity) {

        cores = new ArrayBlockingQueue<>(numberOfCores);
        for (int i = 0; i < numberOfCores; i++) {

            cores.offer(new Core(this, coreMemoryCapacity));
        }

        time = new Singlet();

        entityEnvelopes = new ConcurrentHashMap<>();
        environments = new ConcurrentHashMap<>();

        processorExecutor = Executors.newSingleThreadExecutor();
        coreExecutor = Executors.newFixedThreadPool(numberOfCores);

        immediateCalls = new ConcurrentLinkedQueue<>();
        deferredCalls = new ConcurrentLinkedQueue<>();
    }

    public void launch(final Singularity singularity) {

        time.set(Tryte.ZERO);

        this.singularity = singularity;

        entityEnvelopes.clear();
        final Entity seedEntity = singularity.createEntity(Entity.SEED_ENTITY_TYPE, generateId());
        entityEnvelopes.put(seedEntity.getId(), new EntityEnvelope(seedEntity));

        environments.clear();
        final Environment borderEnvironment = new Environment(Environment.BORDER_ENVIRONMENT_ID);
        environments.put(borderEnvironment.getId(), borderEnvironment);

        start();
    }

    public void launch(final Singularity singularity, final Storage storage) {

        this.singularity = singularity;
        load(storage);

        start();
    }

    public void shutDown(final Storage storage) {

        this.storage = storage;
        stop();
    }

    private void store() {

        storage.beginStoring(time.get());

        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            storage.storeEntity(entityEnvelope.entity.getTrytes());
        }

        for (final Environment environment : environments.values()) {

            storage.storeEnvironment(environment.getTrytes());
        }

        final Set<Nonet> effectIds = new HashSet<>();
        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            for (final Effect effect : entityEnvelope.effects) {

                if (effectIds.add(effect.getId())) {

                    storage.storeEffect(effect.getTrytes());
                }
            }
        }

        storage.endStoring();
    }

    private void load(final Storage storage) {

        time.set(storage.beginLoading());

        Tryte[] trytes;
        while ((trytes = storage.loadEntity()) != null) {

            final Entity entity = singularity.restoreEntity(trytes);
            entityEnvelopes.put(entity.getId(), new EntityEnvelope(entity));
        }

        while ((trytes = storage.loadEnvironment()) != null) {

            final Environment environment = Environment.getEnvironment(trytes);
            environments.put(environment.getId(), environment);

            for (final Nonet entityId : environment.getEntityIds()) {

                entityEnvelopes.get(entityId).environments.add(environment);
            }
        }

        while ((trytes = storage.loadEffect()) != null) {

            final Effect effect = Effect.getEffect(trytes);
            for (final Nonet entityId : environments.get(effect.getEnvironmentId()).getEntityIds()) {

                entityEnvelopes.get(entityId).effects.add(effect);
            }
        }
    }

    private void start() {

        isShuttingDown = false;

        processorExecutor.execute(() -> {

            while (true) {

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.entity.getStage().equals(Entity.EXISTING)
                            && entityEnvelope.environments.isEmpty()) {

                        entityEnvelope.entity.setStage(Entity.DECAYING);
                    }
                }

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.entity.getStage().equals(Entity.AWAITING)) {

                        entityEnvelope.entity.setStage(Entity.EXISTING);

                        process(entityEnvelope, new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, CREATION, entityEnvelope.entity.getId())));

                    } else if (entityEnvelope.entity.getStage().equals(Entity.DECAYING)) {

                        entityEnvelopes.remove(entityEnvelope.entity.getId());
                        for (final Environment environment : entityEnvelope.environments) {

                            environment.exclude(entityEnvelope.entity.getId());
                        }

                        process(entityEnvelope, new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, DESTRUCTION, entityEnvelope.entity.getId())));
                    }
                }

                time.set(time.get().add(Tryte.PLUS_ONE));
                final Environment borderEnvironment = environments.get(Environment.BORDER_ENVIRONMENT_ID);
                final Effect evolutionEffect = new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, EVOLUTION, time));
                for (final Nonet entityId : borderEnvironment.getEntityIds()) {

                    entityEnvelopes.get(entityId).effects.add(evolutionEffect);
                }

                for (final Runnable call : deferredCalls) {

                    call.run();
                }

                do {

                    for (final Runnable call : immediateCalls) {

                        call.run();
                    }

                    if (isShuttingDown) {

                        store();

                        return;
                    }

                    for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                        if (entityEnvelope.entity.getStage().equals(Entity.EXISTING)) {

                            final List<Effect> effects = new LinkedList<>();
                            try {

                                while (true) {

                                    final Effect effect = entityEnvelope.effects.first();
                                    if (effect.getEarliestTime().get().getValue() <= time.get().getValue()) {

                                        effects.add(effect);
                                        entityEnvelope.effects.remove(effect);
                                    }
                                }

                            } catch (final NoSuchElementException e) {
                            }

                            if (effects.size() > 0) {

                                process(entityEnvelope, effects.toArray(new Effect[effects.size()]));
                            }
                        }
                    }

                } while (!immediateCalls.isEmpty());
            }
        });
    }

    private void stop() {

        isShuttingDown = true;
    }

    private void process(final EntityEnvelope entityEnvelope, final Effect... effects) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.react(effects);
            });

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    void salvage(final Core core) {

        try {

            cores.put(core);

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    void create(final Nonet type, final Singlet[] data) {

        deferredCalls.offer(() -> {

            final Entity entity = singularity.createEntity(type, generateId());
            entityEnvelopes.put(entity.getId(), new EntityEnvelope(entity, data));
        });
    }

    void destroy(final Nonet entityId) {

        deferredCalls.offer(() -> {

            entityEnvelopes.get(entityId).entity.setStage(Entity.DECAYING);
        });
    }

    void affect(final Nonet environmentId, final Singlet[] data,
                final Singlet power, final Singlet delay, final Singlet duration) {

        (delay.get().getValue() == Tryte.ZERO_VALUE ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(generateId(), data, environmentId,
                        new Singlet(new Tryte(time.get().getValue() + delay.get().getValue())),
                        new Singlet(new Tryte(time.get().getValue() + duration.get().getValue())));
                int numberOfAffectedEntities = 0;
                for (final Nonet affectedEntityId : environment.getEntityIds()) {

                    if (power.get().getValue() > 0 && ++numberOfAffectedEntities > power.get().getValue()) {

                        break;
                    }

                    entityEnvelopes.get(affectedEntityId).effects.add(effect);
                }
            }
        });
    }

    void include(final Nonet entityId, final Nonet environmentId) {

        deferredCalls.offer(() -> {

            Environment environment;
            synchronized (environments) {

                environment = environments.get(environmentId);
                if (environment == null) {

                    environment = new Environment(environmentId);
                    environments.put(environment.getId(), environment);
                }
                environment.include(entityId);
            }

            entityEnvelopes.get(entityId).environments.add(environment);
        });
    }

    void exclude(final Nonet entityId, final Nonet environmentId) {

        deferredCalls.offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                synchronized (environments) {

                    environment.exclude(entityId);
                    if (environment.getEntityIds().isEmpty()) {

                        environments.remove(environmentId);
                    }
                }

                entityEnvelopes.get(entityId).environments.remove(environment);
            }
        });
    }

    private static Nonet generateId() {

        final Nonet id = new Nonet();
        for (int i = 0; i < id.getWidth(); i++) {

            id.set(i, new Tryte(ThreadLocalRandom.current().nextLong(Tryte.MIN_VALUE, Tryte.MAX_VALUE + 1)));
        }

        return id;
    }
}
