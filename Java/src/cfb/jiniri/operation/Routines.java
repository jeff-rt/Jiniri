package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Routines {

    public void halt();

    public void spawn(final Tryte domain,
                      final Class entityClass, final Tryte maxDataSize,
                      final Tryte effectDataAddress, final Tryte effectDataSize);

    public void decay();

    public void affect(final Tryte domain, final Tryte environment,
                       final Tryte effectDelay, final Tryte effectDuration,
                       final Tryte effectDataAddress, final Tryte effectDataSize);

    public void join(final Tryte environment);

    public void leave(final Tryte environment);
}
