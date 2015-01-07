package cfb.jiniri.ternary;

import java.math.BigInteger;
import java.util.*;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Tryte {

    public static final Tryte MINUS_ONE = new Tryte(BigInteger.valueOf(-1));
    public static final Tryte ZERO = new Tryte(BigInteger.ZERO);
    public static final Tryte PLUS_ONE = new Tryte(BigInteger.ONE);

    private static final BigInteger RADIX = BigInteger.valueOf(Trit.RADIX);

    private static final Set<Integer> VALID_WIDTHS = new HashSet<>(Arrays.asList(new Integer[] {

            Integer.valueOf(1),
            Integer.valueOf(3),
            Integer.valueOf(9),
            Integer.valueOf(27),
            Integer.valueOf(81),
            Integer.valueOf(243)
    }));

    private final int width;
    private final BigInteger value;

    public Tryte(final long value) {

        this(BigInteger.valueOf(value));
    }

    private Tryte(final BigInteger value) {

        width = getTrits(value).length;
        this.value = value;
    }

    public Tryte(final Trit[] trits, final int offset, final int length) {

        int width = length;
        while (!VALID_WIDTHS.contains(width)) {

            width++;
        }
        this.width = width;

        value = getBigInteger(trits, offset, length);
    }

    public Tryte(final Trit[] trits) {

        this(trits, 0, trits.length);
    }

    public Tryte(final int width, final Tryte tryte) {

        if (!VALID_WIDTHS.contains(width)) {

            throw new IllegalArgumentException("Illegal width");
        }

        final Trit[] trits = new Trit[width];
        for (int i = 0; i < trits.length; i++) {

            trits[i] = Trit.UNKNOWN;
        }

        final Trit[] trits2 = tryte.getTrits();
        System.arraycopy(trits2, 0, trits, 0, trits2.length < trits.length ? trits2.length : trits.length);

        this.width = width;
        value = getBigInteger(trits, 0, trits.length);
    }

    public int getWidth() {

        return width;
    }

    public int getIntValue() {

        return value.intValueExact();
    }

    public long getLongValue() {

        return value.longValueExact();
    }

    public void getTrits(final Trit[] trits, final int offset) {

        final int capacity = trits.length - offset;
        if (capacity < getWidth()) {

            throw new IllegalArgumentException("Not enough capacity: " + capacity);
        }

        for (int i = 0; i < getWidth(); i++) {

            trits[offset + i] = Trit.UNKNOWN;
        }
        final Trit[] trits2 = getTrits(value);
        System.arraycopy(trits2, 0, trits, offset, trits2.length);
    }

    public Trit[] getTrits() {

        final Trit[] trits = new Trit[getWidth()];
        getTrits(trits, 0);

        return trits;
    }

    public Tryte sum(final Tryte tryte) {

        final Trit[] trits1 = getTrits();
        final Trit[] trits2 = tryte.getTrits();
        for (int i = 0; i < trits1.length && i < trits2.length; i++) {

            trits1[i] = trits1[i].sum(trits2[i]);
        }

        return new Tryte(trits1);
    }

    public Tryte or(final Tryte tryte) {

        final Trit[] trits1 = getTrits();
        final Trit[] trits2 = tryte.getTrits();
        for (int i = 0; i < trits1.length && i < trits2.length; i++) {

            trits1[i] = trits1[i].or(trits2[i]);
        }

        return new Tryte(trits1);
    }

    public Tryte and(final Tryte tryte) {

        final Trit[] trits1 = getTrits();
        final Trit[] trits2 = tryte.getTrits();
        for (int i = 0; i < trits1.length && i < trits2.length; i++) {

            trits1[i] = trits1[i].and(trits2[i]);
        }

        return new Tryte(trits1);
    }

    public Tryte cmp(final Tryte tryte) {

        return new Tryte(value.compareTo(tryte.value));
    }

    public Tryte add(final Tryte tryte) {

        return new Tryte(value.add(tryte.value));
    }

    public Tryte sub(final Tryte tryte) {

        return new Tryte(value.subtract(tryte.value));
    }

    public Tryte mul(final Tryte tryte) {

        return new Tryte(value.multiply(tryte.value));
    }

    public Tryte div(final Tryte tryte) {

        if (tryte.value.signum() == 0) {

            return ZERO;
        }

        return new Tryte(value.divide(tryte.value));
    }

    public Tryte mod(final Tryte tryte) {

        if (tryte.value.signum() == 0) {

            return ZERO;
        }

        return new Tryte((tryte.value.signum() > 0 ? value : value.negate())
                .mod(tryte.value.signum() > 0 ? tryte.value : tryte.value.negate()));
    }

    @Override
    public String toString() {

        return String.valueOf(value);
    }

    @Override
    public int hashCode() {

        return value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        return value.equals(obj);
    }

    private static BigInteger getBigInteger(final Trit[] trits, final int offset, final int length) {

        if (length < 1) {

            throw new IllegalArgumentException("Illegal length: " + length);
        }

        BigInteger value = BigInteger.ZERO;
        for (int i = length; i-- > 0; ) {

            value = value.multiply(RADIX).add(BigInteger.valueOf(trits[offset + i].getValue()));
        }

        return value;
    }

    private static Trit[] getTrits(final BigInteger value) {

        if (value.signum() == 0) {

            return new Trit[] {Trit.UNKNOWN};
        }

        final List<Trit> trits = new LinkedList<>();

        BigInteger value2 = value;
        if (value2.signum() < 0) {

            value2 = value2.negate();
        }

        while (value2.signum() != 0) {

            byte remainder = value2.mod(RADIX).byteValueExact();
            value2 = value2.divide(RADIX);
            if (remainder > Trit.MAX_VALUE) {

                remainder -= Trit.RADIX;
                value2 = value2.add(BigInteger.ONE);
            }
            trits.add(Trit.getTrit(remainder));
        }

        if (value.signum() < 0) {

            for (int i = 0; i < trits.size(); i++) {

                trits.set(i, trits.get(i).not());
            }
        }

        while (!VALID_WIDTHS.contains(trits.size())) {

            trits.add(Trit.UNKNOWN);
        }

        return trits.toArray(new Trit[trits.size()]);
    }
}
