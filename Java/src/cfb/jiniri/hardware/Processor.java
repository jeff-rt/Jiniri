package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.util.Converter;

import java.util.*;
import java.util.concurrent.*;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private static final class EntityEnvelope {

        final Entity entity;

        final Set<Tryte> environmentIds;
        final SortedSet<Effect> effects;

        EntityEnvelope(final Entity entity, final Trit[] data) {

            this.entity = entity;

            environmentIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();

            if (data != null) {

                effects.add(new Effect(data));
            }
        }
    }

    private final BlockingQueue<Core> cores;
    private final int coreMemoryCapacity;

    private Tryte time;

    private final Map<Entity, EntityEnvelope> entityEnvelopes;
    private final Map<Tryte, Environment> environments;

    private final ExecutorService processorExecutor;
    private final ExecutorService coreExecutor;

    private final Queue<Runnable> immediateCalls;
    private final Queue<Runnable> deferredCalls;

    private boolean isShuttingDown;

    public Processor(final int numberOfCores, final int coreMemoryCapacity) {

        cores = new ArrayBlockingQueue<>(numberOfCores);
        for (int i = 0; i < numberOfCores; i++) {

            cores.offer(new Core(this, coreMemoryCapacity));
        }

        this.coreMemoryCapacity = coreMemoryCapacity;

        entityEnvelopes = new HashMap<>();
        environments = new HashMap<>();

        processorExecutor = Executors.newSingleThreadExecutor();
        coreExecutor = Executors.newFixedThreadPool(numberOfCores);

        immediateCalls = new ConcurrentLinkedQueue<>();
        deferredCalls = new ConcurrentLinkedQueue<>();
    }

    public void launch(final Class seedEntityClass, final Tryte maxDataSize,
                       final Trit[] data) {

        time = Tryte.ZERO;

        entityEnvelopes.clear();
        environments.clear();

        immediateCalls.clear();
        deferredCalls.clear();

        final Entity seedEntity;
        try {

            seedEntity = (Entity)seedEntityClass.newInstance();

        } catch (final Exception e) {

            throw new RuntimeException(e);
        }
        if (maxDataSize.getIntValue() > coreMemoryCapacity) {

            throw new IllegalArgumentException("Not enough core memory capacity");
        }
        entityEnvelopes.put(seedEntity, new EntityEnvelope(seedEntity, data));

        environments.put(Environment.TEMPORAL_ENVIRONMENT, new Environment());

        start();
    }

    public void shutDown() {

        stop();
    }

    private void start() {

        isShuttingDown = false;

        processorExecutor.execute(() -> {

            while (true) {

                for (final Runnable call : deferredCalls) {

                    call.run();
                }

                if (isShuttingDown) {

                    return;
                }

                time = time.add(Tryte.PLUS_ONE);
                final Effect[] evolutionEffect = {new Effect(Converter.combine(Environment.TEMPORAL_ENVIRONMENT, time, new Tryte(System.currentTimeMillis())))};
                for (final Entity entity : environments.get(Environment.TEMPORAL_ENVIRONMENT).getEntities()) {

                    process(entityEnvelopes.get(entity), evolutionEffect);
                }

                do {

                    for (final Runnable call : immediateCalls) {

                        call.run();
                    }

                    for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                        final List<Effect> effects = new LinkedList<>();
                        try {

                            while (true) {

                                final Effect effect = entityEnvelope.effects.first();
                                if (effect.getEarliestTime().getLongValue() <= time.getLongValue()) {

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

                } while (!immediateCalls.isEmpty());

                entityEnvelopes.values().stream()
                        .filter(entityEnvelope -> entityEnvelope.environmentIds.isEmpty()
                                && entityEnvelope.effects.isEmpty())
                        .forEach(this::salvage);
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

    void create(final Tryte entityHeight,
                final Class entityClass, final Tryte maxDataSize,
                final Trit[] data) {

        if (entityHeight.getLongValue() != 0) {

            throw new IllegalArgumentException("Illegal height");
        }

        deferredCalls.offer(() -> {

            final Entity entity;
            try {

                entity = (Entity)entityClass.newInstance();

            } catch (final Exception e) {

                throw new RuntimeException(e);
            }
            if (maxDataSize.getIntValue() <= coreMemoryCapacity) {

                entityEnvelopes.put(entity, new EntityEnvelope(entity, data));
            }
        });
    }

    void destroy(final Entity entity) {

        deferredCalls.offer(() -> {

            salvage(entityEnvelopes.get(entity));
        });
    }

    void affect(final Tryte environmentId,
                final Tryte effectDirection, final Tryte effectDelay, final Tryte effectDuration,
                final Trit[] data) {

        (effectDelay.getLongValue() <= 0 ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(new Tryte(time.getLongValue() + effectDelay.getLongValue()),
                        new Tryte(time.getLongValue() + effectDuration.getLongValue()),
                        data);
                for (final Entity affectedEntity : environment.getEntities()) {

                    entityEnvelopes.get(affectedEntity).effects.add(effect);
                }
            }
        });
    }

    void include(final Entity entity, final Tryte environmentId) {

        deferredCalls.offer(() -> {

            Environment environment = environments.get(environmentId);
            if (environment == null) {

                environment = new Environment();
                environments.put(environmentId, environment);
            }
            environment.include(entity);

            entityEnvelopes.get(entity).environmentIds.add(environmentId);
        });
    }

    void exclude(final Entity entity, final Tryte environmentId) {

        deferredCalls.offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                environment.exclude(entity);
                if (environment.getEntities().isEmpty() && !environmentId.equals(Environment.TEMPORAL_ENVIRONMENT)) {

                    environments.remove(environmentId);
                }

                entityEnvelopes.get(entity).environmentIds.remove(environmentId);
            }
        });
    }

    private void salvage(final EntityEnvelope entityEnvelope) {

        entityEnvelopes.remove(entityEnvelope.entity);

        for (final Tryte environmentId : entityEnvelope.environmentIds) {

            final Environment environment = environments.get(environmentId);
            environment.exclude(entityEnvelope.entity);
            if (environment.getEntities().isEmpty()) {

                environments.remove(environmentId);
            }
        }
    }
}
