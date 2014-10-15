package cfb.jiniri.model;

import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    public static final Tryte SEED_ENTITY_TYPE = Tryte.ZERO;

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

    public abstract void react(final Trit[] effectData, final Trit[] scratchpad, final Conductor conductor);
}
