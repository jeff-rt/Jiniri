package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Routines {

    public void run(final Tryte behaviorAddress, final Tryte behaviorSize,
                    final Tryte stateAddress, final Tryte stateSize,
                    final Tryte effectAddress, final Tryte effectSize);

    public void push(final Tryte memoryAddress,
                     final Tryte destination, final Tryte destinationAddress,
                     final Tryte numberOfTrits, final Tryte addressForStatus);

    public void pull(final Tryte memoryAddress,
                     final Tryte source, final Tryte sourceAddress,
                     final Tryte numberOfTrits, final Tryte addressForStatus);

    public void halt();

    public void spawn(final Tryte entityHeight,
                      final Class entityClass, final Tryte maxDataSize,
                      final Tryte effectDataAddress, final Tryte effectDataSize);

    public void decay();

    public void affect(final Tryte environment,
                       final Tryte effectDirection, final Tryte effectDelay, final Tryte effectDuration,
                       final Tryte effectDataAddress, final Tryte effectDataSize);

    public void join(final Tryte environment);

    public void leave(final Tryte environment);
}
