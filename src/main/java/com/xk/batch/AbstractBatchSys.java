package com.xk.batch;

import com.xk.httpclient.common.util.PropertiesUtil;
import com.xk.resource.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


// 此类一个针对定时批量处理的抽象类，能够满足下列场景：一个定时线程定时从一个地方获取一批数据并形成线程放入到线程池中；
// 并能够智能根据当前线程数及排队长度，自动调整线程池中线程的个数。
// 自动使用线程池批量执行任务，并将执行结果方法到状态队列中 ；
// 一个定时任务定时从状态队列中获取数据并执行后续操作

// 1. 当需要更新队列不为空时，使用单线程定时获取数据。否则，则等待队列为空的通知

// 2. 将获取到的数据形成线程对象添加到线程池中，并发执行。

// 3. 使用单线程将执行后的结果更新。 使用方法：

// 3.1. 实现执行对象
// 3.2. 继承类，比如：
// public classTaiKangEmailSys extends AbstractBatchSys<InsureEmail>
// 3.3. 实现下面几个方法：
// 3.3.1 读取批量数据方法,批处理框架将会自动将这些数据包装成不同的线程
// public abstract List<T> read_data();
// 3.3.2 数据执行任务方法, 如果不需要处理结果则excute_taskImpl方法返回null
// public abstract T excute_taskImpl(Tsms_info_value);
// 3.3.3 单条数据更新方法, 如果不需要更新数据则保持为空方法
// public abstract void update_taskImpl(T value);

// 4. 可以根据具体情况重载下列方法：
// 4.1 加载环境信息
// public void loadEnv()
// 4.2 当需要高性能批量更新状态数据时，请直接覆盖下列方法
// public void update_batchTaskImpl(Iterator<T> values)
//
public abstract class AbstractBatchSys<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBatchSys.class);

    // 待执行任务的对列
    private LinkedBlockingQueue<Runnable> task_queue = new LinkedBlockingQueue<Runnable>();

    // 执行任务的线程池
    private ThreadPoolExecutor executor = null;

    // 定时任务
    private ScheduledThreadPoolExecutor scheduleExecutor = null;

    // 保存状态数据的队列
    private LinkedBlockingQueue<T> task_status_queue = new LinkedBlockingQueue<T>();

    // 数据库连接池
    private ConnectionManager pool = null;

    // 线程池停止标志位
    protected boolean stopFlag = true;

    // 打印调试信息的标志
    protected volatile boolean debugFlag = false;

    // 读取数据的间隔时间，间隔0.5秒
    private volatile int readInterval = Integer.valueOf(PropertiesUtil.getProperty("application.properties","readInterval"));

    // 更新状态间隔是0.2秒
    private volatile int updateInterval =Integer.valueOf(PropertiesUtil.getProperty("application.properties","updateInterval"));

    // 线程池初始线程大小
    private volatile int defaultCorePoolSize = Integer.valueOf(PropertiesUtil.getProperty("application.properties","defaultCorePoolSize"));

    // 线程池最大线程大小
    private volatile int coreMaxSize = Integer.valueOf(PropertiesUtil.getProperty("application.properties","coreMaxSize"));

    // 当前设置的线程大小
    private volatile int currPoolSize = defaultCorePoolSize;

    // 定时线程持大小
    private int schedulePoolSize = Integer.valueOf(PropertiesUtil.getProperty("application.properties","schedulePoolSize"));

    // 线程池调整的阈值
    private static final int AdjustLatchNum =Integer.valueOf(PropertiesUtil.getProperty("application.properties","AdjustLatchNum"));

    // 调整线程池的阈值，缺省是10次
    private int adjustLatch = AdjustLatchNum;

    // 保存环境信息的上下文,可用于读取配置文件数据
    protected HashMap<String, Object> context = new HashMap<String, Object>();

    // 更新队列使用的锁
    private Lock lock = new ReentrantLock();
    // 更新队列为空的条件
    private Condition notEmpty = lock.newCondition();


    // 数据连接名称
    private String jdbc_datasource = PropertiesUtil.getProperty("application.properties","jdbc_datasource");

    // 任务验收执行时间
    private int taskDelayTime = 0;// 可以设置为200毫秒

    /**
     * @return the taskDelayTime
     */
    public int getTaskDelayTime()
    {
        return taskDelayTime;
    }

    /**
     * @param taskDelayTime the taskDelayTime to set
     */
    public void setTaskDelayTime(int taskDelayTime)
    {
        this.taskDelayTime = taskDelayTime;
    }

    // 设置数据库连接池
    public void setDataSouceName(String dataSource)
    {
        jdbc_datasource = dataSource;
    }

    // 获取当前定时任务池中的线程个数
    public int getSchedulePoolSize()
    {
        return schedulePoolSize;
    }

    // 至少是3个线程,因为一个用来做定时获取数据,一个用来更新数据,其他的进行其它定时任务
    public void setSchedulePoolSize(int schedulePoolSize)
    {
        if (schedulePoolSize >= 3) this.schedulePoolSize = schedulePoolSize;
    }

    public boolean isStopFlag()
    {
        return this.stopFlag;
    }

    public void setStopFlag(boolean stop_flag)
    {
        this.stopFlag = stop_flag;
    }

    public boolean isDebugFlag()
    {
        return debugFlag;
    }

    public int getCoreMaxSize()
    {
        return coreMaxSize;
    }

    public void setCoreMaxSize(int coreMaxSize)
    {
        this.coreMaxSize = coreMaxSize;
    }

    public int getCurrPoolSize()
    {
        return currPoolSize;
    }

    public void setCurrPoolSize(int currPoolSize)
    {
        this.currPoolSize = currPoolSize;
    }

    public int getCorePoolSize()
    {
        return defaultCorePoolSize;
    }

    public HashMap<String, Object> getContext()
    {
        return context;
    }

    // 构造函数
    protected AbstractBatchSys()
    {
        initEnv();
    }

    // 生成连接池对象
    public void initEnv()
    {
        try {
            loadEnv();
            if (this.jdbc_datasource != null) {
                pool = new ConnectionManager(this.jdbc_datasource);
            }
        } catch (Exception e) {

        }
    }

    // 输出调试日志
    public void setDebugFlag(boolean debug_flag_value)
    {
        logger.debug("setDebugFlag(boolean) - set debug_flag===={}", debug_flag_value);
        this.debugFlag = debug_flag_value;
    }

    // 输出调试日志
    public boolean getStopFlag()
    {
        logger.debug("getStopFlag() - set stop_flag===={}", this.stopFlag);
        return this.stopFlag;
    }

    // 设置最大的线程数
    public void setMaximumPoolSize(int coreMaxSize)
    {
        this.coreMaxSize = coreMaxSize;
        executor.setMaximumPoolSize(coreMaxSize);
    }

    // 设置最小的线程数
    public void setCorePoolSize(int corePoolSize)
    {
        this.defaultCorePoolSize = corePoolSize;
        executor.setCorePoolSize(corePoolSize);
    }

    // 获得读取间隔时间
    public int getReadInterval()
    {
        return readInterval;
    }

    // 设置读取间隔时间
    public void setReadInterval(int _readInterval)
    {
        this.readInterval = _readInterval;
    }

    // 获得更新间隔时间
    public int getUpdateInterval()
    {
        return updateInterval;
    }

    // 设置更新间隔时间
    public void setUpdateInterval(int _updateInterval)
    {
        this.updateInterval = _updateInterval;
    }

    // 增加对象到状态队列中
    public void addStatueQueue(T value)
    {
        if (value != null) task_status_queue.add(value);
    }

    // 获取当前任务队列的长度
    public long getTaskCount()
    {
        return executor.getTaskCount();
    }

    // 获取数据库连接
    public Connection getConnection() throws Exception
    {
        if (pool != null) {
            return pool.createConnection();
        } else {
            return null;
        }
    }

    ;

    // 关闭数据库连接
    public void closeConnection(Connection conn)
    {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            logger.error("closeConnection(Connection) - close connection exception:", e);
        }
    }

    // 使用缺失参数启动
    public synchronized void startThreads()
    {
        this.startThreads(1, 20, 500, 200);
    }

    // 启动线程池,并执行方法
    public synchronized void startThreads(int corePoolSize, int coreMaxSize, int _readInterval, int _updateInterval)
    {
        if (this.stopFlag) {
            this.stopFlag = false;

            loadEnv();

            this.readInterval = _readInterval;
            this.updateInterval = _updateInterval;
            this.defaultCorePoolSize = corePoolSize;
            this.coreMaxSize = coreMaxSize;

            // 启动线程池
            executor = new ThreadPoolExecutor(this.defaultCorePoolSize, this.coreMaxSize, 5, TimeUnit.SECONDS,
                    task_queue);

            scheduleExecutor = new ScheduledThreadPoolExecutor(this.schedulePoolSize);

            DateFormat d5 = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
            Date now = new Date();
            logger.debug("startThreads1(int, int, int, int) - Start the batch system at: {}", d5.format(now));

            // 启动读取未数据线程
            if (_readInterval > 0) {
                Common_Reader_Thread reader_thread = new Common_Reader_Thread();
                scheduleExecutor.scheduleAtFixedRate(reader_thread, 0, this.readInterval, TimeUnit.MILLISECONDS);
            }

            // 启动更新数据库状态的线程
            if (_updateInterval > 0) {
                Common_Update_Thread status_update_thread = new Common_Update_Thread();
                scheduleExecutor.scheduleAtFixedRate(status_update_thread, 0, this.updateInterval,
                        TimeUnit.MILLISECONDS);
            }
        } else {
            logger.debug("startThreads1(int, int, int, int) - Batch system already started");
        }
    }

    // 生成任务并添加到队列中
    public void addTask(T value)
    {
        executor.execute(new Common_Execute_Worker(value));
    }

    // 增加任务
    public void addTask(Runnable value)
    {
        executor.execute(value);
    }

    // 增加异步任务
    public Future<Boolean> addFutureTask(Callable<Boolean> callable)
    {
        return executor.submit(callable);
    }

    // 增加延时执行任务
    public ScheduledFuture<?> addTask(T value, int delay)
    {
        return scheduleExecutor.schedule(new Common_Execute_Worker(value), delay, TimeUnit.SECONDS);
    }

    // 获取当前系统状况,使用json格式
    public String getStatus()
    {
        StringBuffer result = new StringBuffer(1000);
        if (this.stopFlag) {
            result.append("{status:stopped}");
        } else {
            result.append("{status:running,");
            result.append(",activeThreads:" + executor.getActiveCount());
            result.append(",waitThreads:" + executor.getTaskCount());
            result.append(",maximumPoolSize:" + executor.getMaximumPoolSize());
            result.append(",corePoolSize:" + executor.getCorePoolSize());
            result.append(" statusQueue:" + task_status_queue.size());
            result.append("}");
        }

        return result.toString();
    }

    public void destroy()
    {
        stopThreads();
    }

    // 停止所用线程
    public synchronized void stopThreads()
    {
        this.stopFlag = true;
        // 关闭批量执行任务线程池
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

        // 关闭定时任务线程池
        if (scheduleExecutor != null) {
            scheduleExecutor.shutdown();
            scheduleExecutor = null;
        }
        DateFormat d5 = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
        Date now = new Date();
        logger.debug("stopThreads() - Stop batch system at: {}", d5.format(now));
    }

    /*
     * 智能的动态在获取数据线程中调整线程池的大小,调整原则： ：1.当超过线程池最大值的2倍时，增大基本线程池的大小
     * 2。昂线程池长期处于空闲时，减少线程池的大小. 3.当连续10次满足条件时进行调整
     */
    private void adjustThreadPool()
    {
        if (executor.getTaskCount() == 0 && executor.getCorePoolSize() <= executor.getMaximumPoolSize()
                && getCurrPoolSize() > executor.getCorePoolSize()) {
            adjustLatch--;

            // 减少线程数
            if (adjustLatch == 0) {
                adjustLatch = AdjustLatchNum;
                this.currPoolSize = this.defaultCorePoolSize;
                executor.setCorePoolSize(this.currPoolSize);
                if (debugFlag) {
                    logger.info("set the connection default pool:{}", getStatus());
                }
            }
        } else if (executor.getTaskCount() > executor.getMaximumPoolSize() * 3
                && executor.getCorePoolSize() < executor.getMaximumPoolSize()) {

            adjustLatch--;
            // 增加线程数
            if (adjustLatch == 0) {
                adjustLatch = AdjustLatchNum;
                int incr = (executor.getMaximumPoolSize() - executor.getCorePoolSize()) / 2;

                this.currPoolSize = executor.getCorePoolSize() + (incr > 0 ? incr : 1);

                executor.setCorePoolSize(this.currPoolSize);

                if (debugFlag) {
                    logger.info("adjust the connection pool:{}", getStatus());
                }
            }
        }
    }

    // 获取数据并放到等待队列中
    public class Common_Reader_Thread implements Runnable {// extends Thread {

        public Common_Reader_Thread()
        {
            DateFormat d5 = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
            Date now = new Date();
            logger.debug("Common_Reader_Thread() - Start read thread at: ", d5.format(now));
        }

        public void run()
        {
            if (!stopFlag) {
                // 读取数据，并生成等待线程对象. 当等待更新的对象队列为空时才能继续读取新的数据,否则就等待
                lock.lock();
                try {
                    while (task_status_queue.size() > 0) {
                        notEmpty.await();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }

                // 在增加新的任务之前，动态调整线程数
                adjustThreadPool();

                // 读取数据
                List<T> data = read_data();
                // 根据数据生成执行线程加入到队列中
                if (data != null && data.size() > 0) {
                    for (T value : data) {
                        // 生成任务并添加到队列中
                        if (taskDelayTime == 0) {
                            addTask(value);
                        } else {
                            addTask(value, taskDelayTime);
                        }
                        // Common_Execute_Worker worker = new
                        // Common_Execute_Worker(value);
                        // addTask(worker);
                    }
                    data.clear();

                    // if (debugFlag) {
                    // System.out.println(getStatus());
                    // }
                }
            }
        }
    }

    // 负责执行线程
    private class Common_Execute_Worker implements Runnable {
        private T data_info;

        public Common_Execute_Worker(T data_info)
        {
            this.data_info = data_info;
        }

        // 执行任务并添加到状态队列中,如果执行结果为空，则不向状态队列添加
        public void run()
        {
            if (!stopFlag) {
                addStatueQueue(excute_taskImpl(data_info));
            }
        }
    }

    // 更新状态的线程
    public class Common_Update_Thread implements Runnable { // extends Thread {
        public Common_Update_Thread()
        {
            DateFormat d5 = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL); // 显示日期，周，时间（精确到秒）
            Date now = new Date();
            logger.debug("Common_Update_Thread() - Start update thread at: {}", d5.format(now));
        }

        // 队列不为空或者停止标志为假, 在队列为空并且stop_flag为false时停止执行
        public void run()
        {
            if (task_status_queue.size() > 0 || !stopFlag) {
                // if (debugFlag) {
                // logger.debug("run() - before: status queue size={}",
                // task_status_queue.size());
                // }
                if (task_status_queue.size() > 0) {
                    Iterator<T> values = task_status_queue.iterator();
                    update_batchTaskImpl(values);

                    lock.lock();
                    try {
                        notEmpty.signal();// 唤醒读线程
                    } finally {
                        lock.unlock();
                    }

                    // if (debugFlag) {
                    // logger.debug("run() - after: status queue size={}",
                    // task_status_queue.size());
                    // }
                }
            }
        }
    }

    // 批量执行更新,如果使用批量更新SQL语句则在子类中覆盖这个方法，否则实现update_taskImpl方法
    public void update_batchTaskImpl(Iterator<T> values)
    {
        while (values.hasNext()) {
            T value = values.next();
            try {
                update_taskImpl(value);
            } finally {
                values.remove();
            }
        }
    }

    // 加载环境信息, 可以在此设置连接池的名称
    public abstract void loadEnv();

    // 读取信息
    public abstract List<T> read_data();

    // 在线程池执行
    public abstract T excute_taskImpl(T value);

    // 单个实体更新
    public abstract void update_taskImpl(T value);

}
