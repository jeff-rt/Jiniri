package cfb.jiniri.model;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Singularity {

    public Entity createEntity(final Tryte type);

    public Entity createEntity(final Tryte type, final Trit[] state);
}
