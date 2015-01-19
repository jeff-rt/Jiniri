package cfb.jiniri.model;

import cfb.jiniri.operation.Routines;
import cfb.jiniri.ternary.Trit;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    protected final Trit[] state;

    protected Entity(final Trit[] state) {

        this.state = new Trit[state.length];
        System.arraycopy(state, 0, this.state, 0, state.length);
    }

    public Trit[] getState() {

        return state;
    }

    public int getStateSize() {

        return state.length;
    }

    public abstract int getMaxDataSize();

    public abstract void react(final Trit[] effectData, final Trit[] scratchpad, final Routines routines);
}
