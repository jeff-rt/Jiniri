package cfb.jiniri.model;

import cfb.jiniri.operation.Routines;
import cfb.jiniri.ternary.Trit;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    private final Trit[] state;

    public Entity(final Trit[] state) {

        this.state = new Trit[state.length];
        System.arraycopy(state, 0, this.state, 0, getStateSize());
    }

    public void getState(final Trit[] cache) {

        System.arraycopy(state, 0, cache, 0, getStateSize());
    }

    public void setState(final Trit[] cache) {

        System.arraycopy(cache, 0, state, 0, getStateSize());
    }

    public int getStateSize() {

        return state.length;
    }

    public abstract int getMaxDataSize();

    public abstract boolean morphs();

    public abstract boolean reacts();

    public abstract boolean analyzes();

    public abstract void form(final Trit[] cache, final Routines routines);

    public abstract void morph(final Trit[] cache, final Routines routines);

    public abstract void react(final Trit[] cache, final Routines routines);

    public abstract void analyze(final Trit[] cache, final Routines routines);

    public abstract void decay(final Trit[] cache, final Routines routines);
}
