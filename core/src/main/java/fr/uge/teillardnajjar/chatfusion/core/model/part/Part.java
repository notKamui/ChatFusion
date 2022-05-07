package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;

/**
 * Composable part of a {@link fr.uge.teillardnajjar.chatfusion.core.model.frame.Frame}
 */
public interface Part {

    /**
     * Converts the part to an *unflipped* {@link ByteBuffer}.
     *
     * @return the buffer representing this part
     */
    ByteBuffer toBuffer();
}
