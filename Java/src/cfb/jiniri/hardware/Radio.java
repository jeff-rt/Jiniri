package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Tryte;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * (c) 2015 Come-from-Beyond
 */
public class Radio {

    private Tryte domain;
    private DatagramSocket socket;

    public Radio(final Tryte domain, final String hostname, final int port) {

        this.domain = domain;

        try {

            socket = new DatagramSocket(new InetSocketAddress(hostname, port));

        } catch (final SocketException e) {

            throw new RuntimeException(e);
        }
    }
}
