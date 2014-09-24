package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Nonet extends Multiplet {

    private static final int WIDTH = 9;

    public Nonet() {

        super(WIDTH);
    }

    public Tryte get(final int index) {

        return trytes[index];
    }

    public void set(final int index, final Tryte tryte) {

        trytes[index] = tryte;
    }
}
