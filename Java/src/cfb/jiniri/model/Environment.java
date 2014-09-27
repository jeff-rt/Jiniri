package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;

import java.util.HashSet;
import java.util.Set;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Environment {

    public static final Nonet TEMPORAL_ENVIRONMENT_ID = Nonet.ZERO;

    private final Nonet id;

    private final Set<Nonet> entityIds;

    public Environment(final Nonet id, final Set<Nonet> entityIds) {

        this.id = (Nonet)id.clone();

        this.entityIds = entityIds;
    }

    public Environment(final Nonet id) {

        this(id, new HashSet<>());
    }

    public static Environment getEnvironment(final Tryte[] trytes) {

        int i = 0;

        final Nonet id = new Nonet(trytes, i, Nonet.WIDTH);
        i += Nonet.WIDTH;

        final Set<Nonet> entityIds = new HashSet<>();
        int j = (int)trytes[i].getValue();
        i += 1;
        while (j-- > 0) {

            entityIds.add(new Nonet(trytes, i, Nonet.WIDTH));
            i += Nonet.WIDTH;
        }

        return new Environment(id, entityIds);
    }

    public Nonet getId() {

        return id;
    }

    synchronized public boolean include(final Nonet entityId) {

        return entityIds.add(entityId);
    }

    synchronized public boolean exclude(final Nonet entityId) {

        return entityIds.remove(entityId);
    }

    synchronized public boolean contains(final Nonet entityId) {

        return entityIds.contains(entityId);
    }

    public Tryte[] getTrytes() {

        final Tryte[] trytes = new Tryte[getId().getWidth() + 1 + entityIds.size() * Nonet.WIDTH];
        int i = 0;

        System.arraycopy(getId().getTrytes(), 0, trytes, i, getId().getWidth());
        i += getId().getWidth();

        trytes[i] = new Tryte(entityIds.size());
        i += 1;
        for (final Nonet entityId : entityIds) {

            System.arraycopy(entityId.getTrytes(), 0, trytes, i, entityId.getWidth());
            i += entityId.getWidth();
        }

        return trytes;
    }
}
