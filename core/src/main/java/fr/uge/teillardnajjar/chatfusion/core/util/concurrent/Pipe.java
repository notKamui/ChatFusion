package fr.uge.teillardnajjar.chatfusion.core.util.concurrent;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe pipe. This is basically equivalent to a {@link java.util.concurrent.BlockingQueue}
 * but is simpler to use.
 *
 * @param <T> The type of elements in the pipe.
 */
public class Pipe<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final ArrayDeque<T> pipe = new ArrayDeque<>();

    public void in(T content) {
        Objects.requireNonNull(content);
        lock.lock();
        try {
            pipe.offer(content);
        } finally {
            lock.unlock();
        }
    }

    public T out() {
        lock.lock();
        try {
            if (isEmpty()) throw new NoSuchElementException("No content");
            return pipe.poll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return pipe.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}