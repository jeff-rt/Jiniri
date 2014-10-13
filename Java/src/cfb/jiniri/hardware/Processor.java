package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.model.Singularity;
import cfb.jiniri.ternary.Trit;
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

        static final Trit AWAITING = Trit.FALSE;
        static final Trit EXISTING = Trit.UNKNOWN;
        static final Trit DECAYING = Trit.TRUE;

        final Nonet type;
        final Entity entity;
        Trit stage;

        final Set<Nonet> environmentIds;
        final SortedSet<Effect> effects;

        EntityEnvelope(final Nonet type, final Entity entity, final Singlet[] data) {

            this.type = type;
            this.entity = entity;
            stage = AWAITING;

            environmentIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();

            if (data != null) {

                effects.add(new Effect(data));
            }
        }

        EntityEnvelope(final Nonet type, final Entity entity) {

            this(type, entity, null);
        }

        static EntityEnvelope getEntityEnvelope(final Tryte[] trytes, final Singularity singularity) {

            int i = 0;

            final Nonet type = new Nonet(trytes, i, Nonet.WIDTH);
            i += Nonet.WIDTH;

            final Tryte[] state = new Tryte[(int)trytes[i].getValue()];
            i++;
            System.arraycopy(trytes, i, state, 0, state.length);
            i += state.length;

            final EntityEnvelope entityEnvelope = new EntityEnvelope(type, singularity.createEntity(type, state));

            entityEnvelope.stage = trytes[i].getTrits()[0];
            i++;

            int numberOfEnvironments = (int)trytes[i].getValue();
            i++;
            while (numberOfEnvironments-- > 0) {

                entityEnvelope.environmentIds.add(new Nonet(trytes, i, Nonet.WIDTH));
                i += Nonet.WIDTH;
            }

            int numberOfEffects = (int)trytes[i].getValue();
            i++;
            while (numberOfEffects-- > 0) {

                final Singlet[] data = new Singlet[(int)trytes[i].getValue()];
                i++;
                for (int j = 0; j < data.length; j++) {

                    data[j] = new Singlet(trytes[i]);
                    i++;
                }

                final Singlet earliestTime = new Singlet(trytes[i]);
                i++;

                final Singlet latestTime = new Singlet(trytes[i]);
                i++;

                entityEnvelope.effects.add(new Effect(data, earliestTime, latestTime));
            }

            return entityEnvelope;
        }

        Tryte[] getTrytes() {

            final List<Tryte> trytes = new LinkedList<>();

            for (final Tryte tryte : type.getTrytes()) {

                trytes.add(tryte);
            }

            trytes.add(new Tryte(entity.getStateSize()));
            for (final Tryte tryte : entity.getState()) {

                trytes.add(tryte);
            }

            trytes.add(new Tryte(stage));

            trytes.add(new Tryte(environmentIds.size()));
            for (final Nonet environmentId : environmentIds) {

                for (final Tryte tryte : environmentId.getTrytes()) {

                    trytes.add(tryte);
                }
            }

            trytes.add(new Tryte(effects.size()));
            for (final Effect effect : effects) {

                trytes.add(new Tryte(effect.getDataSize()));
                for (final Singlet singlet : effect.getData()) {

                    trytes.add(singlet.get());
                }

                trytes.add(effect.getEarliestTime().get());
                trytes.add(effect.getLatestTime().get());
            }

            return trytes.toArray(new Tryte[trytes.size()]);
        }
    }

    private final BlockingQueue<Core> cores;

    private final Singlet time;

    private Singularity singularity;

    private final Map<Entity, EntityEnvelope> entityEnvelopes;
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
        final Entity seedEntity = singularity.createEntity(Entity.SEED_ENTITY_TYPE);
        entityEnvelopes.put(seedEntity, new EntityEnvelope(Entity.SEED_ENTITY_TYPE, seedEntity));

        environments.clear();
        final Environment borderEnvironment = new Environment();
        environments.put(Environment.BORDER_ENVIRONMENT_ID, borderEnvironment);

        start();
    }

    public void launch(final Singularity singularity, final Storage storage) {

        this.singularity = singularity;

        entityEnvelopes.clear();
        environments.clear();

        load(storage);

        start();
    }

    public void shutDown(final Storage storage) {

        this.storage = storage;
        stop();
    }

    private void store() {

        storage.beginStoring();

        storage.continueStoring(time.get());

        storage.continueStoring(new Tryte(entityEnvelopes.size()));
        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            final Tryte[] trytes = entityEnvelope.getTrytes();
            storage.continueStoring(new Tryte(trytes.length));
            storage.continueStoring(trytes);
        }

        storage.endStoring();
    }

    private void load(final Storage storage) {

        storage.beginLoading();

        final Tryte[] bufferForTrytes = new Tryte[1];
        storage.continueLoading(bufferForTrytes);
        time.set(bufferForTrytes[0]);

        storage.continueLoading(bufferForTrytes);
        int numberOfEntityEnvelopes = (int)bufferForTrytes[0].getValue();
        while (numberOfEntityEnvelopes-- > 0) {

            storage.continueLoading(bufferForTrytes);
            final Tryte[] bufferForTrytes2 = new Tryte[(int)bufferForTrytes[0].getValue()];
            storage.continueLoading(bufferForTrytes2);
            final EntityEnvelope entityEnvelope = EntityEnvelope.getEntityEnvelope(bufferForTrytes2, singularity);
            entityEnvelopes.put(entityEnvelope.entity, entityEnvelope);
        }

        storage.endLoading();

        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            for (final Nonet environmentId : entityEnvelope.environmentIds) {

                Environment environment = environments.get(environmentId);
                if (environment == null) {

                    environment = new Environment();
                    environments.put(environmentId, environment);
                }
                environment.include(entityEnvelope.entity);
            }
        }
    }

    private void start() {

        isShuttingDown = false;

        processorExecutor.execute(() -> {

            while (true) {

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.stage.equals(EntityEnvelope.EXISTING)
                            && entityEnvelope.environmentIds.isEmpty()) {

                        entityEnvelope.stage = EntityEnvelope.DECAYING;
                    }
                }

                for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

                    if (entityEnvelope.stage.equals(EntityEnvelope.AWAITING)) {

                        entityEnvelope.stage = EntityEnvelope.EXISTING;

                        process(entityEnvelope, new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, CREATION)));

                    } else if (entityEnvelope.stage.equals(EntityEnvelope.DECAYING)) {

                        entityEnvelopes.remove(entityEnvelope.entity);
                        for (final Nonet environmentId : entityEnvelope.environmentIds) {

                            environments.get(environmentId).exclude(entityEnvelope.entity);
                        }

                        process(entityEnvelope, new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, DESTRUCTION)));
                    }
                }

                time.set(time.get().add(Tryte.PLUS_ONE));
                final Environment borderEnvironment = environments.get(Environment.BORDER_ENVIRONMENT_ID);
                final Effect evolutionEffect = new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT_ID, EVOLUTION, time));
                for (final Entity entity : borderEnvironment.getEntities()) {

                    entityEnvelopes.get(entity).effects.add(evolutionEffect);
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

                        if (entityEnvelope.stage.equals(EntityEnvelope.EXISTING)) {

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

            final Entity entity = singularity.createEntity(type);
            entityEnvelopes.put(entity, new EntityEnvelope(type, entity, data));
        });
    }

    void destroy(final Entity entity) {

        deferredCalls.offer(() -> {

            entityEnvelopes.get(entity).stage = EntityEnvelope.DECAYING;
        });
    }

    void affect(final Nonet environmentId, final Singlet[] data,
                final Singlet power, final Singlet delay, final Singlet duration) {

        (delay.get().getValue() == Tryte.ZERO_VALUE ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(data,
                        new Singlet(new Tryte(time.get().getValue() + delay.get().getValue())),
                        new Singlet(new Tryte(time.get().getValue() + duration.get().getValue())));
                int numberOfAffectedEntities = 0;
                for (final Entity affectedEntity : environment.getEntities()) {

                    if (power.get().getValue() > 0 && ++numberOfAffectedEntities > power.get().getValue()) {

                        break;
                    }

                    entityEnvelopes.get(affectedEntity).effects.add(effect);
                }
            }
        });
    }

    void include(final Entity entity, final Nonet environmentId) {

        deferredCalls.offer(() -> {

            synchronized (environments) {

                Environment environment = environments.get(environmentId);
                if (environment == null) {

                    environment = new Environment();
                    environments.put(environmentId, environment);
                }
                environment.include(entity);
            }

            entityEnvelopes.get(entity).environmentIds.add(environmentId);
        });
    }

    void exclude(final Entity entity, final Nonet environmentId) {

        deferredCalls.offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                synchronized (environments) {

                    environment.exclude(entity);
                    if (environment.getEntities().isEmpty()) {

                        environments.remove(environmentId);
                    }
                }

                entityEnvelopes.get(entity).environmentIds.remove(environmentId);
            }
        });
    }
}
