package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Antiterminal;
import cfb.jiniri.hardware.Terminal;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.util.Converter;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * (c) 2014 Come-from-Beyond
 */
public class DirectTerminal implements Terminal, Antiterminal {

    private final Set<Nonet> listenedChannelIds;

    private final Map<Nonet, Queue<Tryte[]>> outgoingMessages;
    private final Map<Nonet, Queue<Tryte[]>> incomingMessages;

    public DirectTerminal() {

        listenedChannelIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        outgoingMessages = new ConcurrentHashMap<>();
        incomingMessages = new ConcurrentHashMap<>();
    }

    @Override
    public void join(final Tryte[] channel) {

        final Nonet channelId = new Nonet(channel);
        if (outgoingMessages.putIfAbsent(channelId, new ConcurrentLinkedQueue<>()) != null) {

            throw new RuntimeException("Already joined channel: " + channelId);
        }
        incomingMessages.put(channelId, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void leave(final Tryte[] channel) {

        final Nonet channelId = new Nonet(channel);
        if (outgoingMessages.remove(channelId) == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }
        incomingMessages.remove(channelId);

        listenedChannelIds.remove(channelId);
    }

    @Override
    public void send(final Tryte[] channel, final Tryte[] message) {

        final Nonet channelId = new Nonet(channel);
        final Queue<Tryte[]> outgoingMessages = this.outgoingMessages.get(channelId);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }

        if (listenedChannelIds.contains(channelId)) {

            outgoingMessages.offer(message);
        }
    }

    @Override
    public Tryte[] receive(final Tryte[] channel) {

        final Nonet channelId = new Nonet(channel);
        final Queue<Tryte[]> incomingMessages = this.incomingMessages.get(channelId);
        if (incomingMessages == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }

        return incomingMessages.poll();
    }

    @Override
    public void join(final byte[] channel) {

        final Nonet channelId = new Nonet(Converter.getTrytes(channel));
        if (outgoingMessages.containsKey(channelId)) {

            if (!listenedChannelIds.add(channelId)) {

                throw new RuntimeException("Already joined channel: " + channelId);
            }

        } else {

            throw new RuntimeException("Non-existent channel: " + channelId);
        }
    }

    @Override
    public void leave(final byte[] channel) {

        final Nonet channelId = new Nonet(Converter.getTrytes(channel));
        if (outgoingMessages.containsKey(channelId)) {

            if (!listenedChannelIds.remove(channelId)) {

                throw new RuntimeException("Non-joined channel: " + channelId);
            }

        } else {

            throw new RuntimeException("Non-existent channel: " + channelId);
        }
    }

    @Override
    public void send(final byte[] channel, final byte[] message) {

        final Nonet channelId = new Nonet(Converter.getTrytes(channel));
        final Queue<Tryte[]> incomingMessages = this.incomingMessages.get(channelId);
        if (incomingMessages == null) {

            throw new RuntimeException("Non-existent channel: " + channelId);

        }

        if (listenedChannelIds.contains(channelId)) {

            incomingMessages.offer(Converter.getTrytes(message));

        } else {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }
    }

    @Override
    public byte[] receive(final byte[] channel) {

        final Nonet channelId = new Nonet(Converter.getTrytes(channel));
        final Queue<Tryte[]> outgoingMessages = this.outgoingMessages.get(channelId);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-existent channel: " + channelId);

        }

        if (listenedChannelIds.contains(channelId)) {

            return Converter.getBytes(outgoingMessages.poll());

        } else {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }
    }
}
