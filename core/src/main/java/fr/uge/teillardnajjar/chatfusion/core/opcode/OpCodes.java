package fr.uge.teillardnajjar.chatfusion.core.opcode;

public final class OpCodes {
    public static final byte AUTH = 0x00;
    public static final byte AUTHOK = 0x01;
    public static final byte AUTHKO = 0x02;
    public static final byte TEMP = 0x03;
    public static final byte TEMPOK = 0x04;
    public static final byte TEMPKO = 0x05;
    public static final byte MSG = 0x10;
    public static final byte MSGRESP = 0x11;
    public static final byte MSGFWD = 0x12;
    public static final byte PRIVMSG = 0x20;
    public static final byte PRIVMSGRESP = 0x21;
    public static final byte PRIVMSGFWD = 0x22;
    public static final byte PRIVFILE = 0x30;
    public static final byte PRIVFILERESP = 0x31;
    public static final byte PRIVFILEFWD = 0x32;
    public static final byte FUSIONREQ = 0x40;
    public static final byte FUSIONREQFWDA = 0x41;
    public static final byte FUSIONREQDENY = 0x42;
    public static final byte FUSIONREQACCEPT = 0x43;
    public static final byte FUSIONREQFWDB = 0x44;
    public static final byte FUSION = 0x50;
    public static final byte FUSIONLINK = 0x51;
    public static final byte FUSIONLINKACCEPT = 0x52;
    public static final byte FUSIONEND = 0x53;

    private OpCodes() {
        throw new AssertionError("No instance");
    }
}
