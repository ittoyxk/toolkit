/**
 *
 */
package com.xk.batch;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步结果对象,供返回值使用
 *
 * @author wangwc11
 * @date 2016年8月23日
 */
public class AsyncResult<V> implements ListenableFuture<V> {
    private final V value;
    private final ExecutionException executionException;

    public AsyncResult(V value)
    {
        this(value, null);
    }

    /**
     * @param value
     * @param executionException
     */
    public AsyncResult(V value, ExecutionException executionException)
    {
        this.value = value;
        this.executionException = executionException;
    }

    /**
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    /**
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled()
    {
        return false;
    }

    /**
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone()
    {
        return true;
    }

    /**
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public V get() throws InterruptedException, ExecutionException
    {
        if (this.executionException != null) {
            throw this.executionException;
        }
        return this.value;
    }

    /**
     * @see java.util.concurrent.Future#get(long, TimeUnit)
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return this.get();
    }

    /**
     * @see com.google.common.util.concurrent.ListenableFuture#addListener(Runnable,
     * Executor)
     */
    @Override
    public void addListener(Runnable listener, Executor executor)
    {
        executor.execute(listener);
    }

}
