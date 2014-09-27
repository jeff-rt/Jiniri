package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.model.Environment;
import cfb.jiniri.model.Singularity;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private final Multiplet id;

    private final Core[] cores;

    private final Singlet time;

    private final Map<Multiplet, Entity> entities;
    private final Map<Multiplet, Environment> environments;
    private final Map<Multiplet, Effect> effects;

    public Processor(final Multiplet id, final int numberOfCores, final int coreMemoryCapacity, final Singlet time) {

        this.id = id.clone();

        cores = new Core[numberOfCores];
        for (int i = 0; i < cores.length; i++) {

            cores[i] = new Core(coreMemoryCapacity);
        }

        this.time = (Singlet)time.clone();

        entities = new ConcurrentHashMap<>();
        environments = new ConcurrentHashMap<>();
        effects = new ConcurrentHashMap<>();
    }

    public void launch(final Singularity singularity) {

        time.set(Tryte.ZERO);

        entities.clear();
        final Entity universeEntity = singularity.createUniverse();
        entities.put(universeEntity.getId(), universeEntity);

        environments.clear();
        final Environment temporalEnvironment = new Environment(Environment.TEMPORAL_ENVIRONMENT_ID);
        environments.put(temporalEnvironment.getId(), temporalEnvironment);
        temporalEnvironment.include(universeEntity.getId());

        effects.clear();
    }

    public void launch(final Singularity singularity, final Storage storage) {

        load(singularity, storage);
    }

    public void shutDown(final Storage storage) {

        store(storage);
    }

    private void store(final Storage storage) {

        storage.beginStoring(time.get());

        for (final Entity entity : entities.values()) {

            storage.storeEntity(entity.getTrytes());
        }

        for (final Environment environment : environments.values()) {

            storage.storeEnvironment(environment.getTrytes());
        }

        for (final Effect effect : effects.values()) {

            storage.storeEffect(effect.getTrytes());
        }

        storage.endStoring();
    }

    private void load(final Singularity singularity, final Storage storage) {

        time.set(storage.beginLoading());

        Tryte[] trytes;
        while ((trytes = storage.loadEntity()) != null) {

            final Entity entity = singularity.restoreEntity(trytes);
            entities.put(entity.getId(), entity);
        }

        while ((trytes = storage.loadEnvironment()) != null) {

            final Environment environment = Environment.getEnvironment(trytes);
            environments.put(environment.getId(), environment);
        }

        while ((trytes = storage.loadEffect()) != null) {

            final Effect effect = Effect.getEffect(trytes);
            effects.put(effect.getId(), effect);
        }
    }
}
