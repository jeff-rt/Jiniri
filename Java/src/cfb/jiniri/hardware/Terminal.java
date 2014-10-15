package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Terminal {

    public void join(final Tryte channel);

    public void leave(final Tryte channel);

    public void send(final Tryte channel, final Trit[] message);

    public Trit[] receive(final Tryte channel);
}
