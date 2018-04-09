package com.xk.redis;

import org.apache.ibatis.cache.decorators.LoggingCache;

/** 
 * @author xiaokang
 * @Description 为mybatis-redis加入日志功能
 * @time 2016年6月14日 下午5:41:20 
 */
public class LoggingRedisCache extends LoggingCache
{
	// <cache eviction="LRU" type="cn.tk.hera.insure.common.redis.LoggingRedisCache" />
	public LoggingRedisCache(String id)
	{
		super(new MybatisRedisCache(id));
	}

}
