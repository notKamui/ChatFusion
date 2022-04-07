package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

/**
 * Interface representing {@link java.nio.ByteBuffer} readers.
 * <p>
 * A reader must be able to process a buffer,
 * get the intended value from its parameter type,
 * and reset its internal status.
 *
 * @param <T> the type of value to be read
 */
public interface Reader<T> {

    /**
     * Processes the given buffer.
     *
     * @param buffer the buffer to process
     * @return the process status
     */
    ProcessStatus process(ByteBuffer buffer);

    /**
     * Gets the value that has been processed.
     * <p>
     * You can only call this method after a call to {@link #process(java.nio.ByteBuffer)} returned DONE.
     *
     * @return the value that has been processed
     */
    T get();

    /**
     * Resets the reader's internal status.
     */
    void reset();

    /**
     * Internal process status values that the reader can return.
     *
     * <li>
     * DONE means that the next value has been completely read.
     * </li>
     * <li>
     * REFILL means that the next value is not yet completely read.
     * </li>
     * <li>
     * ERROR means that an error occurred while reading the next value, which is unrecoverable.
     * </li>
     */
    enum ProcessStatus {DONE, REFILL, ERROR}
}