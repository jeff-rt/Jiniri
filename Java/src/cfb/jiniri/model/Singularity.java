package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Singularity {

    public Entity createUniverse();

    public Entity createEntity(final Multiplet type);

    public Entity restoreEntity(final Tryte[] trytes);
}
