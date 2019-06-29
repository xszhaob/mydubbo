package pers.bo.zhao.mydubbo.common.timer;

/**
 * @author Bo.Zhao
 * @since 19/4/28
 */
public interface TimerTask {

    void run(Timeout timeout) throws Exception;
}
