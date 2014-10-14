package cfb.jiniri.operation;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Conductor {

    public void create(final Tryte type, final Tryte pointer, final Tryte size);

    public void decay();

    public void get(final Tryte type);

    public void add(final Tryte pointer, final Tryte size, final Tryte environmentId);

    public void remove(final Tryte type);

    public void affect(final Tryte environmentId, final Tryte pointer, final Tryte size,
                       final Tryte power, final Tryte delay, final Tryte duration);

    public void join(final Tryte environmentId);

    public void leave(final Tryte environmentId);
}
