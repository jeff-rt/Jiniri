package cfb.jiniri.ternary;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Trit {

    public static final int RADIX = 3;

    public static final byte MINUS_ONE_VALUE = -1;
    public static final byte ZERO_VALUE = 0;
    public static final byte PLUS_ONE_VALUE = 1;

    public static final Trit MINUS_ONE = new Trit(MINUS_ONE_VALUE);
    public static final Trit ZERO = new Trit(ZERO_VALUE);
    public static final Trit PLUS_ONE = new Trit(PLUS_ONE_VALUE);

    public static final byte MIN_VALUE = MINUS_ONE_VALUE;
    public static final byte MAX_VALUE = PLUS_ONE_VALUE;

    public static final int NUMBER_OF_VALUES = MAX_VALUE - MIN_VALUE + 1;

    public static final byte FALSE_VALUE = MINUS_ONE_VALUE;
    public static final byte UNKNOWN_VALUE = ZERO_VALUE;
    public static final byte TRUE_VALUE = PLUS_ONE_VALUE;

    public static final Trit FALSE = MINUS_ONE;
    public static final Trit UNKNOWN = ZERO;
    public static final Trit TRUE = PLUS_ONE;

    private final byte value;

    private Trit(final byte value) {

        this.value = value;
    }

    public static Trit getTrit(final byte value) {

        switch (value) {

            case MINUS_ONE_VALUE: {

                return FALSE;
            }

            case ZERO_VALUE: {

                return UNKNOWN;
            }

            case PLUS_ONE_VALUE: {

                return TRUE;
            }

            default: {

                throw new IllegalArgumentException("Illegal trit value: " + value);
            }
        }
    }

    public byte getValue() {

        return value;
    }

    public Trit not() {

        return getValue() == UNKNOWN_VALUE ? UNKNOWN
                : getValue() == TRUE_VALUE ? FALSE : TRUE;
    }

    public Trit sum(final Trit trit) {

        switch (getValue()) {

            case FALSE_VALUE: {

                switch (trit.getValue()) {

                    case FALSE_VALUE: {

                        return TRUE;
                    }

                    case TRUE_VALUE: {

                        return UNKNOWN;
                    }

                    default: {

                        return FALSE;
                    }
                }
            }

            case TRUE_VALUE: {

                switch (trit.getValue()) {

                    case FALSE_VALUE: {

                        return UNKNOWN;
                    }

                    case TRUE_VALUE: {

                        return FALSE;
                    }

                    default: {

                        return TRUE;
                    }
                }
            }

            default: {

                return trit;
            }
        }
    }

    public Trit or(final Trit trit) {

        return getValue() < trit.getValue() ? trit : this;
    }

    public Trit and(final Trit trit) {

        return getValue() > trit.getValue() ? trit : this;
    }

    @Override
    public String toString() {

        return String.valueOf(getValue());
    }

    @Override
    public int hashCode() {

        return getValue();
    }

    @Override
    public boolean equals(final Object obj) {

        return obj instanceof Trit
                && getValue() == ((Trit)obj).getValue();
    }
}
