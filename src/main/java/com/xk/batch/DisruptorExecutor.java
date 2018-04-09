package com.xk.batch;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使用disruptor实现的线程池,实现了ExecutorService接口，可以使用future
 *
 * @author wangwc11
 * @date 2016年9月12日
 */
public class DisruptorExecutor extends AbstractExecutorService implements EventFactory<DisruptorExecutor.RunnableEvent>,
        EventTranslatorOneArg<DisruptorExecutor.RunnableEvent, Runnable>, WorkHandler<DisruptorExecutor.RunnableEvent>, ExecutorService {

    private Disruptor<RunnableEvent> disruptor;

    private RingBuffer<RunnableEvent> ringBuffer;
    /**
     * ringBuffer的默认长度
     */
    private int bufferSize = 8192;
    private int paralleSize = 4;
    private AtomicBoolean running = new AtomicBoolean(false);

    public DisruptorExecutor()
    {
        start();
    }

    public DisruptorExecutor(int bufferSize, int paralleSize)
    {
        start(bufferSize, paralleSize);
    }

    public void start()
    {
        start(bufferSize, paralleSize);
    }

    @SuppressWarnings("unchecked")
    public void start(int bufferSize, int paralleSize)
    {
        if (!running.compareAndSet(false, true))
            return;
        disruptor = new Disruptor<>(this, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI,
                new BlockingWaitStrategy());
        disruptor.setDefaultExceptionHandler(new Slf4jExceptionHandler<RunnableEvent>());
        WorkHandler<RunnableEvent>[] works = new WorkHandler[paralleSize];
        for (int i = 0; i < paralleSize; i++) {
            works[i] = this;
        }
        disruptor.handleEventsWithWorkerPool(works);
        ringBuffer = disruptor.start();
    }

    @Override
    public void shutdown()
    {
        disruptor.shutdown();
        running.set(false);
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown()
    {
        return !running.get();
    }

    @Override
    public boolean isTerminated()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Runnable command)
    {
        ringBuffer.publishEvent(this, command);
    }


    @Override
    public RunnableEvent newInstance()
    {
        return new RunnableEvent();
    }

    @Override
    public void translateTo(RunnableEvent arg0, long arg1, Runnable arg2)
    {
        arg0.setValue(arg2);

    }

    @Override
    public void onEvent(RunnableEvent var1) throws Exception
    {
        var1.getValue().run();
    }

    @Override
    protected void finalize()
    {
        shutdown();
    }

    class RunnableEvent {
        private Runnable value;

        public RunnableEvent()
        {
        }

        public Runnable getValue()
        {
            return this.value;
        }

        public void setValue(Runnable value)
        {
            this.value = value;
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (!(o instanceof RunnableEvent)) return false;
            final RunnableEvent other = (RunnableEvent) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$value = this.getValue();
            final Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            return true;
        }

        public int hashCode()
        {
            final int PRIME = 59;
            int result = 1;
            final Object $value = this.getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        protected boolean canEqual(Object other)
        {
            return other instanceof RunnableEvent;
        }

        public String toString()
        {
            return "com.xk.batch.DisruptorExecutor.RunnableEvent(value=" + this.getValue() + ")";
        }
    }
}
