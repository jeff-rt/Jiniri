package cfb.jiniri.operation;

import cfb.jiniri.type.Variable;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Functions {

    public static Variable cmp(final Variable variable1, final Variable variable2,
                               final Variable variable3, final Variable variable4, final Variable variable5) {

        switch (variable1.get().cmp(variable2.get()).getIntValue()) {

            case -1: {

                return new Variable(variable3.get());
            }

            case 1: {

                return new Variable(variable5.get());
            }

            default: {

                return new Variable(variable4.get());
            }
        }
    }

    public static Variable id(final Variable variable) {

        return new Variable(variable.get().id());
    }

    public static Variable neg(final Variable variable) {

        return new Variable(variable.get().neg());
    }

    public static Variable sum(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().sum(variable2.get()));
    }

    public static Variable or(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().or(variable2.get()));
    }

    public static Variable and(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().and(variable2.get()));
    }

    public static Variable add(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().add(variable2.get()));
    }

    public static Variable mul(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().mul(variable2.get()));
    }

    public static Variable div(final Variable variable1, final Variable variable2) {

        return new Variable(variable1.get().div(variable2.get()));
    }
}
