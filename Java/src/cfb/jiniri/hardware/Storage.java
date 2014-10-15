package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Trit;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Storage {

    public void beginStoring();

    public void continueStoring(final Trit[] trits);

    public void endStoring();

    public void beginLoading();

    public Trit[] continueLoading();

    public void endLoading();
}
