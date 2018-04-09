package com.xk.exception;


/**
 * 业务异常，比如客户在同一保险期间重复购买产品、客户验证码输入错误，日志里面只需要记录错误，不需要抛出异常，错误信息需要返回前端
 * 
 * @author wanghl80
 *
 */

public class HeraIllegalBusinessException extends HeraException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7322532127211286132L;

	public HeraIllegalBusinessException(ExceptionCode code)
	{
		super(code);
	}

	public HeraIllegalBusinessException(ExceptionCode code, Object[] codeArgs)
	{
		super(code,codeArgs);
	}

	public HeraIllegalBusinessException(ExceptionCode code, Throwable cause)
	{
		super(code,cause);
	}

	public HeraIllegalBusinessException(ExceptionCode code, Throwable cause, Object[] codeArgs)
	{
		super(code,cause,codeArgs);
	}
}
