package cfb.jiniri.model;

import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    public static final Nonet SEED_ENTITY_TYPE = Nonet.ZERO;

    public static final Trit AWAITING = Trit.FALSE;
    public static final Trit EXISTING = Trit.UNKNOWN;
    public static final Trit DECAYING = Trit.TRUE;

    protected final Nonet id;

    protected final Multiplet[] state;

    protected Trit stage;

    protected Entity(final Nonet id, final Multiplet[] state) {

        if (state.length < 1) {

            throw new IllegalArgumentException("No state");
        }

        this.id = (Nonet)id.clone();

        this.state = new Multiplet[state.length];
        for (int i = 0; i < this.state.length; i++) {

            this.state[i] = state[i].clone();
        }

        setStage(AWAITING);
    }

    public Nonet getId() {

        return id;
    }

    public Multiplet[] getState() {

        return state;
    }

    public Trit getStage() {

        return stage;
    }

    public void setStage(final Trit stage) {

        this.stage = stage;
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
