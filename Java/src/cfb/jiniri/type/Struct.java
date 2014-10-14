package cfb.jiniri.type;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Struct {

    public enum Type {

        SINGLET,
        TRIPLET,
        NONET
    }

    public static class Field {

        final Type type;
        final int length;

        public Field(final Type type, final int length) {

            this.type = type;
            this.length = length;
        }
    }

    private final Multiplet[][] fields;

    public Struct(final Field... fields) {

        this.fields = new Multiplet[fields.length][];
        for (int i = 0; i < this.fields.length; i++) {

            final int length = fields[i].length;
            if (length < 1) {

                throw new IllegalArgumentException("Illegal field length: " + length);
            }

            switch (fields[i].type) {

                case SINGLET: {

                    this.fields[i] = new Singlet[length];
                    for (int j = 0; j < this.fields[i].length; j++) {

                        this.fields[i][j] = new Singlet();
                    }

                } break;

                case TRIPLET: {

                    this.fields[i] = new Triplet[length];
                    for (int j = 0; j < this.fields[i].length; j++) {

                        this.fields[i][j] = new Triplet();
                    }

                } break;

                default: {

                    this.fields[i] = new Nonet[length];
                    for (int j = 0; j < this.fields[i].length; j++) {

                        this.fields[i][j] = new Nonet();
                    }
                }
            }
        }
    }

    public void fill(final Tryte[] trytes, final int offset) {

        int i = 0;
        for (int j = 0; j < fields.length; j++) {

            for (int k = 0; k < fields[j].length; k++) {

                fields[j][k].set(trytes, offset + i);
                i += fields[j][k].getWidth();
            }
        }
    }

    public Multiplet getField(final int fieldIndex, final int arrayIndex) {

        return fields[fieldIndex][arrayIndex];
    }

    public Multiplet getField(final int fieldIndex) {

        return getField(fieldIndex, 0);
    }
}
