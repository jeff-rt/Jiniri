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

    public Singlet(final Tryte[] trytes, final int offset, final int length) {

        super(WIDTH);

        if (length < 1 || length > getWidth()) {

            throw new IllegalArgumentException("Illegal length: " + length);
        }

        System.arraycopy(trytes, offset, this.trytes, 0, length);
    }

    public Singlet(final Tryte[] trytes, final int offset) {

        this(trytes, offset, WIDTH);
    }

    public Singlet(final Tryte... trytes) {

        this(trytes, 0);
    }
}
