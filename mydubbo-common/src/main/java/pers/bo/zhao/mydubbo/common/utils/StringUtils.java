package pers.bo.zhao.mydubbo.common.utils;

import pers.bo.zhao.mydubbo.common.io.UnsafeStringWriter;

import java.io.PrintWriter;

/**
 * @author Bo.Zhao
 * @since 19/2/11
 */
public class StringUtils {

    private StringUtils() {
        throw new RuntimeException("工具类不可实例化");
    }


    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence charSequence) {
        return charSequence != null && charSequence.length() > 0;
    }

    public static String toString(Throwable t) {
        UnsafeStringWriter w = new UnsafeStringWriter();

        try (PrintWriter p = new PrintWriter(w)) {
            p.append(t.getClass().getName());

            if (t.getMessage() != null) {
                p.append(": ").append(t.getMessage());
            }
            p.println();

            t.printStackTrace(p);
            return w.toString();
        }
    }

    public static String camelToSplitName(String camelName, String split) {
        if (isEmpty(camelName)) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); i++) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName, 0, i);
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? camelName : buf.toString();
    }
}
