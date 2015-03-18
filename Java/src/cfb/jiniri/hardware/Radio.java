package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.util.Converter;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * (c) 2015 Come-from-Beyond
 */
public class Radio {

    private static final int PROPAGATION_QUOTIENT = 10;
    private static final int TTL = 10;
    private static final int PEERS_UPDATE_PERIOD = 60000;

    private static final byte PEERS_RESPONSE = -1;
    private static final byte MESSAGE_REQUEST = 0;
    private static final byte PEERS_REQUEST = 1;

    private final Processor processor;

    private final int domain;
    private final DatagramSocket socket;
    private final List<String> peerAddresses;

    private final ExecutorService serverExecutor;

    public Radio(final Processor processor, final int domain, final String ownAddress, final String[] peerAddresses) {

        this.processor = processor;

        this.domain = domain;
        try {

            socket = new DatagramSocket(new InetSocketAddress(Converter.getHostName(ownAddress), Converter.getPort(ownAddress)));
            socket.setSoTimeout(5000);

        } catch (final SocketException e) {

            throw new RuntimeException(e);
        }
        this.peerAddresses = new CopyOnWriteArrayList<>(Arrays.asList(peerAddresses));

        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.execute(() -> {

            long lastPeersUpdateTime = 0;

            final byte[] buffer = new byte[65536];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {

                try {

                    socket.receive(packet);

                    if (packet.getLength() >= 4 + 1) {

                        final byte[] requestBytes = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), packet.getOffset(), requestBytes, 0, requestBytes.length);
                        final ByteBuffer request = ByteBuffer.wrap(requestBytes);
                        request.order(ByteOrder.LITTLE_ENDIAN);

                        if (request.getInt() == domain) {

                            switch (request.get()) {

                                case PEERS_RESPONSE: {

                                    int i = request.get();
                                    while (i-- > 0) {

                                        final byte[] address = new byte[request.get()];
                                        request.get(address);
                                        this.peerAddresses.add(new String(address, "UTF-8"));
                                    }

                                } break;

                                case MESSAGE_REQUEST: {

                                    final int ttl = request.get();

                                    final byte[] channel = new byte[request.get()];
                                    request.get(channel);
                                    final byte[] message = new byte[request.getShort()];
                                    request.get(message);
                                    processor.dispatch(new Tryte(Converter.getTrits(channel)), Converter.getTrits(message));

                                    if (ttl > 0) {

                                        request.array()[4 + 1] = (byte)(ttl - 1);
                                        send(request.array());
                                    }

                                } break;

                                case PEERS_REQUEST: {

                                    List<String> nonSharedPeerAddresses = new LinkedList<>(Arrays.asList(peerAddresses));
                                    List<byte[]> sharedPeerAddresses = new LinkedList<>();
                                    for (int i = 0; i < 100 && !nonSharedPeerAddresses.isEmpty(); i++) {

                                        final int index = ThreadLocalRandom.current().nextInt(nonSharedPeerAddresses.size());
                                        sharedPeerAddresses.add(nonSharedPeerAddresses.get(index).getBytes("UTF-8"));
                                        nonSharedPeerAddresses.remove(index);
                                    }

                                    int totalSize = 0;
                                    for (final byte[] sharedPeerAddress : sharedPeerAddresses) {

                                        totalSize += 1 + sharedPeerAddress.length;
                                    }

                                    final ByteBuffer response = ByteBuffer.allocate(4 + 1 + 1 + totalSize);
                                    response.order(ByteOrder.LITTLE_ENDIAN);
                                    response.putInt(domain);
                                    response.put(PEERS_RESPONSE);
                                    response.put((byte)sharedPeerAddresses.size());
                                    for (final byte[] sharedPeerAddress : sharedPeerAddresses) {

                                        response.put((byte)sharedPeerAddress.length);
                                        response.put(sharedPeerAddress);
                                    }
                                    socket.send(new DatagramPacket(response.array(), response.position(), packet.getSocketAddress()));

                                } break;
                            }
                        }
                    }

                } catch (final SocketTimeoutException e) {

                    final long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPeersUpdateTime >= PEERS_UPDATE_PERIOD) {

                        final ByteBuffer request = ByteBuffer.allocate(4 + 1);
                        request.order(ByteOrder.LITTLE_ENDIAN);
                        request.putInt(domain);
                        request.put(PEERS_REQUEST);
                        send(request.array());

                        lastPeersUpdateTime = currentTime;
                    }

                } catch (final Exception e) {
                }
            }
        });
    }

    void broadcast(final byte[] channel, final byte[] message) {

        final ByteBuffer request = ByteBuffer.allocate(4 + 1 + 1 + 1 + channel.length + 2 + message.length);
        request.order(ByteOrder.LITTLE_ENDIAN);
        request.putInt(domain);
        request.put(MESSAGE_REQUEST);
        request.put((byte)TTL);
        request.put((byte)channel.length);
        request.put(channel);
        request.putShort((short)message.length);
        request.put(message);
        send(request.array());
    }

    private void send(final byte[] response) {

        for (int i = 0; i < PROPAGATION_QUOTIENT; i++) {

            try {

                final String peerAddress = peerAddresses.get(ThreadLocalRandom.current().nextInt(peerAddresses.size()));
                socket.send(new DatagramPacket(response, response.length, new InetSocketAddress(Converter.getHostName(peerAddress), Converter.getPort(peerAddress))));

            } catch (final IOException e) {
            }
        }
    }
}
