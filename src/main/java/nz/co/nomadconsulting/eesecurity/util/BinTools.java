package nz.co.nomadconsulting.eesecurity.util;

public class BinTools {

    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();


    public static String bin2hex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static byte[] hex2bin(final String s) {
        String m = s;
        if (s == null) {
            // Allow empty input string.
            m = "";
        }
        else if (s.length() % 2 != 0) {
            // Assume leading zero for odd string length
            m = "0" + s;
        }
        final byte r[] = new byte[m.length() / 2];
        for (int i = 0, n = 0; i < m.length(); n++) {
            final char h = m.charAt(i++);
            final char l = m.charAt(i++);
            r[n] = (byte) (hex2bin(h) * 16 + hex2bin(l));
        }
        return r;
    }


    public static int hex2bin(final char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        throw new IllegalArgumentException(
                "Input string may only contain hex digits, but found '" + c
                + "'");
    }
}
