package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Singularity {

    public Entity createEntity(final Nonet type, final Nonet id);

    public Entity restoreEntity(final Tryte[] trytes);
}
