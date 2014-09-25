package cfb.jiniri.hardware;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.util.Converter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

/**
 * (c) 2014 Come-from-Beyond
 */
public class TCPTerminal implements Terminal {

    // TODO: Solve concurrency issues

    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 32767;

    private static final class Client {

        private static final int READING_BUFFER_SIZE = 4096;

        private static final CompletionHandler<Integer, Client> readingCompletionHandler = new CompletionHandler<Integer, Client>() {

            @Override
            public void completed(final Integer numberOfBytes, final Client client) {

                if (numberOfBytes <= 0) {

                    client.salvage();

                } else {

                    client.getReadingBuffer().flip();
                    final byte[] bytes = new byte[(client.getReadingBuffer().limit() / Converter.TRYTE_SIZE) * Converter.TRYTE_SIZE];
                    client.getReadingBuffer().get(bytes);
                    client.getReadingBuffer().compact();
                    client.receive();

                    client.pushMessage(Converter.getTrytes(bytes));
                }
            }

            @Override
            public void failed(final Throwable exc, final Client client) {

                exc.printStackTrace();

                client.salvage();
            }
        };
        private static final CompletionHandler<Integer, Client> writingCompletionHandler = new CompletionHandler<Integer, Client>() {

            @Override
            public void completed(final Integer numberOfBytes, final Client client) {

                if (client.getWritingBuffer().hasRemaining()) {

                    client.getSocketChannel().write(client.getWritingBuffer(), client, this);

                } else {

                    synchronized (client.getPendingMessages()) {

                        client.getPendingMessages().poll();

                        final byte[] message = client.getPendingMessages().peek();
                        if (message != null) {

                            client.setWritingBuffer(message);
                            client.getSocketChannel().write(client.getWritingBuffer(), client, this);
                        }
                    }
                }
            }

            @Override
            public void failed(final Throwable exc, final Client client) {

                exc.printStackTrace();

                client.salvage();
            }
        };

        private final List<Client> clients;

        private final Queue<Tryte[]> messages;

        private final AsynchronousSocketChannel socketChannel;

        private final ByteBuffer readingBuffer;
        private ByteBuffer writingBuffer;

        private final Queue<byte[]> pendingMessages;

        public Client(final List<Client> clients, final Queue<Tryte[]> messages, final AsynchronousSocketChannel socketChannel) {

            this.clients = clients;

            this.messages = messages;

            this.socketChannel = socketChannel;

            readingBuffer = ByteBuffer.allocateDirect(READING_BUFFER_SIZE);

            pendingMessages = new LinkedList<>();
        }

        public void pushMessage(final Tryte[] message) {

            if (message.length > 0) {

                messages.offer(message);
            }
        }

        public AsynchronousSocketChannel getSocketChannel() {

            return socketChannel;
        }

        public ByteBuffer getReadingBuffer() {

            return readingBuffer;
        }

        public ByteBuffer getWritingBuffer() {

            return writingBuffer;
        }

        public void setWritingBuffer(final byte[] message) {

            writingBuffer = ByteBuffer.wrap(message);
        }

        public Queue<byte[]> getPendingMessages() {

            return pendingMessages;
        }

        public void receive() {

            getSocketChannel().read(getReadingBuffer(), this, readingCompletionHandler);
        }

        public void send(final byte[] message) {

            synchronized (getPendingMessages()) {

                getPendingMessages().offer(message);

                if (getPendingMessages().size() == 1) {

                    setWritingBuffer(message);
                    getSocketChannel().write(getWritingBuffer(), this, writingCompletionHandler);
                }
            }
        }

        public void salvage() {

            clients.remove(this);

            try {

                getSocketChannel().close();

            } catch (final IOException e) {

                e.printStackTrace();
            }
        }
    }

    private final Map<Multiplet, AsynchronousServerSocketChannel> serverSocketChannels;

    private final Map<Multiplet, List<Client>> clients;

    private final Map<Multiplet, Queue<Tryte[]>> messages;

    public TCPTerminal() {

        serverSocketChannels = new HashMap<>();

        clients = new HashMap<>();

        messages = new HashMap<>();
    }

    @Override
    synchronized public void join(final Tryte[] channel) {

        final Multiplet serverSocketChannelId = new Nonet(channel);
        if (serverSocketChannels.containsKey(serverSocketChannelId)) {

            throw new RuntimeException("Already joined channel: " + serverSocketChannelId);
        }

        try {

            final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor()));
            serverSocketChannel.bind(new InetSocketAddress(serverSocketChannelId.hashCode() % (MAX_PORT - MIN_PORT + 1) + MIN_PORT));

            serverSocketChannels.put(serverSocketChannelId, serverSocketChannel);
            clients.put(serverSocketChannelId, new LinkedList<>());
            messages.put(serverSocketChannelId, new ConcurrentLinkedQueue<>());

            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(final AsynchronousSocketChannel clientSocketChannel, final Void attachment) {

                    final Client client = new Client(clients.get(serverSocketChannelId), messages.get(serverSocketChannelId), clientSocketChannel);
                    clients.get(serverSocketChannelId).add(client);
                    client.receive();

                    serverSocketChannel.accept(null, this);
                }

                @Override
                public void failed(final Throwable exc, final Void attachment) {

                    exc.printStackTrace();
                }
            });

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    synchronized public void leave(final Tryte[] channel) {

        final Multiplet serverSocketChannelId = new Nonet(channel);
        final AsynchronousServerSocketChannel serverSocketChannel = serverSocketChannels.get(serverSocketChannelId);
        if (serverSocketChannel == null) {

            throw new RuntimeException("Non-joined channel: " + serverSocketChannelId);
        }

        try {

            for (final Client client : clients.get(serverSocketChannelId)) {

                client.salvage();
            }
            serverSocketChannel.close();

            messages.remove(serverSocketChannelId);
            clients.remove(serverSocketChannelId);
            serverSocketChannels.remove(serverSocketChannelId);

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(final Tryte[] channel, final Tryte[] message) {

        final Multiplet serverSocketChannelId = new Nonet(channel);
        final List<Client> clients = this.clients.get(serverSocketChannelId);
        if (clients == null) {

            throw new RuntimeException("Non-joined channel: " + serverSocketChannelId);
        }

        final byte[] messageBytes = Converter.getBytes(message);
        for (final Client client : clients) {

            client.send(messageBytes);
        }
    }

    @Override
    public Tryte[] receive(final Tryte[] channel) {

        final Multiplet serverSocketChannelId = new Nonet(channel);
        final Queue<Tryte[]> messages = this.messages.get(serverSocketChannelId);
        if (messages == null) {

            throw new RuntimeException("Non-joined channel: " + serverSocketChannelId);
        }

        return messages.poll();
    }
}
