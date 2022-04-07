package fr.uge.teillardnajjar.chatfusion.core.util;

public class Conversions {
    private Conversions() {
        throw new AssertionError("No instance");
    }

    public static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
            ((bytes[1] & 0xFF) << 16) |
            ((bytes[2] & 0xFF) << 8) |
            ((bytes[3] & 0xFF));
    }

    public static byte[] toByteArray(int i) {
        return new byte[]{
            (byte) ((i >> 24) & 0xff),
            (byte) ((i >> 16) & 0xff),
            (byte) ((i >> 8) & 0xff),
            (byte) ((i) & 0xff),
        };
    }
}
