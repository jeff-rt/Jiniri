package cfb.jiniri.model;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect {

    private final Trit[] data;
    private final Tryte delay;

    public Effect(final Trit[] data, final Tryte delay) {

        this.data = new Trit[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        this.delay = delay;
    }

    public Trit[] getData() {

        return data;
    }

    public int getDataSize() {

        return getData().length;
    }

    public Tryte getDelay() {

        return delay;
    }
}
