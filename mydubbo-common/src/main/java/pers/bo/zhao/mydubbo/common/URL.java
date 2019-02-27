package pers.bo.zhao.mydubbo.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Bo.Zhao
 * @since 19/2/10
 */
public final class URL {



    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
