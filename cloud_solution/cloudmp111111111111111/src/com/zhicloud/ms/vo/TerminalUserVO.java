/**
 * Project Name:ms
 * File Name:TerminalUserVO.java
 * Package Name:com.zhicloud.ms.vo
 * Date:Mar 17, 201510:50:46 PM
 * 
 *
 */

package com.zhicloud.ms.vo;

import com.zhicloud.ms.common.util.DateUtil;

import java.text.ParseException;
import java.util.Date;

/**
 * ClassName: TerminalUserVO 
 * Function: 定义TerminalUserVO对象. 
 * date: Mar 17, 2015 10:50:46 PM 
 *
 * @author sean
 * @version 
 * @since JDK 1.7
 */
public class TerminalUserVO {

	private String id;
	private String username;
	private String name;
	private String password;
	private String groupId;
	private String groupName;
	private String email;
	private String phone;
	private Integer usbStatus;
	private Integer status;
	private Integer cloudHostAmount;
	private String createTime;
	private String modifiedTime;
	private Date createTimeDate;
	private String alias;
	private String boxId;
    private String region;
    private String industry;
	
    /* 字段翻译 */
    private String status_name;
    private String usbstatus_name;
	
	/**
	 * username.
	 *
	 * @return  the username
	 * @since   JDK 1.7
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * username.
	 *
	 * @param   username    the username to set
	 * @since   JDK 1.7
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * password.
	 *
	 * @return  the password
	 * @since   JDK 1.7
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * password.
	 *
	 * @param   password    the password to set
	 * @since   JDK 1.7
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * groupId.
	 *
	 * @return  the groupId
	 * @since   JDK 1.7
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * groupId.
	 *
	 * @param   groupId    the groupId to set
	 * @since   JDK 1.7
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	/**
	 * groupName.
	 *
	 * @return  the groupName
	 * @since   JDK 1.7
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * groupName.
	 *
	 * @param   groupName    the groupName to set
	 * @since   JDK 1.7
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * id.
	 *
	 * @return  the id
	 * @since   JDK 1.7
	 */
	public String getId() {
		return id;
	}
	/**
	 * id.
	 *
	 * @param   id    the id to set
	 * @since   JDK 1.7
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * name.
	 *
	 * @return  the name
	 * @since   JDK 1.7
	 */
	public String getName() {
		return name;
	}
	/**
	 * name.
	 *
	 * @param   name    the name to set
	 * @since   JDK 1.7
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * email.
	 *
	 * @return  the email
	 * @since   JDK 1.7
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * email.
	 *
	 * @param   email    the email to set
	 * @since   JDK 1.7
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * phone.
	 *
	 * @return  the phone
	 * @since   JDK 1.7
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * phone.
	 *
	 * @param   phone    the phone to set
	 * @since   JDK 1.7
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * usbStatus.
	 *
	 * @return  the usbStatus
	 * @since   JDK 1.7
	 */
	public Integer getUsbStatus() {
		return usbStatus;
	}
	/**
	 * usbStatus.
	 *
	 * @param   usbStatus    the usbStatus to set
	 * @since   JDK 1.7
	 */
	public void setUsbStatus(Integer usbStatus) {
		this.usbStatus = usbStatus;
	}
	/**
	 * status.
	 *
	 * @return  the status
	 * @since   JDK 1.7
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * status.
	 *
	 * @param   status    the status to set
	 * @since   JDK 1.7
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * createTime.
	 *
	 * @return  the createTime
	 * @since   JDK 1.7
	 */
	public String getCreateTime() {
		return createTime;
	}
	/**
	 * createTime.
	 *
	 * @param   createTime    the createTime to set
	 * @since   JDK 1.7
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
		if(this.createTime!=null){
			try {
				createTimeDate = DateUtil.stringToDate(this.createTime, "yyyyMMddHHmmssSSS");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * modifiedTime.
	 *
	 * @return  the modifiedTime
	 * @since   JDK 1.7
	 */
	public String getModifiedTime() {
		return modifiedTime;
	}
	/**
	 * modifiedTime.
	 *
	 * @param   modifiedTime    the modifiedTime to set
	 * @since   JDK 1.7
	 */
	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	/**
	 * cloudHostAmount.
	 *
	 * @return  the cloudHostAmount
	 * @since   JDK 1.7
	 */
	public Integer getCloudHostAmount() {
		return cloudHostAmount;
	}
	/**
	 * cloudHostAmount.
	 *
	 * @param   cloudHostAmount    the cloudHostAmount to set
	 * @since   JDK 1.7
	 */
	public void setCloudHostAmount(Integer cloudHostAmount) {
		this.cloudHostAmount = cloudHostAmount;
	}
	public Date getCreateTimeDate() {
		return createTimeDate;
	}
	public void setCreateTimeDate(Date createTimeDate) {
		this.createTimeDate = createTimeDate;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getBoxId() {
		return boxId;
	}
	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    /**
     * @Description:导出需要，增加翻译的get方法
     * @return String
     */
    public String getStatus_name() {
        if (status == 1) {
            status_name = "禁用";
        } else {
            status_name = "正常";
        }
        return status_name;
    }

    /**
     * @Description:导出需要，增加翻译的get方法
     * @return String
     */
    public String getUsbstatus_name() {
        if (usbStatus == 1) {
            usbstatus_name = "开启";
        } else {
            usbstatus_name = "未开启";
        }
        return usbstatus_name;
    }
	
}

