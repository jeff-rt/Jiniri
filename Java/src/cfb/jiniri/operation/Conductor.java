package cfb.jiniri.operation;

import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Conductor {

    public void create(final Nonet type, final Singlet pointer, final Singlet size);

    public void decay();

    public void get(final Nonet type);

    public void add(final Singlet pointer, final Singlet size, final Nonet environmentId);

    public void remove(final Nonet type);

    public void affect(final Nonet environmentId, final Singlet pointer, final Singlet size,
                       final Singlet power, final Singlet delay, final Singlet duration);

    public void join(final Nonet environmentId);

    public void leave(final Nonet environmentId);
}
