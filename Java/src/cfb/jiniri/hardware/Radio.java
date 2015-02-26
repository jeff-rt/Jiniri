package cfb.jiniri.hardware;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * (c) 2015 Come-from-Beyond
 */
public class Radio {

    private final Processor processor;

    private int domain;
    private DatagramSocket socket;

    public Radio(final Processor processor, final int domain, final String hostname, final int port) {

        this.processor = processor;

        this.domain = domain;

        try {

            socket = new DatagramSocket(new InetSocketAddress(hostname, port));

        } catch (final SocketException e) {

            throw new RuntimeException(e);
        }
    }

    void broadcast(final byte[] channel, final byte[] message) {

        // TODO: Implement!
    }
}
