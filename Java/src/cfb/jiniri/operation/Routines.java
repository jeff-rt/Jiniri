package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Routines {

    public void halt();

    public void spawn(final Class entityClass,
                      final Tryte initializationDataAddress, final Tryte initializationDataSize);

    public void decay();

    public void affect(final Tryte environment,
                       final Tryte effectDelay, final Tryte effectDataAddress, final Tryte effectDataSize);

    public void join(final Tryte environment);

    public void leave(final Tryte environment);

    public void evaluate(final Class entityClass,
                         final Tryte inputDataAddress, final Tryte inputDataSize,
                         final Tryte quota);

    public void send(final Tryte channel, final Tryte messageAddress, final Tryte messageSize);

    public void receive(final Tryte channel);
}
