package fr.uge.teillardnajjar.chatfusion.core.context;

import java.io.IOException;

/**
 * Connection context for the chat server or client.
 */
public interface Context {

    /**
     * Performs the write action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doWrite and after the call
     *
     * @throws IOException if an I/O error occurs
     */
    void doWrite() throws IOException;

    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     *
     * @throws IOException if an I/O error occurs
     */
    void doRead() throws IOException;

    /**
     * Update the interestOps of the key looking only at values of the boolean
     * closed and of both ByteBuffers.
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * updateInterestOps and after the call. Also, it is assumed that process has
     * been called just before updateInterestOps.
     */
    void updateInterestOps();

    /**
     * Close the connection silently.
     */
    void silentlyClose();
}
