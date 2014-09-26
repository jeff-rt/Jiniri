package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Terminal;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Nonet;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * (c) 2014 Come-from-Beyond
 */
public class DirectTerminal implements Terminal {

    private final AtomicInteger operatorCounter;

    private final Map<Multiplet, Map<Integer, Queue<Tryte[]>>> outgoingMessages;
    private final Map<Multiplet, Queue<Tryte[]>> incomingMessages;

    public DirectTerminal() {

        operatorCounter = new AtomicInteger();

        outgoingMessages = new ConcurrentHashMap<>();
        incomingMessages = new ConcurrentHashMap<>();
    }

    @Override
    public void join(final Tryte[] channel) {

        final Multiplet channelId = new Nonet(channel);
        if (outgoingMessages.putIfAbsent(channelId, new ConcurrentHashMap<>()) != null) {

            throw new RuntimeException("Already joined channel: " + channelId);
        }
        incomingMessages.put(channelId, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void leave(final Tryte[] channel) {

        final Multiplet channelId = new Nonet(channel);
        if (outgoingMessages.remove(channelId) == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }
        incomingMessages.remove(channelId);
    }

    @Override
    public void send(final Tryte[] channel, final Tryte[] message) {

        final Multiplet channelId = new Nonet(channel);
        final Map<Integer, Queue<Tryte[]>> outgoingMessages = this.outgoingMessages.get(channelId);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }

        for (final Queue<Tryte[]> messageQueue : outgoingMessages.values()) {

            messageQueue.offer(message);
        }
    }

    @Override
    public Tryte[] receive(final Tryte[] channel) {

        final Multiplet channelId = new Nonet(channel);
        final Queue<Tryte[]> messageQueue = incomingMessages.get(channelId);
        if (messageQueue == null) {

            throw new RuntimeException("Non-joined channel: " + channelId);
        }

        return messageQueue.poll();
    }

    public int getOperatorId() {

        return operatorCounter.getAndIncrement();
    }

    public void push(final Tryte[] channel, final Tryte[] message) {

        final Multiplet channelId = new Nonet(channel);
        final Queue<Tryte[]> messageQueue = incomingMessages.get(channelId);
        if (messageQueue == null) {

            throw new RuntimeException("Non-existent channel: " + channelId);
        }

        messageQueue.offer(message);
    }

    public Tryte[] pull(final Tryte[] channel, final int operatorId) {

        final Multiplet channelId = new Nonet(channel);
        final Map<Integer, Queue<Tryte[]>> outgoingMessages = this.outgoingMessages.get(channelId);
        if (outgoingMessages == null) {

            throw new RuntimeException("Non-existent channel: " + channelId);
        }

        final Queue<Tryte[]> messageQueue = outgoingMessages.get(operatorId);
        if (messageQueue == null) {

            outgoingMessages.put(operatorId, new ConcurrentLinkedQueue<>());

            return null;

        } else {

            return messageQueue.poll();
        }
    }
}
