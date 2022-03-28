package fr.uge.teillardnajjar.chatfusion.core.util.concurrent;

import java.util.NoSuchElementException;

public class Pipe<T> {
    private final Object lock = new Object();
    private T content;

    public void in(T content) {
        synchronized (lock) {
            this.content = content;
        }
    }

    public T out() {
        synchronized (lock) {
            if (content == null) throw new NoSuchElementException("No content");
            var ret = content;
            content = null;
            return ret;
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return content == null;
        }
    }
}