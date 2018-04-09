package com.xk.mybatis;

import com.xk.batch.PooledDisruptorBatcher;
import com.xk.batch.bean.EventPerformer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;

public class AsyncSqlSessionTemplate extends SqlSessionTemplate implements
		EventPerformer<MybatisParam>
{
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(AsyncSqlSessionTemplate.class);
	private PooledDisruptorBatcher pooledDisruptorBatcher;
	private static final String UPDATE = "update";
	private static final String INSERT = "insert";
	private static final String DELETE = "delete";
	public void setPooledDisruptorBatcher(
			PooledDisruptorBatcher pooledDisruptorBatcher)
	{
		this.pooledDisruptorBatcher = pooledDisruptorBatcher;
	}

	public AsyncSqlSessionTemplate(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	@Override
	public int update(String statement, Object parameter)
	{
		MybatisParam bean = new MybatisParam();
		bean.setMethodName(UPDATE);
		bean.setStatement(statement);
		bean.setParameter(parameter);
		boolean submit = pooledDisruptorBatcher.submit(bean, this);
		return submit?1:0;
	}

	@Override
	public int update(String statement)
	{
		return update(statement, null);
	}

	@Override
	public int insert(String statement, Object parameter)
	{
		MybatisParam bean = new MybatisParam();
		bean.setMethodName(INSERT);
		bean.setStatement(statement);
		bean.setParameter(parameter);
		boolean submit = pooledDisruptorBatcher.submit(bean, this);
		return submit?1:0;
	}

	@Override
	public int insert(String statement)
	{
		return insert(statement, null);
	}

	@Override
	public int delete(String statement, Object parameter)
	{
		MybatisParam bean = new MybatisParam();
		bean.setMethodName(DELETE);
		bean.setStatement(statement);
		bean.setParameter(parameter);
		boolean submit = pooledDisruptorBatcher.submit(bean, this);
		return submit?1:0;
	}

	@Override
	public int delete(String statement)
	{
		return delete(statement, null);
	}

	@Override
	public void handle(MybatisParam bean)
	{
		String methodName = bean.getMethodName();
		String statement = bean.getStatement();
		Object parameter = bean.getParameter();
		log.debug("异步更新开始执行，方法是："+methodName);
		if (methodName.equals(UPDATE))
		{
			if (parameter != null)
			{
				super.update(statement, parameter);
			} else
			{
				super.update(statement);
			}
			return;
		}
		if (methodName.equals(INSERT))
		{
			if (parameter != null)
			{
				super.insert(statement, parameter);
			} else
			{
				super.insert(statement);
			}
			return;
		}
		if (methodName.equals(DELETE))
		{
			if (parameter != null)
			{
				super.delete(statement, parameter);
			} else
			{
				super.delete(statement);
			}
			return;
		}
	}
}
