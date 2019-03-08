package pers.bo.zhao.mydubbo.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Bo.Zhao
 * @since 19/3/8
 */
public interface Result extends Serializable {

    Object getValue();

    Throwable getException();

    boolean hasException();

    Object recreate() throws Throwable;

    Map<String, String> getAttachments();

    void addAttachments(Map<String, String> map);

    void setAttachments(Map<String, String> map);

    String getAttachment(String key);

    String getAttachment(String key, String defaultValue);

    void setAttachment(String key, String value);

}
