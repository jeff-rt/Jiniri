package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Routines {

    public void halt();

    public void form(final Class entityClass,
                     final Tryte initializationDataAddress, final Tryte initializationDataSize);

    public void decay();

    public void affect(final Tryte environment,
                       final Tryte effectDataAddress, final Tryte effectDataSize, final Tryte delay);

    public void join(final Tryte environment);

    public void leave(final Tryte environment);

    public void broadcast(final Tryte channel, final Tryte messageAddress, final Tryte messageSize);

    public void listen(final Tryte channel);

    public void ignore(final Tryte channel);
}
