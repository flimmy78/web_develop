package com.zhicloud.op.httpGateway.verifyMethod;

import java.io.UnsupportedEncodingException;

import com.zhicloud.op.common.util.MD5Util;
import com.zhicloud.op.exception.AppException;

/**
 * M1使用的是md5加密
 */
public class M1 implements VerifyMethod
{

	@Override
	public String digest(String challengeKey)
	{
		try
		{
			return MD5Util.digestToHex(challengeKey.getBytes("utf-8"));
		}
		catch( UnsupportedEncodingException e )
		{
//			throw new AppException(e);
			throw new AppException("失败");
		}
	}
	
}
