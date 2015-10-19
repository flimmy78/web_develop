package com.zhicloud.ms.mapper;

import com.zhicloud.ms.vo.SysTenant;
import com.zhicloud.ms.vo.SysTenantExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysTenantMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int countByExample(SysTenantExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int deleteByExample(SysTenantExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int insert(SysTenant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int insertSelective(SysTenant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    List<SysTenant> selectByExample(SysTenantExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    SysTenant selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int updateByExampleSelective(@Param("record") SysTenant record, @Param("example") SysTenantExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int updateByExample(@Param("record") SysTenant record, @Param("example") SysTenantExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int updateByPrimaryKeySelective(SysTenant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_tenant
     *
     * @mbggenerated Sun Sep 06 15:28:27 CST 2015
     */
    int updateByPrimaryKey(SysTenant record);
    /**
     * 
    * @Title: qureyTenantByUserId 
    * @Description: 根据用户id去查询其管理的租户 
    * @param @param useriD
    * @param @return      
    * @return List<SysTenant>     
    * @throws
     */
    public List<SysTenant> qureyTenantByUserId(String userId);
}