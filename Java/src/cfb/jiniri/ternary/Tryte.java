package cfb.jiniri.ternary;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Tryte {

    public static final int NUMBER_OF_TRITS = 27;

    public static final long MINUS_ONE_VALUE = -1L;
    public static final long ZERO_VALUE = 0L;
    public static final long PLUS_ONE_VALUE = 1L;

    public static final Tryte MINUS_ONE;
    public static final Tryte ZERO;
    public static final Tryte PLUS_ONE;

    public static final long MIN_VALUE;
    public static final long MAX_VALUE;

    public static final long NUMBER_OF_VALUES;

    private static final long[] COEFFICIENTS = new long[NUMBER_OF_TRITS];

    private static final long MULTIPLICATION_BASE;

    static {

        COEFFICIENTS[0] = 1L;
        for (int i = 1; i < COEFFICIENTS.length; i++) {

            COEFFICIENTS[i] = COEFFICIENTS[i - 1] * Trit.RADIX;
        }

        MAX_VALUE = (COEFFICIENTS[COEFFICIENTS.length - 1] - 1) / 2;
        MIN_VALUE = -MAX_VALUE;

        MINUS_ONE = new Tryte(MINUS_ONE_VALUE);
        ZERO = new Tryte(ZERO_VALUE);
        PLUS_ONE = new Tryte(PLUS_ONE_VALUE);

        NUMBER_OF_VALUES = MAX_VALUE - MIN_VALUE + 1;

        MULTIPLICATION_BASE = COEFFICIENTS[NUMBER_OF_TRITS / 3];
    }

    private final long value;

    public Tryte(final long value) {

        if (value < MIN_VALUE || value > MAX_VALUE) {

            throw new IllegalArgumentException("Illegal tryte value: " + value);
        }

        this.value = value;
    }

    public Tryte(final Trit[] trits, final int offset, final int length) {

        if (length < 1 || length > NUMBER_OF_TRITS) {

            throw new IllegalArgumentException("Illegal number of trits: " + length);
        }

        long value = ZERO_VALUE;
        for (int i = 0; i < length; i++) {

            value += trits[offset + i].getValue() * COEFFICIENTS[i];
        }
        this.value = value;
    }

    public Tryte(final Trit... trits) {

        this(trits, 0, trits.length);
    }

    public Tryte(final Tryte tryte) {

        this(tryte.getValue());
    }

    public long getValue() {

        return value;
    }

    public void getTrits(final Trit[] trits, final int offset) {

        final int capacity = trits.length - offset;
        if (capacity < NUMBER_OF_TRITS) {

            throw new IllegalArgumentException("Not enough capacity: " + capacity);
        }

        long value = getValue();
        if (value < ZERO_VALUE) {

            value = -value;
        }

        for (int i = 0; i < NUMBER_OF_TRITS; i++) {

            byte remainder = (byte)(value % Trit.RADIX);
            value /= Trit.RADIX;
            if (remainder > Trit.MAX_VALUE) {

                remainder -= Trit.RADIX;
                value++;
            }
            trits[offset + i] = Trit.getTrit(remainder);
        }

        if (getValue() < ZERO_VALUE) {

            for (int i = 0; i < trits.length; i++) {

                trits[offset + i] = trits[offset + i].not();
            }
        }
    }

    public Trit[] getTrits() {

        final Trit[] trits = new Trit[NUMBER_OF_TRITS];
        getTrits(trits, 0);

        return trits;
    }

    public Tryte not() {

        final Trit[] trits = getTrits();
        for (int i = 0; i < trits.length; i++) {

            trits[i] = trits[i].not();
        }

        return new Tryte(trits);
    }

    public Tryte and(final Tryte tryte) {

        final Trit[] trits1 = getTrits();
        final Trit[] trits2 = tryte.getTrits();
        for (int i = 0; i < trits1.length; i++) {

            trits1[i] = trits1[i].and(trits2[i]);
        }

        return new Tryte(trits1);
    }

    public Tryte or(final Tryte tryte) {

        final Trit[] trits1 = getTrits();
        final Trit[] trits2 = tryte.getTrits();
        for (int i = 0; i < trits1.length; i++) {

            trits1[i] = trits1[i].or(trits2[i]);
        }

        return new Tryte(trits1);
    }

    public Tryte add(final Tryte tryte) {

        final long value = getValue() + tryte.getValue();
        if (value < MIN_VALUE) {

            return new Tryte(value + NUMBER_OF_VALUES);

        } else if (value > MAX_VALUE) {

            return new Tryte(value - NUMBER_OF_VALUES);

        } else {

            return new Tryte(value);
        }
    }

    public Tryte addOverflow(final Tryte tryte) {

        final long value = getValue() + tryte.getValue();
        if (value < MIN_VALUE) {

            return MINUS_ONE;

        } else if (value > MAX_VALUE) {

            return PLUS_ONE;

        } else {

            return ZERO;
        }
    }

    public Tryte mul(final Tryte tryte) {

        /*
        The numbers are represented in [A * e^2 + B * e + C] form with e = MULTIPLICATION_BASE:

        (A1 * e^2 + B1 * e + C1) * (A2 * e^2 + B2 * e + C2) =

        = A1 * A2 * e^4 + A1 * B2 * e^3 + A1 * C2 * e^2 +
        + B1 * A2 * e^3 + B1 * B2 * e^2 + B1 * C2 * e +
        + C1 * A2 * e^2 + C1 * B2 * e   + C1 * C2 =

        = (A1 * A2) * e^4 +
        + (A1 * B2 + B1 * A2) * e^3 +
        + (A1 * C2 + B1 * B2 + C1 * A2) * e^2 +
        + (B1 * C2 + C1 * B2) * e +
        + C1 * C2
         */

        long multiplicand = getValue();
        if (multiplicand < ZERO_VALUE) {

            multiplicand = -multiplicand;
        }
        final long multiplicandC = multiplicand % MULTIPLICATION_BASE;
        multiplicand /= MULTIPLICATION_BASE;
        final long multiplicandB = multiplicand % MULTIPLICATION_BASE;
        final long multiplicandA = multiplicand / MULTIPLICATION_BASE;

        long multiplier = tryte.getValue();
        if (multiplier < ZERO_VALUE) {

            multiplier = -multiplier;
        }
        final long multiplierC = multiplier % MULTIPLICATION_BASE;
        multiplier /= MULTIPLICATION_BASE;
        final long multiplierB = multiplier % MULTIPLICATION_BASE;
        final long multiplierA = multiplier / MULTIPLICATION_BASE;

        long product0 = ((multiplicandA * multiplierC + multiplicandB * multiplierB + multiplicandC * multiplierA) * MULTIPLICATION_BASE
                + (multiplicandB * multiplierC + multiplicandC * multiplierB)) * MULTIPLICATION_BASE
                + (multiplicandC * multiplierC);

        while (product0 > MAX_VALUE) {

            product0 -= NUMBER_OF_VALUES;
        }

        if ((getValue() < ZERO_VALUE && tryte.getValue() > ZERO_VALUE)
                || (getValue() > ZERO_VALUE && tryte.getValue() < ZERO_VALUE)) {

            product0 = -product0;
        }

        return new Tryte(product0);
    }

    public Tryte mulOverflow(final Tryte tryte) {

        /*
        See the comment in mul(Tryte) method
         */

        long multiplicand = getValue();
        if (multiplicand < ZERO_VALUE) {

            multiplicand = -multiplicand;
        }
        final long multiplicandC = multiplicand % MULTIPLICATION_BASE;
        multiplicand /= MULTIPLICATION_BASE;
        final long multiplicandB = multiplicand % MULTIPLICATION_BASE;
        final long multiplicandA = multiplicand / MULTIPLICATION_BASE;

        long multiplier = tryte.getValue();
        if (multiplier < ZERO_VALUE) {

            multiplier = -multiplier;
        }
        final long multiplierC = multiplier % MULTIPLICATION_BASE;
        multiplier /= MULTIPLICATION_BASE;
        final long multiplierB = multiplier % MULTIPLICATION_BASE;
        final long multiplierA = multiplier / MULTIPLICATION_BASE;

        long product0 = ((multiplicandA * multiplierC + multiplicandB * multiplierB + multiplicandC * multiplierA) * MULTIPLICATION_BASE
                + (multiplicandB * multiplierC + multiplicandC * multiplierB)) * MULTIPLICATION_BASE
                + (multiplicandC * multiplierC);
        long product1 = (multiplicandA * multiplierA) * MULTIPLICATION_BASE
                + (multiplicandA * multiplierB + multiplicandB * multiplierA);

        while (product0 > MAX_VALUE) {

            product0 -= NUMBER_OF_VALUES;
            product1++;
        }

        if ((getValue() < ZERO_VALUE && tryte.getValue() > ZERO_VALUE)
                || (getValue() > ZERO_VALUE && tryte.getValue() < ZERO_VALUE)) {

            product1 = -product1;
        }

        return new Tryte(product1);
    }

    public Tryte div(final Tryte tryte) {

        if (tryte.getValue() == ZERO_VALUE) {

            throw new IllegalArgumentException("Division by zero");
        }

        return new Tryte(getValue() / tryte.getValue());
    }

    @Override
    public String toString() {

        return String.valueOf(getValue());
    }

    @Override
    public int hashCode() {

        return ((int)getValue()) ^ ((int)getValue() >> 32);
    }

    @Override
    public boolean equals(final Object obj) {

        return obj instanceof Tryte
                && getValue() == ((Tryte)obj).getValue();
    }
}
