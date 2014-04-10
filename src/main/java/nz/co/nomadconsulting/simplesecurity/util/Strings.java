package nz.co.nomadconsulting.simplesecurity.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;


public class Strings {
    public static String unqualify(final String name) {
        return unqualify(name, '.');
    }


    public static String unqualify(final String name, final char sep) {
        return name.substring(name.lastIndexOf(sep) + 1, name.length());
    }


    public static boolean isEmpty(final String string) {
        int len;
        if (string == null || (len = string.length()) == 0) {
            return true;
        }

        for (int i = 0; i < len; i++) {
            if (Character.isWhitespace(string.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }


    public static String nullIfEmpty(final String string) {
        return isEmpty(string) ? null : string;
    }


    public static String emptyIfNull(final String string) {
        return string == null ? "" : string;
    }


    public static String toString(final Object component) {
        try {
            final PropertyDescriptor[] props = Introspector.getBeanInfo(
                    component.getClass()).getPropertyDescriptors();
            final StringBuilder builder = new StringBuilder();
            for (final PropertyDescriptor descriptor : props) {
                builder.append(descriptor.getName()).append('=')
                .append(descriptor.getReadMethod().invoke(component))
                .append("; ");
            }
            return builder.toString();
        }
        catch (final Exception e) {
            return "";
        }
    }


    public static String[] split(final String strings, final String delims) {
        if (strings == null) {
            return new String[0];
        }
        else {
            final StringTokenizer tokens = new StringTokenizer(strings, delims);
            final String[] result = new String[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                result[i++] = tokens.nextToken();
            }
            return result;
        }
    }


    public static String toString(final Object... objects) {
        return toString(" ", objects);
    }


    public static String toString(final String sep, final Object... objects) {
        if (objects.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Object object : objects) {
            builder.append(sep).append(object);
        }
        return builder.substring(sep.length());
    }


    public static String toClassNameString(final String sep,
            final Object... objects) {
        if (objects.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Object object : objects) {
            builder.append(sep);
            if (object == null) {
                builder.append("null");
            }
            else {
                builder.append(object.getClass().getName());
            }
        }
        return builder.substring(sep.length());
    }


    public static String toString(final String sep, final Class<?>... classes) {
        if (classes.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Class<?> clazz : classes) {
            builder.append(sep).append(clazz.getName());
        }
        return builder.substring(sep.length());
    }


    public static String toString(final InputStream in) throws IOException {
        final StringBuilder out = new StringBuilder();
        final byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
