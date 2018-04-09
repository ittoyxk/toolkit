package com.xk.exception;


/**
 *
 * 
 * @author xiaokang
 * @date 2016年7月4日 上午8:54:21
 *
 */
public class PropertyRuntimeException extends HeraRuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5692368185085690433L;

	// 包含ExceptionCode的构造函数
	public PropertyRuntimeException(ExceptionCode code)
	{
		super(code);
	}

	public PropertyRuntimeException(ExceptionCode code, Object[] codeArgs)
	{
		super(code,codeArgs);
	}

	public PropertyRuntimeException(ExceptionCode code, Throwable cause)
	{
		super(code,cause);
	}

	public PropertyRuntimeException(ExceptionCode code, Throwable cause, Object[] codeArgs)
	{
		super(code,cause,codeArgs);
	}
}