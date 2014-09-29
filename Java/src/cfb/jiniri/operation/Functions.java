package cfb.jiniri.operation;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.type.Multiplet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Functions {

    public static final Trit SINGLET_TRIT = Trit.UNKNOWN;
    public static final Trit TRIPLET_TRIT = Trit.TRUE;
    public static final Trit NONET_TRIT = Trit.FALSE;

    public static final Trit[] LIT_TRITS = {Trit.UNKNOWN, Trit.UNKNOWN, Trit.UNKNOWN};
    public static final Trit[] CMP_TRITS = {Trit.TRUE, Trit.UNKNOWN, Trit.UNKNOWN};
    public static final Trit[] ADD_TRITS = {Trit.FALSE, Trit.UNKNOWN, Trit.UNKNOWN};
    public static final Trit[] MUL_TRITS = {Trit.UNKNOWN, Trit.TRUE, Trit.UNKNOWN};
    public static final Trit[] DIV_TRITS = {Trit.TRUE, Trit.TRUE, Trit.UNKNOWN};
    public static final Trit[] MOD_TRITS = {Trit.FALSE, Trit.TRUE, Trit.UNKNOWN};
    public static final Trit[] NOT_TRITS = {Trit.UNKNOWN, Trit.FALSE, Trit.UNKNOWN};
    public static final Trit[] AND_TRITS = {Trit.TRUE, Trit.FALSE, Trit.UNKNOWN};
    public static final Trit[] OR_TRITS = {Trit.FALSE, Trit.FALSE, Trit.UNKNOWN};

    public static Multiplet lit(final Multiplet multiplet) {

        return multiplet.clone();
    }

    public static Multiplet cmp(final Multiplet multiplet1, final Multiplet multiplet2,
                                final Multiplet multiplet3, final Multiplet multiplet4, final Multiplet multiplet5) {

        if (multiplet3.getWidth() != multiplet4.getWidth() || multiplet3.getWidth() != multiplet5.getWidth()) {

            throw new RuntimeException("Non-matching widths of operand 3, operand 4 and operand 5");
        }

        switch (multiplet1.cmp(multiplet2)) {

            case Multiplet.LESS: {

                return multiplet3.clone();
            }

            case Multiplet.GREATER: {

                return multiplet5.clone();
            }

            default: {

                return multiplet4.clone();
            }
        }
    }

    public static Multiplet add(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().add(multiplet2);
    }

    public static Multiplet mul(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().mul(multiplet2);
    }

    public static Multiplet div(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().div(multiplet2);
    }

    public static Multiplet mod(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().mod(multiplet2);
    }

    public static Multiplet not(final Multiplet multiplet) {

        return multiplet.clone().not();
    }

    public static Multiplet and(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().and(multiplet2);
    }

    public static Multiplet or(final Multiplet multiplet1, final Multiplet multiplet2) {

        return multiplet1.clone().or(multiplet2);
    }
}
