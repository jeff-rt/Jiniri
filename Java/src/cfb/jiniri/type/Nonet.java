package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Nonet extends Multiplet {

    public static final int WIDTH = 9;

    public static final Nonet MINUS_ONE = new Nonet(Tryte.MINUS_ONE);
    public static final Nonet ZERO = new Nonet(Tryte.ZERO);
    public static final Nonet PLUS_ONE = new Nonet(Tryte.PLUS_ONE);

    public Nonet() {

        super(WIDTH);
    }

    public Nonet(final Tryte[] trytes, final int offset, final int length) {

        this();

        if (length < 1 || length > getWidth()) {

            throw new IllegalArgumentException("Illegal length: " + length);
        }

        System.arraycopy(trytes, offset, this.trytes, 0, length);
    }

    public Nonet(final Tryte[] trytes, final int offset) {

        this(trytes, offset, WIDTH);
    }

    public Nonet(final Tryte... trytes) {

        this(trytes, 0);
    }
}
