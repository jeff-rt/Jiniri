package cfb.jiniri.model;

import cfb.jiniri.type.Multiplet;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Singularity {

    public Entity createUniverse();

    public Multiplet[] getEntityTypes();

    public Entity createEntity(final Multiplet type);
}
