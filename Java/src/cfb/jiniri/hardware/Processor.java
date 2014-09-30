package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.model.Singularity;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private static final Trit AWAITING = Trit.FALSE;
    private static final Trit FUNCTIONING = Trit.UNKNOWN;
    private static final Trit DECAYING = Trit.TRUE;

    private static final class EntityEnvelope {

        final Entity entity;

        Trit stage;

        final Set<Environment> environments;
        final SortedSet<Effect> effects;

        EntityEnvelope(final Entity entity) {

            this.entity = entity;

            stage = AWAITING;

            environments = Collections.newSetFromMap(new ConcurrentHashMap<>());
            effects = new ConcurrentSkipListSet<>();
        }
    }

    private final Singlet id;

    private final Core[] cores;

    private final Singlet time;

    private Singularity singularity;

    private final Map<Nonet, EntityEnvelope> entityEnvelopes;
    private final Map<Nonet, Environment> environments;

    private final Queue<Nonet> decayingEntityIds;

    public Processor(final long id, final int numberOfCores, final int coreMemoryCapacity) {

        this.id = new Singlet(new Tryte(id));

        cores = new Core[numberOfCores];
        for (int i = 0; i < cores.length; i++) {

            cores[i] = new Core(this, coreMemoryCapacity);
        }

        time = new Singlet();

        entityEnvelopes = new ConcurrentHashMap<>();
        environments = new ConcurrentHashMap<>();

        decayingEntityIds = new ConcurrentLinkedQueue<>();
    }

    public void launch(final Singularity singularity) {

        time.set(Tryte.ZERO);

        this.singularity = singularity;

        entityEnvelopes.clear();
        final Entity universeEntity = singularity.createUniverse(generateId());
        entityEnvelopes.put(universeEntity.getId(), new EntityEnvelope(universeEntity));

        environments.clear();
        final Environment temporalEnvironment = new Environment(Environment.TEMPORAL_ENVIRONMENT_ID);
        environments.put(temporalEnvironment.getId(), temporalEnvironment);
    }

    public void launch(final Singularity singularity, final Storage storage) {

        this.singularity = singularity;

        load(storage);
    }

    public void shutDown(final Storage storage) {

        store(storage);
    }

    private void store(final Storage storage) {

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

    void create(final Nonet type, final Singlet[] data) {

        final Entity entity = singularity.createEntity(type, generateId());
        entityEnvelopes.put(entity.getId(), new EntityEnvelope(entity));
    }

    void destroy(final Nonet entityId) {

        entityEnvelopes.get(entityId).stage = DECAYING;
    }

    void affect(final Singlet[] data, final Nonet entityId, final Nonet environmentId,
                final Singlet delay, final Singlet duration) {

        final Environment environment = environments.get(environmentId);
        if (environment != null) {

            final Effect effect = new Effect(generateId(), data, entityId, environmentId, time, delay, duration);
            for (final Nonet affectedEntityId : environment.getEntityIds()) {

                entityEnvelopes.get(affectedEntityId).effects.add(effect);
            }
        }
    }

    void include(final Nonet entityId, final Nonet environmentId) {

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
    }

    void exclude(final Nonet entityId, final Nonet environmentId) {

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
    }

    private Nonet generateId() {

        final Nonet id = new Nonet();
        for (int i = 0; i < id.getWidth(); i++) {

            id.set(i, new Tryte(ThreadLocalRandom.current().nextLong(Tryte.MIN_VALUE, Tryte.MAX_VALUE + 1)));
        }

        return id;
    }
}
