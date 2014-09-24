package cfb.jiniri.model;

import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public abstract class Entity {

    protected final Multiplet id;

    protected final Multiplet[] state;

    protected Entity(final Multiplet id, final Multiplet[] state) {

        this.id = id.clone();

        this.state = new Multiplet[state.length];
        for (int i = 0; i < this.state.length; i++) {

            this.state[i] = state[i].clone();
        }
    }

    public Multiplet getId() {

        return id;
    }

    public Multiplet[] getState() {

        return state;
    }

    public abstract void react(final Singlet[] effectData, final int scratchpadSize);

    public abstract Singlet[] getEffectData();

    public abstract Singlet[] getScratchpad();
}
