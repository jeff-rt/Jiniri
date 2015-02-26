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

        final Set<Tryte> channelIds;

        EntityEnvelope(final Entity entity, final Trit[] data) {

            this.entity = entity;

            environmentIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();

            channelIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

            if (data != null) {

                effects.add(new Effect(data));
            }
        }
    }

    private final BlockingQueue<Core> cores;
    private final int coreMemoryCapacity;
    private final Radio radio;

    private Tryte time;

    private final Map<Entity, EntityEnvelope> entityEnvelopes;
    private final Map<Tryte, Environment> environments;
    private final Map<Tryte, Set<Entity>> channels;

    private final ExecutorService processorExecutor;
    private final ExecutorService coreExecutor;

    private final Queue<Runnable> immediateCalls;
    private final Queue<Runnable> deferredCalls;

    private boolean isShuttingDown;

    public Processor(final int numberOfCores, final int coreMemoryCapacity,
                     final int domain, final String hostname, final int port) {

        cores = new ArrayBlockingQueue<>(numberOfCores);
        for (int i = 0; i < numberOfCores; i++) {

            cores.offer(new Core(this, coreMemoryCapacity));
        }

        this.coreMemoryCapacity = coreMemoryCapacity;

        radio = new Radio(this, domain, hostname, port);

        entityEnvelopes = new HashMap<>();
        environments = new HashMap<>();
        channels = new HashMap<>();

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
                                if (effect.getTime().getLongValue() <= time.getLongValue()) {

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

    void create(final Class entityClass,
                final Trit[] initializationData,
                final Tryte priority) {

        deferredCalls.offer(() -> {

            final Entity entity;
            try {

                entity = (Entity)entityClass.newInstance();

            } catch (final Exception e) {

                throw new RuntimeException(e);
            }
            if (entity.getMaxDataSize() <= coreMemoryCapacity) {

                entityEnvelopes.put(entity, new EntityEnvelope(entity, initializationData));
            }
        });
    }

    void destroy(final Entity entity) {

        deferredCalls.offer(() -> {

            salvage(entityEnvelopes.get(entity));
        });
    }

    void affect(final Tryte environmentId,
                final Tryte effectDelay, final Trit[] data) {

        (effectDelay.getLongValue() <= 0 ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(new Tryte(time.getLongValue() + effectDelay.getLongValue()), data);
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

    void broadcast(final Tryte channel, final Trit[] message) {

        radio.broadcast(Converter.getBytes(channel.getTrits()), Converter.getBytes(message));
    }

    void listen(final Entity entity, final Tryte channelId) {

        Set<Entity> channelEntities = channels.get(channelId);
        if (channelEntities == null) {

            channelEntities = new HashSet<>();
            channels.put(channelId, channelEntities);
        }
        channelEntities.add(entity);

        entityEnvelopes.get(entity).channelIds.add(channelId);
    }

    void ignore(final Entity entity, final Tryte channelId) {

        final Set<Entity> channelEntities = channels.get(channelId);
        if (channelEntities != null) {

            channelEntities.remove(entity);
        }

        entityEnvelopes.get(entity).channelIds.remove(channelId);
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
