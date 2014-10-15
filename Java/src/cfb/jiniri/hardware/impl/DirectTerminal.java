package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Antiterminal;
import cfb.jiniri.hardware.Terminal;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;
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

    private final Set<Tryte> listenedChannelIds;

    private final Map<Tryte, Queue<Trit[]>> outgoingMessages;
    private final Map<Tryte, Queue<Trit[]>> incomingMessages;

    public DirectTerminal() {

        listenedChannelIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        outgoingMessages = new ConcurrentHashMap<>();
        incomingMessages = new ConcurrentHashMap<>();
    }

    @Override
    public void join(final Tryte channel) {

        if (outgoingMessages.putIfAbsent(channel, new ConcurrentLinkedQueue<>()) != null) {

            throw new RuntimeException("Already joined channel: " + channel);
        }
        incomingMessages.put(channel, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void leave(final Tryte channel) {

        if (outgoingMessages.remove(channel) == null) {

            throw new RuntimeException("Non-joined channel: " + channel);
        }
        incomingMessages.remove(channel);

        listenedChannelIds.remove(channel);
    }

    @Override
    public void send(final Tryte channel, final Trit[] message) {

        final Queue<Trit[]> outgoingMessages = this.outgoingMessages.get(channel);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-joined channel: " + channel);
        }

        if (listenedChannelIds.contains(channel)) {

            outgoingMessages.offer(message);
        }
    }

    @Override
    public Trit[] receive(final Tryte channel) {

        final Queue<Trit[]> incomingMessages = this.incomingMessages.get(channel);
        if (incomingMessages == null) {

            throw new RuntimeException("Non-joined channel: " + channel);
        }

        return incomingMessages.poll();
    }

    @Override
    public void join(final byte[] channel) {

        final Tryte channel2 = new Tryte(Converter.getTrits(channel));
        if (outgoingMessages.containsKey(channel2)) {

            if (!listenedChannelIds.add(channel2)) {

                throw new RuntimeException("Already joined channel: " + channel2);
            }

        } else {

            throw new RuntimeException("Non-existent channel: " + channel2);
        }
    }

    @Override
    public void leave(final byte[] channel) {

        final Tryte channel2 = new Tryte(Converter.getTrits(channel));
        if (outgoingMessages.containsKey(channel2)) {

            if (!listenedChannelIds.remove(channel2)) {

                throw new RuntimeException("Non-joined channel: " + channel2);
            }

        } else {

            throw new RuntimeException("Non-existent channel: " + channel2);
        }
    }

    @Override
    public void send(final byte[] channel, final byte[] message) {

        final Tryte channel2 = new Tryte(Converter.getTrits(channel));
        final Queue<Trit[]> incomingMessages = this.incomingMessages.get(channel2);
        if (incomingMessages == null) {

            throw new RuntimeException("Non-existent channel: " + channel2);

        }

        if (listenedChannelIds.contains(channel2)) {

            incomingMessages.offer(Converter.getTrits(message));

        } else {

            throw new RuntimeException("Non-joined channel: " + channel2);
        }
    }

    @Override
    public byte[] receive(final byte[] channel) {

        final Tryte channel2 = new Tryte(Converter.getTrits(channel));
        final Queue<Trit[]> outgoingMessages = this.outgoingMessages.get(channel2);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-existent channel: " + channel2);

        }

        if (listenedChannelIds.contains(channel2)) {

            return Converter.getBytes(outgoingMessages.poll());

        } else {

            throw new RuntimeException("Non-joined channel: " + channel2);
        }
    }
}
