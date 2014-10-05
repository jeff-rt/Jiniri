package cfb.jiniri.operation;

import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Conductor {

    public void create(final Nonet type, final Singlet pointer, final Singlet size);

    public void decay();

    public void affect(final Nonet environmentId, final Singlet delay, final Singlet duration, final Singlet power,
                       final Singlet pointer, final Singlet size);

    public void join(final Nonet environmentId);

    public void leave(final Nonet environmentId);

    public void distance(final Nonet environmentId, final Singlet distance);

    public void approach(final Nonet environmentId, final Singlet delta);

    public void retreat(final Nonet environmentId, final Singlet delta);
}
