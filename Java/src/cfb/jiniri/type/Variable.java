package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Variable {

    private Tryte tryte;

    public Variable() {

        this(Tryte.ZERO);
    }

    public Variable(final Tryte tryte) {

        set(tryte);
    }

    public Tryte get() {

        return tryte;
    }

    public void set(final Tryte tryte) {

        this.tryte = tryte;
    }
}
