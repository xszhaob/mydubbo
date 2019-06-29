package pers.bo.zhao.mydubbo.common.timer;

/**
 * @author Bo.Zhao
 * @since 19/4/28
 */
public interface Timeout {

    /**
     * 返回创建这个处理的timer
     */
    Timer timer();

    /**
     * 返回和这个处理相关联的task
     */
    TimerTask task();

    /**
     * 只有和这个处理相关联的task已经过期时才返回true
     */
    boolean isExpired();

    /**
     * 只有和这个处理相关联的task已经取消时才返回true
     */
    boolean isCancelled();

    /**
     * 尝试取消和这个处理相关联的task。
     * 如果任务已经执行完成或已取消，返回的结果没有副作用？
     *
     * @return true如果取消操作成功完成，否则返回false
     */
    boolean cancel();
}
