package cfb.jiniri.model;

import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    public static final Nonet SEED_ENTITY_TYPE = Nonet.ZERO;

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

    public abstract void react(final Singlet[] effectData, final Singlet[] scratchpad, final Conductor conductor);
}
