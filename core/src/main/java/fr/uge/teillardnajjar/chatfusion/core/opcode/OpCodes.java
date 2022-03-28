package fr.uge.teillardnajjar.chatfusion.core.opcode;

public final class OpCodes {
    public static final int AUTH = 0x00;
    public static final int AUTHOK = 0x01;
    public static final int AUTHKO = 0x02;
    public static final int TEMP = 0x03;
    public static final int TEMPOK = 0x04;
    public static final int TEMPKO = 0x05;
    public static final int MSG = 0x10;
    public static final int MSGRESP = 0x11;
    public static final int MSGFWD = 0x12;
    public static final int PRIVMSG = 0x20;
    public static final int PRIVMSGRESP = 0x21;
    public static final int PRIVMSGFWD = 0x22;
    public static final int PRIVFILE = 0x30;
    public static final int PRIVFILERESP = 0x31;
    public static final int PRIVFILEFWD = 0x32;
    public static final int FUSIONREQ = 0x40;
    public static final int FUSIONREQFWDA = 0x41;
    public static final int FUSIONREQDENY = 0x42;
    public static final int FUSIONREQACCEPT = 0x43;
    public static final int FUSIONREQFWDB = 0x44;
    public static final int FUSION = 0x50;
    public static final int FUSIONLINK = 0x51;
    public static final int FUSIONLINKACCEPT = 0x52;
    public static final int FUSIONEND = 0x53;

    private OpCodes() {
        throw new AssertionError("No instance");
    }
}
