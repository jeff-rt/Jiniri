package cfb.jiniri.model;

import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    protected final Nonet id;

    protected final Multiplet[] state;

    protected Entity(final Nonet id, final Multiplet[] state) {

        if (state.length < 1) {

            throw new IllegalArgumentException("No state");
        }

        this.id = (Nonet)id.clone();

        this.state = new Multiplet[state.length];
        for (int i = 0; i < this.state.length; i++) {

            this.state[i] = state[i].clone();
        }
    }

    public Nonet getId() {

        return id;
    }

    public Multiplet[] getState() {

        return state;
    }

    public int getStateSize() {

        int size = 0;
        for (final Multiplet stateElement : getState()) {

            size += stateElement.getWidth();
        }

        return size;
    }

    public abstract Tryte[] getTrytes();

    public abstract void react(final Singlet[] effectData, final Singlet[] scratchpad, final Conductor conductor);
}
