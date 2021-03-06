package com.zhicloud.op.httpGateway;
 
import com.zhicloud.op.app.helper.RegionHelper;
import com.zhicloud.op.app.helper.RegionHelper.RegionData;
import com.zhicloud.op.app.pool.CloudHostData;
import com.zhicloud.op.app.pool.CloudHostPoolManager;
import com.zhicloud.op.app.pool.diskImagePool.DiskImageProgressData;
import com.zhicloud.op.app.pool.diskImagePool.DiskImageProgressPool;
import com.zhicloud.op.app.pool.diskImagePool.DiskImageProgressPoolManager;
import com.zhicloud.op.app.pool.host.back.HostBackupProgressData;
import com.zhicloud.op.app.pool.host.back.HostBackupProgressPool;
import com.zhicloud.op.app.pool.host.back.HostBackupProgressPoolManager;
import com.zhicloud.op.app.pool.host.reset.HostResetProgressData;
import com.zhicloud.op.app.pool.host.reset.HostResetProgressPool;
import com.zhicloud.op.app.pool.host.reset.HostResetProgressPoolManager;
import com.zhicloud.op.app.pool.hostMonitorInfoPool.HostMonitorInfo;
import com.zhicloud.op.app.pool.hostMonitorInfoPool.HostMonitorInfoManager;
import com.zhicloud.op.app.pool.isoImagePool.IsoImageProgressData;
import com.zhicloud.op.app.pool.isoImagePool.IsoImageProgressPool;
import com.zhicloud.op.app.pool.isoImagePool.IsoImageProgressPoolManager;
import com.zhicloud.op.app.pool.network.*;
import com.zhicloud.op.app.pool.network.NetworkInfoExt.Host;
import com.zhicloud.op.app.pool.network.NetworkInfoExt.Port;
import com.zhicloud.op.app.pool.rule.RuleInfo;
import com.zhicloud.op.app.pool.rule.RuleInfoPool;
import com.zhicloud.op.app.pool.rule.RulePoolManager;
import com.zhicloud.op.common.util.CapacityUtil;
import com.zhicloud.op.common.util.json.JSONLibUtil;
import com.zhicloud.op.service.constant.AppConstant;
import com.zhicloud.op.service.constant.AppInconstant;
import com.zhicloud.op.service.constant.MonitorConstant;
import com.zhicloud.op.vo.PlatformResourceMonitorVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpGatewayAsyncMessageHandlerImpl {

	private final static Logger logger = Logger.getLogger(HttpGatewayAsyncMessageHandlerImpl.class);
	

	@HttpGatewayMessageHandler(messageType = "host_monitor_data")
	public Map<String, String> hostMonitorData(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process host monitor data.");
		
		JSONArray hostList = messageData.getJSONArray("host_list");
		
		// 新起线程处理数据2015-9-1 
		CloudHostPoolManager.getSingleton().puHostList(hostList);
		// 原来的方法，CloudHostPoolManager.getSingleton().updateMonitorDataThreadly(hostList);
		
		// 处理返回值,返回http_gateway格式{"statue":0/1}
		int task = messageData.getInt("task");
		String sessionId = channel.getSessionId();

		boolean result = false;
		HostMonitorInfo[] hostMonitorInfoList = HostMonitorInfoManager.singleton().getAllHostMonitorInfo();
		for (HostMonitorInfo hostMonitorInfo : hostMonitorInfoList) {// 遍历所有HostMonitorInfo,判断sessionid和task是否都对应。
			if (sessionId != null && sessionId.equalsIgnoreCase(hostMonitorInfo.getSessionId()) && task == hostMonitorInfo.getTask()) {// 吻合，则更新时间和设置标志位result
				hostMonitorInfo.updateTime();
				result = true;
				break;
			}
		}
		// sessionid和task对应上，则认为该监控可继续进行，否则，则认为是不可再继续的。
		Map<String, String> response = new HashMap<String, String>();
		if (result) {
			response.put("status", "1");
		} else {
			response.put("status", "0");
		}

		return response;
	}

	@HttpGatewayMessageHandler(messageType = "disk_image_create_progress")
	public void diskImageCreateProgress(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process disk image create progress data.");
		// 获取数据
		String name = messageData.getString("name");
		int progress = messageData.getInt("progress");
		String sessionId = channel.getSessionId();
		// 获取对象
		DiskImageProgressPool pool = DiskImageProgressPoolManager.singleton().getPool();
		DiskImageProgressData diskImage = pool.get(sessionId, name);
		// 对象不存在
		if (diskImage == null) {
			diskImage = new DiskImageProgressData();
			diskImage.setSessionId(sessionId);
			diskImage.setName(name);

			pool.put(diskImage);
		}
		// 更新
		diskImage.setProgress(progress);
		diskImage.setFinished(false);
		diskImage.updateTime();
	}

	@HttpGatewayMessageHandler(messageType = "disk_image_create_result")
	public void diskImageCreateResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process disk image create result data.");

		String sessionId = channel.getSessionId();
		// 获取数据
		String name = messageData.getString("name");
		String message = messageData.getString("message");

		// 获取对象
		DiskImageProgressPool pool = DiskImageProgressPoolManager.singleton().getPool();
		DiskImageProgressData diskImage = pool.get(sessionId, name);
		// 对象不存在
		if (diskImage == null) {
			diskImage = new DiskImageProgressData();
			diskImage.setSessionId(sessionId);
			diskImage.setName(name);

			pool.put(diskImage);
		}
		// 更新
		diskImage.setFinished(true);
		diskImage.setMessage(message);
		diskImage.updateTime();
		if (HttpGatewayResponseHelper.isSuccess(messageData) == false) {
			diskImage.setSuccess(false);
		} else {
			diskImage.setSuccess(true);
			String uuid = messageData.getString("uuid");
			long size = messageData.getLong("size");

			diskImage.setRealImageId(uuid);
			diskImage.setSize(size);
		}
		// 释放资源
		channel.release();
	}

	@HttpGatewayMessageHandler(messageType = "iso_image_upload_progress")
	public void isoImageUploadProgress(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process iso image upload progress data.");
		// 获取数据
		String name = messageData.getString("name");
		int progress = messageData.getInt("progress");
		String sessionId = channel.getSessionId();
		// 获取对象
		IsoImageProgressPool pool = IsoImageProgressPoolManager.singleton().getPool();
		IsoImageProgressData isoImage = pool.get(sessionId, name);
		// 对象不存在
		if (isoImage == null) {
			isoImage = new IsoImageProgressData();
			isoImage.setSessionId(sessionId);
			isoImage.setName(name);

			pool.put(isoImage);
		}
		// 更新
		isoImage.setProgress(progress);
		isoImage.setFinished(false);
		isoImage.updateTime();
	}

	@HttpGatewayMessageHandler(messageType = "iso_image_upload_result")
	public void isoImageUploadResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process iso image upload result data.");

		String sessionId = channel.getSessionId();
		// 获取数据
		String name = messageData.getString("name");
		String message = messageData.getString("message");

		// 获取对象
		IsoImageProgressPool pool = IsoImageProgressPoolManager.singleton().getPool();
		IsoImageProgressData isoImage = pool.get(sessionId, name);
		// 对象不存在
		if (isoImage == null) {
			isoImage = new IsoImageProgressData();
			isoImage.setSessionId(sessionId);
			isoImage.setName(name);

			pool.put(isoImage);
		}

		// 更新
		isoImage.setFinished(true);
		isoImage.setMessage(message);
		isoImage.updateTime();
		if (HttpGatewayResponseHelper.isSuccess(messageData) == false) {
			isoImage.setSuccess(false);
		} else {
			isoImage.setSuccess(true);
			String uuid = messageData.getString("uuid");
			String ip = messageData.getString("ip");
			String port = messageData.getString("port");
			BigInteger size = JSONLibUtil.getBigInteger(messageData, "size");

			isoImage.setRealImageId(uuid);
			isoImage.setIp(ip);
			isoImage.setPort(port);
			isoImage.setSize(size);
		}
		// 释放资源
		channel.release();
	}

	// 创建vpc
	@HttpGatewayMessageHandler(messageType = "network_create_result")
	public void networkCreateResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process network create result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();
		// 获取数据
		String name = messageData.getString("name");

		// 获取对象
		NetworkCreateInfoPool pool = NetworkPoolManager.singleton().getCreateInfoPool();
		NetworkInfoExt network = pool.get(regionId, name);
		// 对象不存在
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setSessionId(sessionId);
			network.setName(name);

			network.initAsyncStatus();
			network.setMessage("");
			network.updateTime();

			pool.put(network);
		}

		// 更新
		network.updateTime();
		if (HttpGatewayResponseHelper.isSuccess(messageData) == false) {
			String message = messageData.getString("message");

			network.fail();
			network.setMessage(message);
		} else {
			String uuid = messageData.getString("uuid");
			String networkAddress = messageData.getString("network_address");
			String message = messageData.getString("message");

			network.success();
			network.setMessage(message);
			network.setUuid(uuid);
			network.setNetworkAddress(networkAddress);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 查询vpc
	@HttpGatewayMessageHandler(messageType = "network_query_result")
	public void networkQueryResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process network query result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			// 获取对象
			NetworkInfoPool pool = NetworkPoolManager.singleton().getInfoPool();

			JSONArray networkList = messageData.getJSONArray("networks");
			for (int index = 0; index < networkList.size(); index++) {
				JSONObject network = networkList.getJSONObject(index);
				String uuid = network.getString("uuid");
				String name = network.getString("name");

				NetworkInfoExt localNetwork = pool.get(uuid);
				if (localNetwork == null) {
					localNetwork = new NetworkInfoExt();
					localNetwork.setRegionId(regionId);
					localNetwork.setSessionId(sessionId);
				}

				localNetwork.setName(name);
				localNetwork.setUuid(uuid);
				localNetwork.updateTime();

				pool.put(localNetwork);
			}
		}
	}

	// 修改vpc
	@HttpGatewayMessageHandler(messageType = "network_modify_result")
	public void networkModifyResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process network modify result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getModifyPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.setMessage(message);
			network.updateTime();
		} else {
			network.fail();
			network.setMessage(message);
			network.updateTime();
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 查询vpc详情
	@HttpGatewayMessageHandler(messageType = "network_detail_result")
	public void networkDetailResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process query network detail result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getInfoPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setUuid(uuid);
			network.setRegionId(regionId);
			network.setSessionId(sessionId);
			network.initAsyncStatus();
			network.updateTime();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String name = messageData.getString("name");
			int size = messageData.getInt("size");
			String networkAddress = messageData.getString("network_address");
			int netmask = messageData.getInt("netmask");
			String description = messageData.getString("description");
			int status = messageData.getInt("network_status");
			String[] ip = JSONLibUtil.getStringArray(messageData, "ip");

			network.setName(name);
			network.setSize(size);
			network.setNetworkAddress(networkAddress);
			network.setNetmask(netmask);
			network.setDescription(description);
			network.setStatus(status);
			network.setIp(ip);

			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 启动vpc
	@HttpGatewayMessageHandler(messageType = "start_network_result")
	public void networkStartResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process start network result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getStartPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.setMessage(message);
			network.updateTime();
		} else {
			network.fail();
			network.setMessage(message);
			network.updateTime();
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 停止vpc
	@HttpGatewayMessageHandler(messageType = "stop_network_result")
	public void networkStopResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process stop network result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getStopPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.setMessage(message);
			network.updateTime();
		} else {
			network.fail();
			network.setMessage(message);
			network.updateTime();
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 删除vpc
	@HttpGatewayMessageHandler(messageType = "delete_network_result")
	public void networkDeleteResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process delete network result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getDelPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.setMessage(message);
			network.updateTime();
		} else {
			network.fail();
			network.setMessage(message);
			network.updateTime();
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// 查询vpc关联云主机
	@HttpGatewayMessageHandler(messageType = "query_network_host_result")
	public void networkQueryHostResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process query network host result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String uuid = messageData.getString("uuid");
		String message = HttpGatewayResponseHelper.getMessage(messageData);

		NetworkInfoPool pool = NetworkPoolManager.singleton().getQueryHostPool();
		NetworkInfoExt network = pool.get(uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();
			network.setHostList(new Host[0]);

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray hostList = messageData.getJSONArray("hosts");
			Host[] hostArray = new Host[hostList.size()];

			for (int i = 0; i < hostList.size(); i++) {
				JSONObject host = hostList.getJSONObject(i);
				String hostUuid = host.getString("uuid");
				String hostName = host.getString("name");
				String hostNetwokAddress = host.getString("network_address");

				Host attachHost = network.new Host();
				attachHost.setName(hostName);
				attachHost.setUuid(hostUuid);
				attachHost.setNetworkAddress(hostNetwokAddress);
				hostArray[i] = attachHost;
			}

			network.success();
			network.setHostList(hostArray);
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.setMessage(message);
			network.updateTime();
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc关联云主机
	@HttpGatewayMessageHandler(messageType = "attach_host_result")
	public void networkAttachHostResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process attach network host result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");
		String hostUuid = messageData.getString("host");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getAttachHostPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();
			network.setHostUuid(hostUuid);

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.updateTime();
			network.setMessage(message);
			network.setHostNetworkAddress(messageData.getString("network_address"));
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc移除云主机
	@HttpGatewayMessageHandler(messageType = "detach_host_result")
	public void networkDetachHostResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process detach network host result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");
		String hostUuid = messageData.getString("host");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getDetachHostPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();
			network.setHostUuid(hostUuid);

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc申请公网ip
	@HttpGatewayMessageHandler(messageType = "attach_address_result")
	public void networkAttachAddressResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process attach network address result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getAttachAddressPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String[] ip = JSONLibUtil.getStringArray(messageData, "ip");
			network.setIp(ip);

			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc移除公网ip
	@HttpGatewayMessageHandler(messageType = "detach_address_result")
	public void networkDetachAddressResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process detach network address result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getDetachAddressPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String[] successIpList = JSONLibUtil.getStringArray(messageData, "success_ip_list");
			String[] failIpList = JSONLibUtil.getStringArray(messageData, "fail_ip_list");
			network.setSuccessIpList(successIpList);

			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc绑定端口
	@HttpGatewayMessageHandler(messageType = "network_bind_port_result")
	public void networkBindPortResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process bind network port result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getBindPortPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray portInfoList = messageData.getJSONArray("port");
			Port[] successPortList = new Port[portInfoList.size()];

			for (int i = 0; i < portInfoList.size(); i++) {
				JSONObject portInfo = portInfoList.getJSONObject(i);
				String protocol = portInfo.getString("protocol");
				String publicIp = portInfo.getString("public_ip");
				String publicPort = portInfo.getString("public_port");
				String hostUuid = portInfo.getString("host_uuid");
				String hostPort = portInfo.getString("host_port");

				Port port = network.new Port();
				port.setProtocol(protocol);
				port.setPublicIp(publicIp);
				port.setPublicPort(publicPort);
				port.setHostUuid(hostUuid);
				port.setHostPort(hostPort);

				successPortList[i] = port;
			}

			network.setSuccessPortList(successPortList);
			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}

	// vpc解除绑定端口
	@HttpGatewayMessageHandler(messageType = "network_unbind_port_result")
	public void networkUnbindPortResult(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process unbind network port result data.");

		String sessionId = channel.getSessionId();
		Integer regionId = channel.getRegion();
		// 释放资源
		channel.release();

		String message = HttpGatewayResponseHelper.getMessage(messageData);
		String uuid = messageData.getString("uuid");

		NetworkInfoSessionPool pool = NetworkPoolManager.singleton().getUnbindPortPool();
		NetworkInfoExt network = pool.get(sessionId, uuid);
		if (network == null) {
			network = new NetworkInfoExt();
			network.setRegionId(regionId);
			network.setUuid(uuid);
			network.setSessionId(sessionId);
			network.updateTime();
			network.setMessage("");
			network.initAsyncStatus();

			pool.put(network);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray portInfoList = messageData.getJSONArray("port");
			Port[] successPortList = new Port[portInfoList.size()];

			for (int i = 0; i < portInfoList.size(); i++) {
				JSONObject portInfo = portInfoList.getJSONObject(i);
				String protocol = portInfo.getString("protocol");
				String publicIp = portInfo.getString("public_ip");
				String publicPort = portInfo.getString("public_port");
				String hostUuid = portInfo.getString("host_uuid");
				String hostPort = portInfo.getString("host_port");

				Port port = network.new Port();
				port.setProtocol(protocol);
				port.setPublicIp(publicIp);
				port.setPublicPort(publicPort);
				port.setHostUuid(hostUuid);
				port.setHostPort(hostPort);

				successPortList[i] = port;
			}

			network.setSuccessPortList(successPortList);
			network.success();
			network.updateTime();
			network.setMessage(message);
		} else {
			network.fail();
			network.updateTime();
			network.setMessage(message);
		}
		synchronized (network) {
			network.notifyAll();
		}
	}
	
	@HttpGatewayMessageHandler(messageType = "system_monitor_data")
	public Map<String, String> systemMonitorData(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process system monitor data.");
		PlatformResourceMonitorVO resource = new PlatformResourceMonitorVO();
		DecimalFormat    df   = new DecimalFormat("######0.00");   
		int task = messageData.getInt("task");//监控任务id
		JSONArray server = messageData.getJSONArray("server");//宿主机数量[停止,告警,故障,正常]
		JSONArray host = messageData.getJSONArray("host");//云主机数量[停止,告警,故障,正常]
		int cpuCount = messageData.getInt("cpu_count");//cpu总核心数
		double cpuUsage = messageData.getDouble("cpu_usage");//cpu利用率
		JSONArray memory = messageData.getJSONArray("memory");//内存空间，单位：字节，[可用,总量]
		double memoryUsage = messageData.getDouble("memory_usage");//内存利用率
		JSONArray diskVolume = messageData.getJSONArray("disk_volume");//磁盘空间，单位：字节，[可用,总量]
		double diskUsage = messageData.getDouble("disk_usage");//磁盘利用率
		JSONArray diskIO = messageData.getJSONArray("disk_io");//磁盘io，[读请求,读字节,写请求,写字节,IO错误次数]
		JSONArray networkIO = messageData.getJSONArray("network_io");//网络io，[接收字节,接收包,接收错误,接收丢包,发送字节,发送包,发送错误,发送丢包]
		JSONArray speed = messageData.getJSONArray("speed");//速度，单位字节/s，[读速度,写速度,接收速度,发送速度]
		String timestamp = messageData.getString("timestamp");//时间戳，格式"YYYY-MM-DD HH:MI:SS"
		resource.setTask(task);
		resource.setServer(server);
		resource.setHost(host);
		resource.setCpuCount(cpuCount);
		resource.setCpuUsage(df.format(cpuUsage*100)+"%");
		for(int i = 0;i<memory.size();i++){
			memory.set(i, CapacityUtil.toGBValue(new BigInteger(memory.get(i).toString()),0)+"GB");
		}
		resource.setMemory(memory);
		resource.setMemoryUsage(df.format(memoryUsage*100)+"%");
		for(int i = 0;i<diskVolume.size();i++){
			diskVolume.set(i, CapacityUtil.toGBValue(new BigInteger(diskVolume.get(i).toString()),0)+"GB");
		}
		resource.setDiskVolume(diskVolume);
		resource.setDiskUsage(df.format(diskUsage*100)+"%");
		for(int i = 0;i<diskIO.size();i++){
			if(i == 1 || i == 3){				
				diskIO.set(i, CapacityUtil.toGBValue(new BigInteger(diskIO.get(i).toString()),0)+"GB");
			}
		}
 		resource.setDiskIO(diskIO);
 		for(int i = 0;i<networkIO.size();i++){
 			if(i == 0 || i == 4){			
 				networkIO.set(i, CapacityUtil.toGBValue(new BigInteger(networkIO.get(i).toString()),0)+"GB");
 			}
 		}
		resource.setNetworkIO(networkIO);
		for(int i = 0;i<speed.size();i++){
			speed.set(i, CapacityUtil.toKB(new BigInteger(speed.get(i).toString()),0));
		}
		resource.setSpeed(speed);
		resource.setTimestamp(timestamp);
		Integer region = AppInconstant.regionAndTaskRelation.get(task);
		resource.setRegion(region);
		AppInconstant.platformResourceMonitorData.put(region, resource);
		if((new Date().getTime() - AppInconstant.platformResourceMonitorDataLastGet.getTime())>300000){
			RegionData[] regionDatas = RegionHelper.singleton.getAllResions();
			for (RegionData regionData : regionDatas) {			
				try {
					HttpGatewayAsyncChannel newChannel = HttpGatewayManager.getAsyncChannel(regionData.getId());
					PlatformResourceMonitorVO vo = AppInconstant.platformResourceMonitorData.get(regionData.getId());
					channel.stopSystemMonitor(vo.getTask()); 
				} catch (MalformedURLException e1) {
					
					// TODO Auto-generated catch block
					e1.printStackTrace();
					
				} catch (IOException e1) {
					
					// TODO Auto-generated catch block
					e1.printStackTrace();
					
				} 
			}
			AppInconstant.regionAndTaskRelation.clear();
			AppInconstant.platformResourceMonitorData.clear();
		}
		System.err.println(String.format("task[%s], server[%s], host[%s], cpu_count[%s], cpu_usage[%s], memory[%s], memory_usage[%s], disk_volume[%s], disk_usage[%s], disk_io[%s], network_io[%s], speed[%s], timestamp[%s]",
				task, server, host, cpuCount, cpuUsage, memory, memoryUsage, diskVolume, diskUsage, diskIO, networkIO, speed, timestamp));

		Map<String, String> response = new HashMap<String, String>();
		response.put("status", "1");

		return response;
	}

	@HttpGatewayMessageHandler(messageType = "query_snapshot_pool")
	public void snapshotPoolQuery(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process query snapshot pool response.");
		channel.release();

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray snapshotPoolList = messageData.getJSONArray("snapshot_pool");
			for (int i = 0; i < snapshotPoolList.size(); i++) {
				JSONObject snapshotPool = snapshotPoolList.getJSONObject(i);
				String name = snapshotPool.getString("name");
				String uuid = snapshotPool.getString("uuid");
				JSONArray node = snapshotPool.getJSONArray("node");
				JSONArray snapshot = snapshotPool.getJSONArray("snapshot");
				int cpuCount = snapshotPool.getInt("cpu_count");
				double cpuUsage = snapshotPool.getDouble("cpu_usage");
				JSONArray memory = snapshotPool.getJSONArray("memory");
				double memoryUsage = snapshotPool.getDouble("memory_usage");
				JSONArray diskVolume = snapshotPool.getJSONArray("disk_volume");
				double diskUsage = snapshotPool.getDouble("disk_usage");
				int status = snapshotPool.getInt("status");

				System.err.println(String.format("name[%s], uuid[%s], node[%s], snapshot[%s], cpu_count[%s], cpu_usage[%s], memory[%s], memory_usage[%s], disk_volume[%s], disk_usage[%s], status[%s]",
						name, uuid, node, snapshot, cpuCount, cpuUsage, memory, memoryUsage, diskVolume, diskUsage, status));
			}
		}
	}

	@HttpGatewayMessageHandler(messageType = "create_snapshot_pool")
	public void snapshotPoolCreate(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process create snapshot pool response.");
		channel.release();
		
		String name = messageData.getString("name");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String uuid = messageData.getString("uuid");
			System.err.println(String.format("success to create snapshot pool. name[%s], uuid[%s]", name, uuid));
		} else {
			System.err.println(String.format("fail to create snapshot pool. name[%s], uuid[%s]", name));
		}
	}

	@HttpGatewayMessageHandler(messageType = "modify_snapshot_pool")
	public void snapshotPoolModify(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process modify snapshot pool response.");
		channel.release();
		
		String uuid = messageData.getString("uuid");
		System.err.println(String.format("modify snapshot pool. uuid[%s], success[%s]", uuid, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "delete_snapshot_pool")
	public void snapshotPoolDelete(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process delete snapshot pool response.");
		channel.release();
		
		String uuid = messageData.getString("uuid");
		System.err.println(String.format("delete snapshot pool. uuid[%s], success[%s]", uuid, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "add_snapshot_node")
	public void snapshotPoolAddNode(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process add snapshot node response.");
		channel.release();
		
		String pool = messageData.getString("pool");
		String nodeName = messageData.getString("name");
		System.err.println(String.format("add snapshot node. pool[%s], node_name[%s], success[%s]", pool, nodeName, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "remove_snapshot_node")
	public void snapshotPoolRemoveNode(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process remove snapshot node response.");
		channel.release();
		
		String pool = messageData.getString("pool");
		String nodeName = messageData.getString("name");
		System.err.println(String.format("remove snapshot node. pool[%s], node_name[%s], success[%s]", pool, nodeName, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "query_snapshot_node")
	public void snapshotPoolQueryNode(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process query snapshot node response.");
		channel.release();
		
		String pool = messageData.getString("pool");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			System.err.println(String.format("success to query snapshot node. pool[%s]", pool));
			JSONArray nodeList = messageData.getJSONArray("snapshot_node");
			for (int i = 0; i < nodeList.size(); i ++) {
				JSONObject node = nodeList.getJSONObject(i);
				String name = node.getString("name");
				int status = node.getInt("status");
				int cpuCount = node.getInt("cpu_count");
				double cpuUsage = node.getDouble("cpu_usage");
				JSONArray memory = node.getJSONArray("memory");
				double memoryUsage = node.getDouble("memory_usage");
				JSONArray diskVolume = node.getJSONArray("disk_volume");
				double diskUsage = node.getDouble("disk_usage");
				String ip = node.getString("ip");
				
				System.err.println(String.format("node: name[%s], status[%s], cpu_count[%s], cpu_usage[%s], memory[%s], memory_usage[%s], disk_volume[%s], disk_usage[%s], ip[%s]",
						name, status, cpuCount, cpuUsage, memory, memoryUsage, diskVolume, diskUsage, ip));		
			}
			
		} else {
			System.err.println(String.format("fail to query snapshot node. pool[%s]", pool));			
		}
	}

	@HttpGatewayMessageHandler(messageType = "query_snapshot")
	public void snapshotQuery(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process query snapshot response.");
		channel.release();
		
		String uuid = messageData.getString("uuid");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			System.err.println(String.format("success to query snapshot. uuid[%s]", uuid));
			JSONArray snapshotList = messageData.getJSONArray("snapshot");
			for (int i = 0; i < snapshotList.size(); i ++) {
				JSONObject snapshot = snapshotList.getJSONObject(i);
				String name = snapshot.getString("name");
				String snapshotUuid = snapshot.getString("uuid");
				String timestamp = snapshot.getString("timestamp");
				int status = snapshot.getInt("status");
				
				System.err.println(String.format("snapshot: name[%s], uuid[%s], timestamp[%s], status[%s]",
						name, uuid, timestamp, status));		
			}
			
		} else {
			System.err.println(String.format("fail to query snapshot. uuid[%s]", uuid));			
		}
	}

	@HttpGatewayMessageHandler(messageType = "create_snapshot")
	public void snapshotCreate(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process create snapshot response.");
		channel.release();
		
		String target = messageData.getString("target");
		String name = messageData.getString("name");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String uuid = messageData.getString("uuid");
			String timestamp = messageData.getString("timestamp");
			System.err.println(String.format("success to create snapshot. target[%s], name[%s], uuid[%s], timestamp[%s]", target, name, uuid, timestamp));		
		} else {
			System.err.println(String.format("fail to create snapshot. target[%s], name[%s]", target, name));			
		}
	}

	@HttpGatewayMessageHandler(messageType = "delete_snapshot")
	public void snapshotDelete(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process delete snapshot response.");
		channel.release();
		
		String uuid = messageData.getString("uuid");
		System.err.println(String.format("delete snapshot. uuid[%s], success[%s]", uuid, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "resume_snapshot")
	public void snapshotResume(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("start to process resume snapshot response.");
		channel.release();
		
		String uuid = messageData.getString("uuid");
		System.err.println(String.format("resume snapshot. uuid[%s], success[%s]", uuid, HttpGatewayResponseHelper.isSuccess(messageData)));
	}

	@HttpGatewayMessageHandler(messageType = "flush_disk_image_ack")
	public void flushDiskImageAck(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve flush disk image ack.");
		
		String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		int disk = messageData.getInt("disk");
		
		AppInconstant.hostResetProgress.put(uuid+"_reset", "reset_true");
		
		//获取对象
		HostResetProgressPool pool = HostResetProgressPoolManager.singleton().getPool();
		HostResetProgressData hostReset = pool.get(uuid);
		
		//对象不存在
		if(hostReset == null){
			hostReset = new HostResetProgressData();
            hostReset.setRealHostId(uuid);
			hostReset.setSessionId(sessionId);
			pool.put(hostReset);
		}
		
		hostReset.setReady(true);
		
		hostReset.updateTime();
		
		
		logger.info(String.format("start to flush disk image. uuid[%s], disk[%d]", uuid, disk));
	}

	@HttpGatewayMessageHandler(messageType = "flush_disk_image_progress")
	public void flushDiskImageProgress(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve flush disk image progress response.");
		
		String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		int disk = messageData.getInt("disk");
		int level = messageData.getInt("level");

        AppInconstant.hostResetProgress.put(uuid+"_reset", "reset_true");


        //获取对象
		HostResetProgressPool pool = HostResetProgressPoolManager.singleton().getPool();
		HostResetProgressData hostReset = pool.get(uuid);
				
		//对象不存在
		if(hostReset == null){
			hostReset = new HostResetProgressData();
            hostReset.setRealHostId(uuid);
			hostReset.setSessionId(sessionId);
			pool.put(hostReset);
		}
		
		hostReset.setProgress(level);
		hostReset.setFinished(false);
		hostReset.updateTime();
		
		logger.error(String.format("flush disk image at progress[%d]. uuid[%s], disk[%d]", level, uuid, disk));
	}

	@HttpGatewayMessageHandler(messageType = "flush_disk_image")
	public void flushDiskImage(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve flush disk image response.");
		
		//获取数据
		String sessionId = channel.getSessionId();
		String uuid = messageData.getString("uuid");
		int disk = messageData.getInt("disk");
						
		//获取对象
		HostResetProgressPool pool = HostResetProgressPoolManager.singleton().getPool();
		HostResetProgressData hostReset = pool.get(uuid);
				
		//对象不存在
		if(hostReset == null){
			hostReset = new HostResetProgressData();
            hostReset.setRealHostId(uuid);
            hostReset.setSessionId(sessionId);
			pool.put(hostReset);
		}
				
		hostReset.setFinished(true);
		hostReset.updateTime();
				
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			hostReset.setSuccess(true);
			logger.info(String.format("flush disk image success. uuid[%s], disk[%d], success", uuid, disk));
		} else {
			hostReset.setSuccess(false);
			logger.error(String.format("[%s]flush disk image fail. uuid[%s]", sessionId, uuid));
		}
        AppInconstant.hostResetProgress.put(uuid+"_reset", "reset_false");
        channel.release();
		
	}

	@HttpGatewayMessageHandler(messageType = "backup_host_ack")
	public void backupHostAck(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve backup host ack.");

        String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		
		AppInconstant.hostBackupProgress.put(uuid+"_backup", "backup_true");
		
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
		
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
            hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.setReady(true);
		
		hostBackup.updateTime();


        logger.info(String.format("[%s]start to backup host. uuid[%s]", sessionId, uuid));
	}

	@HttpGatewayMessageHandler(messageType = "backup_host_progress")
	public void backupHostProgress(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve backup host progress notify.");

		String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		int level = messageData.getInt("level");

        AppInconstant.hostBackupProgress.put(uuid+"_backup", "backup_true");
		
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
				
				
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
            hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.setProgress(level);
		hostBackup.setFinished(false);
		hostBackup.updateTime();

        logger.info(String.format("[%s]backup host at progress %d%%. uuid[%s]", sessionId, level, uuid));
    }

    @HttpGatewayMessageHandler(messageType = "backup_host")
	public void backupHost(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve backup host progress response.");

        //获取数据
		String sessionId = channel.getSessionId();
		String uuid = messageData.getString("uuid");
				
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
				
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
			hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.setFinished(true);
        hostBackup.updateTime();

        if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String timestamp = messageData.getString("timestamp");
			hostBackup.setSuccess(true);
            logger.info(String.format("[%s]backup host success. uuid[%s], timestamp[%s]", sessionId, uuid, timestamp));
        } else {
			hostBackup.setSuccess(false);
            logger.error(String.format("[%s]backup host fail. uuid[%s]", sessionId, uuid));
        }
        AppInconstant.hostBackupProgress.put(uuid + "_backup", "backup_false");
		channel.release();
	}

	@HttpGatewayMessageHandler(messageType = "resume_host_ack")
	public void resumeHostAck(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve resume host ack.");

        String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		
		AppInconstant.hostBackupProgress.put(uuid+"_resume", "resume_true");
		
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
		
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
            hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.setReady(true);
		
		hostBackup.updateTime();

        logger.info(String.format("[%s]start to resume host. uuid[%s]", sessionId, uuid));
		
	}

	@HttpGatewayMessageHandler(messageType = "resume_host_progress")
	public void resumeHostProgress(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve resume host progress notify.");

        String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		int level = messageData.getInt("level");

        AppInconstant.hostBackupProgress.put(uuid+"_resume", "resume_true");

        //获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
				
				
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
            hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.setProgress(level);
        hostBackup.setFinished(false);
        hostBackup.updateTime();

        logger.info(String.format("[%s]resume host at progress %d%%. uuid[%s]", sessionId, level, uuid));


    }

	@HttpGatewayMessageHandler(messageType = "resume_host")
	public void resumeHost(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve resume host progress response.");

        //获取数据
		String sessionId = channel.getSessionId();
		String uuid = messageData.getString("uuid");
					
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
						
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
            hostBackup.setSessionId(sessionId);
            pool.put(hostBackup);
		}
				
		hostBackup.setFinished(true);
		hostBackup.updateTime();
				
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			hostBackup.setSuccess(true);
            logger.info(String.format("[%s]resume host success. uuid[%s]", sessionId, uuid));
        } else {
			hostBackup.setSuccess(false);
            logger.error(String.format("[%s]resume host fail. uuid[%s]", sessionId, uuid));
        }
        AppInconstant.hostBackupProgress.put(uuid+"_resume", "resume_false");

    }

	@HttpGatewayMessageHandler(messageType = "query_host_backup")
	public void queryHostBackup(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve query host backup response.");

      String sessionId = channel.getSessionId();
		
		//获取数据
		String uuid = messageData.getString("uuid");
		
		//获取对象
		HostBackupProgressPool pool = HostBackupProgressPoolManager.singleton().getPool();
		HostBackupProgressData hostBackup = pool.get(uuid);
		
		
		//对象不存在
		if(hostBackup == null){
			hostBackup = new HostBackupProgressData();
            hostBackup.setRealHostId(uuid);
			hostBackup.setSessionId(sessionId);
			pool.put(hostBackup);
		}
		
		hostBackup.updateTime();

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray index = messageData.getJSONArray("index");
			JSONArray diskVolume = messageData.getJSONArray("disk_volume");
			JSONArray timestamp = messageData.getJSONArray("timestamp");
      Long osStamp = Long.valueOf(timestamp.get(0).toString().replaceAll("[-\\s:]", ""));
      Long dataStamp = Long.valueOf(timestamp.get(1).toString().replaceAll("[-\\s:]",""));
        if(diskVolume.isEmpty()) {
				hostBackup.setAvaliable(false);
			} else {
				hostBackup.setAvaliable(true);
				if(osStamp > dataStamp){
            hostBackup.setMode(AppConstant.PART_BACKUP_MODE);
            hostBackup.setDisk(AppConstant.OS_BACKUP_DISK); //系统盘备份
            hostBackup.setTimestamp(timestamp.get(0).toString());
				}else if (osStamp < dataStamp){
            hostBackup.setMode(AppConstant.PART_BACKUP_MODE);
            hostBackup.setDisk(AppConstant.DATA_BACKUP_DISK); // 数据盘备份
					hostBackup.setTimestamp(timestamp.get(1).toString());
				} else {
            hostBackup.setMode(AppConstant.FULL_BACKUP_MODE); //全备份
            hostBackup.setTimestamp(timestamp.get(0).toString());
        }
			}
			System.err.println(String.format("[%s]query host backup success. uuid[%s], index[%s], disk_volume[%s], timestamp[%s]", sessionId, uuid, index, diskVolume, timestamp));
			logger.info(String.format("[%s]query host backup success. uuid[%s], disk_volume[%s], timestamp[%s]", sessionId, uuid, diskVolume, timestamp));
		} else {
        System.err.println((String.format("[%s]query host backup fail. uuid[%s]", sessionId, uuid)));
        hostBackup.setAvaliable(false);
        logger.error((String.format("[%s]query host backup fail. uuid[%s]", sessionId, uuid)));
		}
		channel.release();
	}

	@HttpGatewayMessageHandler(messageType = "add_rule")
	public void addRule(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve add rule response.");
		String session = channel.getSessionId();
		
		String target = messageData.getString("target");
		int mode = messageData.getInt("mode");
		JSONArray ip = messageData.getJSONArray("ip");
		JSONArray port = messageData.getJSONArray("port");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			System.err.println(String.format("[%s]add rule success. target[%s], mode[%d], ip[%s], port[%s]", session, target, mode, ip, port));
		} else {
			System.err.println(String.format("[%s]add rule fail. target[%s], mode[%d], ip[%s], port[%s]", session, target, mode, ip, port));			
		}
		String message = HttpGatewayResponseHelper.getMessage(messageData);
		 
		RuleInfoPool pool = RulePoolManager.singleton().getInfoPool();
		List<RuleInfo> ruleList = pool.get(session);
		if (ruleList == null) {
			ruleList = new ArrayList<RuleInfo>();
			RuleInfo rule = new RuleInfo();
  			rule.setSessionId(session);
 			rule.setMessage("");
			rule.initAsyncStatus();
			ruleList.add(rule);
			pool.put(ruleList);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
 			 
 			RuleInfo rule = ruleList.get(0); 
			rule.success();
			rule.updateTime();
			rule.setMessage(message); 
		} else {
			ruleList.get(0).fail();
			ruleList.get(0).updateTime();
			ruleList.get(0).setMessage(message);
		}
		synchronized (ruleList.get(0)) {
			ruleList.get(0).notifyAll();
		}
	}

	@HttpGatewayMessageHandler(messageType = "remove_rule")
	public void removeRule(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve remove rule response.");
		String session = channel.getSessionId();
		
		String target = messageData.getString("target");
		int mode = messageData.getInt("mode");
		JSONArray ip = messageData.getJSONArray("ip");
		JSONArray port = messageData.getJSONArray("port");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			System.err.println(String.format("[%s]remove rule success. target[%s], mode[%d], ip[%s], port[%s]", session, target, mode, ip, port));
		} else {
			System.err.println(String.format("[%s]remove rule fail. target[%s], mode[%d], ip[%s], port[%s]", session, target, mode, ip, port));			
		}
		String message = HttpGatewayResponseHelper.getMessage(messageData);
		 
		RuleInfoPool pool = RulePoolManager.singleton().getInfoPool();
		List<RuleInfo> ruleList = pool.get(session);
		if (ruleList == null) {
			ruleList = new ArrayList<RuleInfo>();
			RuleInfo rule = new RuleInfo();
  			rule.setSessionId(session);
 			rule.setMessage("");
			rule.initAsyncStatus();
			ruleList.add(rule);
			pool.put(ruleList);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
 			 
 			RuleInfo rule = ruleList.get(0); 
			rule.success();
			rule.updateTime();
			rule.setMessage(message); 
		} else {
			ruleList.get(0).fail();
			ruleList.get(0).updateTime();
			ruleList.get(0).setMessage(message);
		}
		synchronized (ruleList.get(0)) {
			ruleList.get(0).notifyAll();
		}
	}

	@HttpGatewayMessageHandler(messageType = "query_rule")
	public void queryRule(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		logger.debug("recieve query rule response.");
		String sessionId = channel.getSessionId();
		
		String target = messageData.getString("target");
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			JSONArray mode = messageData.getJSONArray("mode");
			JSONArray ip = messageData.getJSONArray("ip");
			JSONArray port = messageData.getJSONArray("port");
 			System.err.println(String.format("[%s]query rule success. target[%s], mode[%s], ip[%s], port[%s]", sessionId, target, mode, ip, port));
 
		} else {
			System.err.println(String.format("[%s]query rule fail. target[%s]", sessionId, target));			
		}
		
		String message = HttpGatewayResponseHelper.getMessage(messageData);
 
		RuleInfoPool pool = RulePoolManager.singleton().getInfoPool();
		List<RuleInfo> ruleList = pool.get(sessionId);
		if (ruleList == null) {
			ruleList = new ArrayList<RuleInfo>();
			RuleInfo rule = new RuleInfo();
  			rule.setSessionId(sessionId);
 			rule.setMessage("");
			rule.initAsyncStatus();
			ruleList.add(rule);
			pool.put(ruleList);
		}

		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
 			JSONArray modeObjectArr = messageData.getJSONArray("mode");
			String[] mode = new String[modeObjectArr.size()*2];
			for(int i=0;i<modeObjectArr.size();i++){ 
				RuleInfo rule = new RuleInfo();
				mode = JSONLibUtil.getStringArray(JSONArray.fromObject(modeObjectArr.get(i)));
				rule.setMode(mode);
				ruleList.add(rule);
 			}
			
			
			JSONArray ipObjectArr = messageData.getJSONArray("ip");
			String[] ip = new String[ipObjectArr.size()*2];
			for(int i=0;i<ipObjectArr.size();i++){ 
				RuleInfo rule = ruleList.get(i+1);
				ip = JSONLibUtil.getStringArray(JSONArray.fromObject(ipObjectArr.get(i)));
				rule.setIpList(ip);
  			}
			
			JSONArray portObjectArr = messageData.getJSONArray("port");
			BigInteger[] port = new BigInteger[portObjectArr.size()*2];
			for(int i=0;i<portObjectArr.size();i++){ 
				RuleInfo rule = ruleList.get(i+1);
				port = JSONLibUtil.getBigIntegerArray(JSONArray.fromObject(portObjectArr.get(i)));
				rule.setPortList(port);
  			}
 			RuleInfo rule = ruleList.get(0);
 			rule.setPortList(port);
			rule.setIpList(ip);
			rule.setMode(mode);

			rule.success();
			rule.updateTime();
			rule.setMessage(message); 
		} else {
			ruleList.get(0).fail();
			ruleList.get(0).updateTime();
			ruleList.get(0).setMessage(message);
		}
		synchronized (ruleList.get(0)) {
			ruleList.get(0).notifyAll();
		}
	}

	@HttpGatewayMessageHandler(messageType = "start_monitor")
	public void startMonitor(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve start monitor response.");
        //获取数据
		String sessionId = channel.getSessionId();
					
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			int task = messageData.getInt("task");
            System.err.println(String.format("[%s]start monitor success. task[%d]", sessionId, task));
        } else {
            System.err.println(String.format("[%s]start monitor fail.", sessionId));
            channel.release();
        }
    }
	
	@HttpGatewayMessageHandler(messageType = "monitor_data")
	public void receiveMonitorData(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve monitor data.");
        //获取数据
		String sessionId = channel.getSessionId();

		int task = messageData.getInt("task");
		int level = messageData.getInt("level");
//        System.err.println(String.format("[%s]receive monitor data success. task[%d], level[%d]", sessionId, task, level));
        try {
            MonitorConstant.saveUUIDAndTaskID(messageData);
			channel.monitorHeartbeat(task);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@HttpGatewayMessageHandler(messageType = "stop_monitor")
	public void stopMonitor(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve stop monitor response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
					
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
            System.err.println(String.format("[%s]stop monitor success.", sessionId));
        } else {
            System.err.println(String.format("[%s]stop monitor fail.", sessionId));
        }
    }

	@HttpGatewayMessageHandler(messageType = "modify_compute_pool")
	public void modifyComputePool(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve modify compute pool response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
					
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
            System.err.println(String.format("[%s]modify compute pool success.", sessionId));
        } else {
            System.err.println(String.format("[%s]modify compute pool fail, message '%s'", sessionId, HttpGatewayResponseHelper.getMessage(messageData)));
        }
    }

	@HttpGatewayMessageHandler(messageType = "query_compute_pool_detail")
	public void queryComputePoolDetail(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve query compute pool detail response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
		String uuid = messageData.getString("uuid");
					
		if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
			String name = messageData.getString("name");
			int networkType = messageData.getInt("network_type");
			String network = messageData.getString("network");
			int diskType = messageData.getInt("disk_type");
			String diskSource = messageData.getString("disk_source");
			JSONArray mode = messageData.getJSONArray("mode");
            System.err.println(String.format("[%s]query compute pool detail success, uuid '%s', name '%s', network_type '%d', network '%s', disk_type '%d', disk_source '%s', mode '%s'", sessionId, uuid, name, networkType, network, diskType, diskSource, mode));
        } else {
            System.err.println(String.format("[%s]query compute pool detail fail, uuid '%s', message '%s'", sessionId, uuid, HttpGatewayResponseHelper.getMessage(messageData)));
        }
    }

	@HttpGatewayMessageHandler(messageType = "reset_host")
	public void resetHost(HttpGatewayAsyncChannel channel, JSONObject messageData) {
	  //补充完整代码后，请删除控制台输出代码。
        logger.debug("recieve reset host response.");
        //获取数据
        String sessionId = channel.getSessionId();
        channel.release();
        
        String uuid = messageData.getString("uuid");
        int status = 1;          
        if (HttpGatewayResponseHelper.isSuccess(messageData) == true) {
            status = 2; //表示成功了
         }  
        CloudHostData myCloudHostData = CloudHostPoolManager.getCloudHostPool().getByRealHostId(uuid);
        CloudHostData newCloudHostData = myCloudHostData.clone();
        newCloudHostData.setLastOperStatus(status);
        CloudHostPoolManager.getCloudHostPool().put(newCloudHostData);
    }

	@HttpGatewayMessageHandler(messageType = "query_storage_device")
	public void queryStorageDevice(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve query storage device response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
        System.err.println(String.format("[%s]query storage device response, data '%s'", sessionId, messageData));
    }

	@HttpGatewayMessageHandler(messageType = "add_storage_device")
	public void addStorageDevice(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve add storage device response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
        System.err.println(String.format("[%s]add storage device response, data '%s'", sessionId, messageData));
    }

	@HttpGatewayMessageHandler(messageType = "remove_storage_device")
	public void removeStorageDevice(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve remove storage device response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
        System.err.println(String.format("[%s]remove storage device response, data '%s'", sessionId, messageData));
    }

	@HttpGatewayMessageHandler(messageType = "enable_storage_device")
	public void enableStorageDevice(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve enable storage device response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
        System.err.println(String.format("[%s]enable storage device response, data '%s'", sessionId, messageData));
    }

	@HttpGatewayMessageHandler(messageType = "disable_storage_device")
	public void disableStorageDevice(HttpGatewayAsyncChannel channel, JSONObject messageData) {
		//补充完整代码后，请删除控制台输出代码。
		logger.debug("recieve disable storage device response.");
        //获取数据
		String sessionId = channel.getSessionId();
		channel.release();
		
        System.err.println(String.format("[%s]disable storage device response, data '%s'", sessionId, messageData));
    }

}
