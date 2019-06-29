package pers.bo.zhao.mydubbo.common.timer;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Bo.Zhao
 * @since 19/4/28
 */
public interface Timer {

    Timeout newTimeout(TimerTask task, long delay, TimeUnit unit);

    Set<Timeout> stop();

    boolean isStop();

}
