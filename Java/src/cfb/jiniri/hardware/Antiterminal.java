package cfb.jiniri.hardware;

/**
 * (c) 2014 Come-from-Beyond
 */
public interface Antiterminal {

    public void join(final byte[] channel);

    public void leave(final byte[] channel);

    public void send(final byte[] channel, final byte[] message);

    public byte[] receive(final byte[] channel);
}
