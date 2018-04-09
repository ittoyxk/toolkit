package com.xk.exception;


/**
 * 调用接口传递的参数异常，该异常日志里面不需要抛出exception
 * 
 * @author wanghl80
 * @date 2016年6月16日 上午7:35:31
 *
 */
public class HeraIllegalArgumentException extends HeraException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3245258783983511058L;


	public HeraIllegalArgumentException(ExceptionCode code)
	{
		super(code);
	}

	public HeraIllegalArgumentException(ExceptionCode code, Object[] codeArgs)
	{
		super(code,codeArgs);
	}

	public HeraIllegalArgumentException(ExceptionCode code, Throwable cause)
	{
		super(code,cause);
	}

	public HeraIllegalArgumentException(ExceptionCode code, Throwable cause, Object[] codeArgs)
	{
		super(code,cause,codeArgs);
	}
}
