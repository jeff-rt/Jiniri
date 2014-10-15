package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.model.Singularity;
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

        static final Trit AWAITING = Trit.FALSE;
        static final Trit EXISTING = Trit.UNKNOWN;
        static final Trit DECAYING = Trit.TRUE;

        final Tryte type;
        final Entity entity;
        Trit stage;

        final Set<Tryte> environmentIds;
        final SortedSet<Effect> effects;

        EntityEnvelope(final Tryte type, final Entity entity, final Trit[] data) {

            this.type = type;
            this.entity = entity;
            stage = AWAITING;

            environmentIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();

            if (data != null) {

                effects.add(new Effect(data));
            }
        }

        EntityEnvelope(final Tryte type, final Entity entity) {

            this(type, entity, null);
        }

        static EntityEnvelope getEntityEnvelope(final Tryte[] trytes, final Singularity singularity) {

            int i = 0;

            final Tryte type = trytes[i++];

            final Trit[] state = trytes[i++].getTrits();

            final EntityEnvelope entityEnvelope = new EntityEnvelope(type, singularity.createEntity(type, state));

            entityEnvelope.stage = trytes[i++].getTrits()[0];

            int numberOfEnvironments = trytes[i++].getIntValue();
            while (numberOfEnvironments-- > 0) {

                entityEnvelope.environmentIds.add(trytes[i++]);
            }

            int numberOfEffects = trytes[i++].getIntValue();
            while (numberOfEffects-- > 0) {

                final Trit[] data = trytes[i++].getTrits();

                final Tryte earliestTime = trytes[i++];
                final Tryte latestTime = trytes[i++];

                entityEnvelope.effects.add(new Effect(data, earliestTime, latestTime));
            }

            return entityEnvelope;
        }

        Tryte[] getTrytes() {

            final List<Tryte> trytes = new LinkedList<>();

            trytes.add(type);

            trytes.add(new Tryte(entity.getState()));

            trytes.add(new Tryte(stage.getValue()));

            trytes.add(new Tryte(environmentIds.size()));
            for (final Tryte environmentId : environmentIds) {

                trytes.add(environmentId);
            }

            trytes.add(new Tryte(effects.size()));
            for (final Effect effect : effects) {

                trytes.add(new Tryte(effect.getData()));

                trytes.add(effect.getEarliestTime());
                trytes.add(effect.getLatestTime());
            }

            return trytes.toArray(new Tryte[trytes.size()]);
        }
    }

    private final BlockingQueue<Core> cores;

    private Tryte time;

    private Singularity singularity;

    private final Map<Entity, EntityEnvelope> entityEnvelopes;
    private final Map<Tryte, Environment> environments;

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

        entityEnvelopes = new ConcurrentHashMap<>();
        environments = new ConcurrentHashMap<>();

        processorExecutor = Executors.newSingleThreadExecutor();
        coreExecutor = Executors.newFixedThreadPool(numberOfCores);

        immediateCalls = new ConcurrentLinkedQueue<>();
        deferredCalls = new ConcurrentLinkedQueue<>();
    }

    public void launch(final Singularity singularity) {

        time = Tryte.ZERO;

        this.singularity = singularity;

        entityEnvelopes.clear();
        final Entity seedEntity = singularity.createEntity(Entity.SEED_ENTITY_TYPE);
        entityEnvelopes.put(seedEntity, new EntityEnvelope(Entity.SEED_ENTITY_TYPE, seedEntity));

        environments.clear();
        final Environment borderEnvironment = new Environment();
        environments.put(Environment.BORDER_ENVIRONMENT, borderEnvironment);

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

        storage.continueStoring(time.getTrits());

        storage.continueStoring(new Tryte(entityEnvelopes.size()).getTrits());
        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            final Tryte[] trytes = entityEnvelope.getTrytes();
            storage.continueStoring((new Tryte(trytes.length)).getTrits());
            for (final Tryte tryte : trytes) {

                storage.continueStoring(tryte.getTrits());
            }
        }

        storage.endStoring();
    }

    private void load(final Storage storage) {

        storage.beginLoading();

        time = new Tryte(storage.continueLoading());

        int numberOfEntityEnvelopes = new Tryte(storage.continueLoading()).getIntValue();
        while (numberOfEntityEnvelopes-- > 0) {

            final Tryte[] trytes = new Tryte[new Tryte(storage.continueLoading()).getIntValue()];
            for (int i = 0; i < trytes.length; i++) {

                trytes[i] = new Tryte(storage.continueLoading());
            }
            final EntityEnvelope entityEnvelope = EntityEnvelope.getEntityEnvelope(trytes, singularity);
            entityEnvelopes.put(entityEnvelope.entity, entityEnvelope);
        }

        storage.endLoading();

        for (final EntityEnvelope entityEnvelope : entityEnvelopes.values()) {

            for (final Tryte environmentId : entityEnvelope.environmentIds) {

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

                    } else if (entityEnvelope.stage.equals(EntityEnvelope.DECAYING)) {

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

                time = time.add(Tryte.PLUS_ONE);
                final Environment borderEnvironment = environments.get(Environment.BORDER_ENVIRONMENT);
                final Effect evolutionEffect = new Effect(Converter.combine(Environment.BORDER_ENVIRONMENT, time, new Tryte(System.currentTimeMillis())));
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

    void create(final Tryte type, final Trit[] data) {

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

    void affect(final Tryte environmentId, final Trit[] data,
                final Tryte power, final Tryte delay, final Tryte duration) {

        (delay.getLongValue() == 0 ? immediateCalls : deferredCalls).offer(() -> {

            final Environment environment = environments.get(environmentId);
            if (environment != null) {

                final Effect effect = new Effect(data,
                        new Tryte(time.getLongValue() + delay.getLongValue()),
                        new Tryte(time.getLongValue() + duration.getLongValue()));
                long numberOfAffectedEntities = 0;
                for (final Entity affectedEntity : environment.getEntities()) {

                    if (power.getLongValue() > 0 && ++numberOfAffectedEntities > power.getLongValue()) {

                        break;
                    }

                    entityEnvelopes.get(affectedEntity).effects.add(effect);
                }
            }
        });
    }

    void include(final Entity entity, final Tryte environmentId) {

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

    void exclude(final Entity entity, final Tryte environmentId) {

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
