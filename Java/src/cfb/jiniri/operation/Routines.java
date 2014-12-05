package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Routines {

    public void idle();

    public void push(final Tryte memoryOffset,
                     final Tryte destination, final Tryte destinationOffset,
                     final Tryte numberOfTritsToPush, final Tryte offsetForNumberOfPushedTrits);

    public void pull(final Tryte memoryOffset,
                     final Tryte source, final Tryte sourceOffset,
                     final Tryte numberOfTritsToPull, final Tryte offsetForNumberOfPulledTrits);

    public void halt();

    public void spawn(final Tryte entityDomain,
                      final Class entityClass, final Tryte maxEffectDataAndScratchpadSize,
                      final Tryte effectDataPointer, final Tryte effectDataSize);

    public void decay();

    public void affect(final Tryte environment,
                       final Tryte effectRange, final Tryte effectEffectiveness,
                       final Tryte effectDelay, final Tryte effectDuration,
                       final Tryte effectDataPointer, final Tryte effectDataSize);

    public void join(final Tryte environment);

    public void leave(final Tryte environment);
}
