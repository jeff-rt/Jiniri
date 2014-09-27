package cfb.jiniri.model;

import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Nonet;

import java.util.HashSet;
import java.util.Set;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Environment {

    public static final Multiplet TEMPORAL_ENVIRONMENT = Nonet.ZERO;

    private final Multiplet id;

    private final Set<Multiplet> entityIds;

    public Environment(final Multiplet id) {

        this.id = id.clone();

        entityIds = new HashSet<>();
    }

    public Multiplet getId() {

        return id;
    }

    synchronized public boolean include(final Multiplet entityId) {

        return entityIds.add(entityId);
    }

    synchronized public boolean exclude(final Multiplet entityId) {

        return entityIds.remove(entityId);
    }

    synchronized public boolean contains(final Multiplet entityId) {

        return entityIds.contains(entityId);
    }
}
