package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Storage {

    public void beginStoring(final Tryte time);

    public void storeEntity(final Tryte[] entityTrytes);

    public void storeEnvironment(final Tryte[] environmentTrytes);

    public void storeEffect(final Tryte[] effectTrytes);

    public void endStoring();

    public Tryte beginLoading();

    public Tryte[] loadEntity();

    public Tryte[] loadEnvironment();

    public Tryte[] loadEffect();
}
