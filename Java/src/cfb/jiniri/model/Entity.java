package cfb.jiniri.model;

import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    public static final Tryte SEED_ENTITY_TYPE = Tryte.ZERO;

    protected final Tryte[] state;

    protected Entity(final Tryte[] state) {

        this.state = new Tryte[state.length];
        System.arraycopy(state, 0, this.state, 0, state.length);
    }

    public Tryte[] getState() {

        return state;
    }

    public int getStateSize() {

        return state.length;
    }

    public abstract void react(final Tryte[] effectData, final Tryte[] scratchpad, final Conductor conductor);
}
