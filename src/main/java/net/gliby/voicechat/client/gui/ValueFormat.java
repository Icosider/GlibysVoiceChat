package net.gliby.voicechat.client.gui;

public class ValueFormat {
    private static final char[] PREFIXS = new char[]{'K', 'M', 'B', 'T'};

    public static String format(long value, int settings) {
        StringBuilder sb = new StringBuilder(32);
        sb.append(value);
        char[] data = sb.toString().toCharArray();
        boolean commas = (settings & 1) == 1;
        int precision = 0;
        int prefix = 0;

        if (settings >= 64) {
            prefix = settings >> 6;

            if (prefix > PREFIXS.length) {
                prefix = PREFIXS.length;
            }
        }

        if (settings > 1) {
            precision = settings >> 2 & 15;
        }

        sb.setLength(0);
        byte negative = 0;

        if (data[0] == 45) {
            negative = 1;
        }

        int length = data.length - negative;

        if (prefix * 3 >= length) {
            prefix = (int) ((double) length * 0.334D);

            if (prefix * 3 == length && precision == 0) {
                --prefix;
            }
        }

        int end = length - prefix * 3;
        int start = length % 3;

        if (start == 0) {
            start = 3;
        }

        start += negative;

        if (end > 0 && negative == 1) {
            sb.append('-');
        }

        int max = end + negative;

        int i;

        for (i = negative; i < max; ++i) {
            if (i == start && i + 2 < max && commas) {
                start += 3;
                sb.append(',');
            }

            sb.append(data[i]);
        }

        if (prefix > 0) {
            if (end == 0) {
                if (negative == 1 && precision > 0) {
                    sb.append('-');
                }
                sb.append('0');
            }

            max = precision + end + negative;

            if (max > data.length) {
                max = data.length;
            }

            for (end += negative; max > end && data[max - 1] == 48; --max) {
            }

            if (max - end != 0) {
                sb.append('.');
            }

            for (i = end; i < max; ++i) {
                sb.append(data[i]);
            }
            sb.append(PREFIXS[prefix - 1]);
        }

        return sb.toString();
    }

    static int PRECISION(int precision) {
        return precision << 2;
    }

    public static int PREFIX(int prefix) {
        return prefix << 6;
    }

    public static String toString(int settings) {
        return "Prefix: " +
                (Math.min(settings >> 6, PREFIXS.length)) +
                ", Precision: " +
                (settings >> 2 & 15) +
                ", Commas: " +
                ((settings & 1) == 1);
    }
}