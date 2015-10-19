package com.zhicloud.ms.vo;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.zhicloud.ms.util.CapacityUtil;

public class SysTenant {
	
	public String getLogCmt(){
		return this.name+"("+this.getId()+")";
	}
	
	
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.id
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.name
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private String name;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.remark
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private String remark;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.cpu
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private Integer cpu;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.mem
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private BigInteger mem;
    
    private String memStr;
    
    private BigInteger bandwidth;
    private String bandwidthStr;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.disk
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private BigInteger disk;
    private String diskStr;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_tenant.status
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    private Integer status;
    
    private String useLevel;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.id
     *
     * @return the value of sys_tenant.id
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.id
     *
     * @param id the value for sys_tenant.id
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.name
     *
     * @return the value of sys_tenant.name
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.name
     *
     * @param name the value for sys_tenant.name
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.remark
     *
     * @return the value of sys_tenant.remark
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.remark
     *
     * @param remark the value for sys_tenant.remark
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.cpu
     *
     * @return the value of sys_tenant.cpu
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.cpu
     *
     * @param cpu the value for sys_tenant.cpu
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.mem
     *
     * @return the value of sys_tenant.mem
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public BigInteger getMem() {

        return mem;
    }
    

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.mem
     *
     * @param mem the value for sys_tenant.mem
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setMem(BigInteger mem) {
        this.mem = mem;
        this.memStr =  CapacityUtil.toGBValue(mem,0).toString();

    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.disk
     *
     * @return the value of sys_tenant.disk
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public BigInteger getDisk() {
        return disk;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.disk
     *
     * @param disk the value for sys_tenant.disk
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setDisk(BigInteger disk) {
        this.disk = disk;
        this.diskStr =  CapacityUtil.toTB(disk,0).toString().replace("TB", "");

    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_tenant.status
     *
     * @return the value of sys_tenant.status
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_tenant.status
     *
     * @param status the value for sys_tenant.status
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMemStr() {
        return memStr;
    }

    public void setMemStr(String memStr) {
        this.memStr = memStr;
    }

    public BigInteger getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(BigInteger bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getBandwidthStr() {
        return bandwidthStr;
    }

    public void setBandwidthStr(String bandwidthStr) {
        this.bandwidthStr = bandwidthStr;
    }

    public String getDiskStr() {
        return diskStr;
    }

    public void setDiskStr(String diskStr) {
        this.diskStr = diskStr;
    }

    public String getUseLevel() {
        return useLevel;
    }

    public void setUseLevel(String useLevel) {
        this.useLevel = useLevel;
    }
    //导出需要====================特殊处理下//
    public String getCpu_name() {
        return cpu + "核";
    }

    public String getMemStr_name() {
        return memStr + "GB";
    }

    public String getDiskStr_name() {
        return diskStr + "TB";
    }
    
}