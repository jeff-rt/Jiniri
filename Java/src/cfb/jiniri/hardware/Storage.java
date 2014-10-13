package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Storage {

    public void beginStoring();

    public void continueStoring(final Tryte... trytes);

    public void endStoring();

    public void beginLoading();

    public void continueLoading(final Tryte[] bufferForTrytes);

    public void endLoading();
}
