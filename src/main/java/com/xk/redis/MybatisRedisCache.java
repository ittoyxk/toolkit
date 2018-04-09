/** 
 * @author 王文超 
 * @time 2016年6月14日 下午4:57:34  
 * 类说明 
 */
package com.xk.redis;

import com.xk.httpclient.common.util.PropertiesUtil;
import org.apache.ibatis.cache.Cache;
import redis.clients.jedis.ShardedJedis;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaokang
 * @Description mybatis-redis缓存,仅供mybatis使用，手动不要使用
 * @time 2016年6月14日 下午4:57:34
 */
public class MybatisRedisCache extends ShareJedisManager implements Cache
{
	private static final String MYBATIS_REDIS_CACHE =PropertiesUtil.getProperty("application.properties","MYBATIS_REDIS_CACHE");
	private String id;
	private String tableName;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public MybatisRedisCache(String id)
	{
		this.id = id;
		String[] split = id.split("\\.");
		if (split.length>1)
		{
			tableName=split[split.length-1];
		}
		logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>MybatisRedisCache:tableName={}" ,tableName);
	}

	@Override
	public void clear()
	{
		execute(new ShareJedisCallback<Object>()
		{
			@Override
			public Object doWithRedis(ShardedJedis jedis, String projecttrade)
			{
				logger.debug("clear the cache:{}",MYBATIS_REDIS_CACHE+tableName);
				return jedis.del(MYBATIS_REDIS_CACHE+tableName);
			}
		});
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public ReadWriteLock getReadWriteLock()
	{
		return readWriteLock;
	}

	@Override
	public int getSize()
	{
		return execute(new ShareJedisCallback<Integer>()
		{
			@Override
			public Integer doWithRedis(ShardedJedis jedis, String projecttrade)
			{
				Long hlen = jedis.hlen(MYBATIS_REDIS_CACHE+tableName);
				return hlen.intValue();
			}
		});
	}

	@Override
	public Object getObject(final Object key)
	{
		return execute(new ShareJedisCallback<Object>()
		{
			@Override
			public Object doWithRedis(ShardedJedis jedis, String projecttrade)
			{
				Object value = SerializeUtil.unserialize(jedis.hget(
						(MYBATIS_REDIS_CACHE+tableName).getBytes(),
						SerializeUtil.serialize(key.toString())));
				logger.debug("getObject key={},value={}", key, value);
				if (value == null)
				{
					removeObject(key);
				}
				return value;
			}
		});
	}

	@Override
	public void putObject(final Object key, final Object value)
	{
		logger.debug("putObject key:{}, value{}", key, value);
		if (value == null || "".equals(value))
		{
			return;
		}
		execute(new ShareJedisCallback<Object>()
		{
			@Override
			public Object doWithRedis(ShardedJedis jedis, String projecttrade)
			{
				jedis.hset((MYBATIS_REDIS_CACHE+tableName).getBytes(),
						SerializeUtil.serialize(key.toString()),
						SerializeUtil.serialize(value));
				jedis.expire((MYBATIS_REDIS_CACHE+tableName), 300);
				return null;
			}
		});

	}

	@Override
	public Object removeObject(final Object key)
	{
		return execute(new ShareJedisCallback<Object>()
		{
			@Override
			public Object doWithRedis(ShardedJedis jedis, String projecttrade)
			{
				return jedis.hdel((MYBATIS_REDIS_CACHE+tableName), key.toString());
			}
		});
	}
}
