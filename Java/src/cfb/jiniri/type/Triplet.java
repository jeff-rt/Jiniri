package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Triplet extends Multiplet {

    private static final int WIDTH = 3;

    public static final Triplet ZERO = new Triplet(Tryte.ZERO);

    public Triplet() {

        super(WIDTH);
    }

    public Triplet(final Tryte[] trytes, final int offset, final int length) {

        this();

        if (length < 1 || length > getWidth()) {

            throw new IllegalArgumentException("Illegal length: " + length);
        }

        System.arraycopy(trytes, offset, this.trytes, 0, length);
    }

    public Triplet(final Tryte[] trytes, final int offset) {

        this(trytes, offset, WIDTH);
    }

    public Triplet(final Tryte... trytes) {

        this(trytes, 0);
    }
}
