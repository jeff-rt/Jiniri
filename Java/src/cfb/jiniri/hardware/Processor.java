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
        Trit[] initializationData;
        boolean isDecaying;

        final Set<Tryte> environmentIds;
        final Queue<Effect> effects;

        final Set<Tryte> channelIds;
        final Queue<Trit[]> messages;

        EntityEnvelope(final Entity entity, final Trit[] initializationData) {

            this.entity = entity;
            this.initializationData = initializationData;

            environmentIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentLinkedQueue<>();

            channelIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            messages = new ConcurrentLinkedQueue<>();
        }
    }

    private final int numberOfCores;
    private final BlockingQueue<Core> cores;
    private final int coreCacheCapacity;
    private final Radio radio;

    private final Map<Entity, EntityEnvelope> entityEnvelopes;
    private final Map<Tryte, Environment> environments;
    private final Map<Tryte, Set<Entity>> channels;

    private final ExecutorService processorExecutor;
    private final ExecutorService coreExecutor;

    private final Queue<Runnable> immediateCalls;
    private final Queue<Runnable> deferredCalls;

    private boolean isShuttingDown;

    public Processor(final int numberOfCores, final int coreCacheCapacity,
                     final int domain, final String ownAddress, final String[] peerAddresses) {

        this.numberOfCores = numberOfCores;

        cores = new ArrayBlockingQueue<>(numberOfCores);
        for (int i = 0; i < numberOfCores; i++) {

            cores.offer(new Core(this, coreCacheCapacity));
        }

        this.coreCacheCapacity = coreCacheCapacity;

        radio = new Radio(this, domain, ownAddress, peerAddresses);

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
        if (maxDataSize.getIntValue() > coreCacheCapacity) {

            throw new IllegalArgumentException("Not enough core cache capacity");
        }
        entityEnvelopes.put(seedEntity, new EntityEnvelope(seedEntity, data));

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

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.isDecaying ||
                            (entityEnvelope.environmentIds.isEmpty() && entityEnvelope.effects.isEmpty())) {

                        process(entityEnvelope);
                    }
                }

                if (isShuttingDown) {

                    return;
                }

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.initializationData != null) {

                        process(entityEnvelope, entityEnvelope.initializationData);

                        entityEnvelope.initializationData = null;
                    }
                }

                final Tryte time = new Tryte(System.currentTimeMillis());
                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.entity.morphs()) {

                        process(entityEnvelope, time);
                    }
                }

                do {

                    for (final Runnable call : immediateCalls) {

                        call.run();
                    }

                    for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                        if (!entityEnvelope.effects.isEmpty()) {

                            process(entityEnvelope, entityEnvelope.effects.toArray(new Effect[entityEnvelope.effects.size()]));
                        }

                        if (!entityEnvelope.messages.isEmpty()) {

                            process(entityEnvelope, entityEnvelope.messages.toArray(new Trit[entityEnvelope.messages.size()][]));
                        }
                    }

                } while (!immediateCalls.isEmpty() || cores.size() < numberOfCores);
            }
        });
    }

    private void stop() {

        isShuttingDown = true;
    }

    private void process(final EntityEnvelope entityEnvelope, final Trit[] initializationData) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.executeForm(initializationData);
            });

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    private void process(final EntityEnvelope entityEnvelope, final Tryte time) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.executeMorph(time);
            });

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    private void process(final EntityEnvelope entityEnvelope, final Effect[] effects) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.executeReact(effects);
            });

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    private void process(final EntityEnvelope entityEnvelope, final Trit[][] messages) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.executeAnalyze(messages);
            });

        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
    }

    private void process(final EntityEnvelope entityEnvelope) {

        try {

            final Core core = cores.take();
            coreExecutor.execute(() -> {

                core.deploy(entityEnvelope.entity);
                core.executeDecay();

                salvage(entityEnvelope);
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
                final Trit[] initializationData) {

        deferredCalls.offer(() -> {

            final Entity entity;
            try {

                entity = (Entity)entityClass.newInstance();

            } catch (final Exception e) {

                throw new RuntimeException(e);
            }
            if (entity.getMaxDataSize() <= coreCacheCapacity) {

                entityEnvelopes.put(entity, new EntityEnvelope(entity, initializationData));
            }
        });
    }

    void destroy(final Entity entity) {

        deferredCalls.offer(() -> {

            entityEnvelopes.get(entity).isDecaying = true;
        });
    }

    void affect(final Tryte environmentId,
                final Trit[] data, final Tryte delay) {

        if (delay.getIntValue() < 0) {

            throw new UnsupportedOperationException("Delays below zero not supported");
        }

        (delay.getIntValue() == 0 ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(data, delay);
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
                if (environment.getEntities().isEmpty()) {

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

    void dispatch(final Tryte channelId, final Trit[] message) {

        deferredCalls.offer(() -> {

            final Set<Entity> channelEntities = channels.get(channelId);
            if (channelEntities != null) {

                for (final Entity affectedEntity : channelEntities) {

                    entityEnvelopes.get(affectedEntity).messages.add(message);
                }
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

        for (final Tryte channelId : entityEnvelope.channelIds) {

            final Set<Entity> channel = channels.get(channelId);
            channel.remove(entityEnvelope.entity);
            if (channel.isEmpty()) {

                channels.remove(channelId);
            }
        }
    }
}
