package cfb.jiniri.type;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Struct {

    public static final class VariableProperties {

        final int width;
        final int length;

        public VariableProperties(final int width, final int length) {

            this.width = width;
            this.length = length;
        }
    }

    private final Variable[][] variables;

    public Struct(final VariableProperties... properties) {

        this.variables = new Variable[properties.length][];
        for (int i = 0; i < this.variables.length; i++) {

            final int length = properties[i].length;
            if (length < 1) {

                throw new IllegalArgumentException("Illegal variable length: " + length);
            }

            this.variables[i] = new Variable[length];
            for (int j = 0; j < this.variables[i].length; j++) {

                this.variables[i][j] = new Variable(properties[i].width);
            }
        }
    }

    public void fill(final Trit[] trits, final int offset) {

        int i = 0;
        for (int j = 0; j < variables.length; j++) {

            for (int k = 0; k < variables[j].length; k++) {

                variables[j][k].set(new Tryte(trits, offset + i, variables[j][k].get().getWidth()));
                i += variables[j][k].get().getWidth();
            }
        }
    }

    public Tryte getField(final int fieldIndex, final int arrayIndex) {

        return variables[fieldIndex][arrayIndex].get();
    }

    public Tryte getField(final int fieldIndex) {

        return getField(fieldIndex, 0);
    }
}
