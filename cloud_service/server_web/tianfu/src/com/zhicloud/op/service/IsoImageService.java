package com.zhicloud.op.service;

import java.io.InputStream;

import com.zhicloud.op.app.pool.isoImagePool.IsoImageProgressData;

public interface IsoImageService {
	
	public String upload(Integer region, String fileName, InputStream fileStream, String name, String description, String group, String user);
	public IsoImageProgressData getProgressData(String sessionId, String name);

}
