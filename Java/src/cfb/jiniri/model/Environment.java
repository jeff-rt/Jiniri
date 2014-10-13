package cfb.jiniri.model;

import cfb.jiniri.type.Nonet;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Environment {

    public static final Nonet BORDER_ENVIRONMENT_ID = Nonet.ZERO;

    private final Set<Entity> entities;

    public Environment() {

        entities = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public Set<Entity> getEntities() {

        return entities;
    }

    public void include(final Entity entity) {

        getEntities().add(entity);
    }

    public void exclude(final Entity entity) {

        getEntities().remove(entity);
    }
}
