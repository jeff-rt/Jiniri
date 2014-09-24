package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Singlet extends Multiplet {

    private static final int WIDTH = 1;

    public Singlet() {

        super(WIDTH);
    }

    public Tryte get() {

        return trytes[0];
    }

    public void set(final Tryte tryte) {

        trytes[0] = tryte;
    }
}
