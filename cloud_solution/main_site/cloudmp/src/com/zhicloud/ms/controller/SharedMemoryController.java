package com.zhicloud.ms.controller;

import com.zhicloud.ms.app.cache.address.AddressCache;
import com.zhicloud.ms.app.cache.address.AddressCacheManager;
import com.zhicloud.ms.app.cache.address.AddressVO;
import com.zhicloud.ms.common.util.StringUtil;
import com.zhicloud.ms.remote.MethodResult;
import com.zhicloud.ms.service.IOperLogService;
import com.zhicloud.ms.service.SharedMemoryService;
import com.zhicloud.ms.transform.constant.TransFormPrivilegeConstant;
import com.zhicloud.ms.transform.constant.TransformConstant;
import com.zhicloud.ms.transform.controller.TransFormBaseAction;
import com.zhicloud.ms.transform.util.TransFormLoginHelper;
import com.zhicloud.ms.transform.util.TransFormLoginInfo;
import com.zhicloud.ms.transform.util.TransFormPrivilegeUtil;
import com.zhicloud.ms.vo.SharedMemoryVO;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @ClassName: SharedMemoryController
 * @Description: 共享存储管理控制器
 * @author 张本缘 于 2015年10月10日 上午10:45:48
 */
@Controller
@RequestMapping("/storageresourcepool/sharedmemory")
public class SharedMemoryController extends TransFormBaseAction {

    public static final Logger logger = Logger.getLogger(SharedMemoryController.class);

    @Resource
    private SharedMemoryService sharedMemoryService;

    @Resource
    private IOperLogService operLogService;

    /**
     * @Description:管理首页
     * @param request
     * @return String
     */
    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String managePage(HttpServletRequest request) {
        logger.debug("SharedMemoryController.managePage()");
        boolean flag = isHasPrivilege(request, TransFormPrivilegeConstant.shared_memory_query);
        if (!flag) {
            return TransformConstant.transform_jsp_noaccsess;
        }

        List<SharedMemoryVO> lists = sharedMemoryService.queryInfo(null);
        request.setAttribute("datas", lists);

        // 初始化配置数据
        sharedMemoryService.readFromFile();
        return "/transform/sharedmemory/shared_memory_manage";
    }

    /**
     * @Description:编辑共享存储信息界面跳转
     * @param request
     * @param id
     * @throws Exception
     */
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable String id, @PathVariable String type, HttpServletRequest request) {
        logger.debug("SharedMemoryController.edit()");
        boolean flag = false;
        if ("add".equals(type)) {
            flag = isHasPrivilege(request, TransFormPrivilegeConstant.shared_memory_add);
        } else {
            flag = isHasPrivilege(request, TransFormPrivilegeConstant.shared_memory_update);

        }
        if (!flag) {
            return TransformConstant.transform_jsp_noaccsess;
        }


        SharedMemoryVO sharedmeomory = sharedMemoryService.getVO(id);
        request.setAttribute("sharedmeomory", sharedmeomory);
        request.setAttribute("type", type);
        return "/transform/sharedmemory/shared_memory_edit";
    }

    /**
     * @Description:保存共享存储信息界面跳转
     * @param request
     * @param data
     * @throws Exception
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public MethodResult save(@RequestParam(value = "data", defaultValue = "") String data, HttpServletRequest request) {
        logger.debug("SharedMemoryController.save()");
        JSONObject json = JSONObject.fromObject(data);
        if (json != null && !json.isEmpty()) {
            try {
                String type = json.getString("type");
                String name = json.getString("name");
                String url = json.getString("url");
                TransFormLoginInfo login = TransFormLoginHelper.getLoginInfo(request);
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("name", name);
                map.put("url", url);
                map.put("username", json.getString("username"));
                map.put("password", json.getString("password"));
                map.put("secretkey", json.getString("secretkey"));
                map.put("type", type);
                if ("add".equals(type)) {
                    map.put("insert_user", login.getBillid());
                    map.put("insert_date", StringUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    map.put("id", StringUtil.generateUUID());
                    if (!sharedMemoryService.validateName(map)) {
                        sharedMemoryService.addInfo(map);
                        operLogService.addLog("共享存储信息", "新增共享存储信息成功", "1", "1", request);
                    } else {
                        return new MethodResult(MethodResult.FAIL, "路径名称重复");
                    }
                } else {
                    map.put("id", json.getString("id"));
                    if (!sharedMemoryService.validateName(map)) {
                        sharedMemoryService.updateInfo(map);

                        // 写入配置文件
                        AddressCache addressCache = AddressCacheManager.singleton().getCache();
                        addressCache.put(new AddressVO(name, url, "ntfs", "defaults", 0, 0));
                        List<AddressVO> addressVOs = Arrays.asList(addressCache.getAll());
                        sharedMemoryService.writeToFile(addressVOs);

                        operLogService.addLog("共享存储信息", "修改共享存储信息成功", "1", "1", request);
                    } else {
                        return new MethodResult(MethodResult.FAIL, "路径名称重复");
                    }
                }

                return new MethodResult(MethodResult.SUCCESS, "保存成功");
            } catch (Exception e) {
                e.printStackTrace();
                operLogService.addLog("共享存储信息", "保存共享存储信息失败", "1", "2", request);
                return new MethodResult(MethodResult.FAIL, "保存失败,请联系管理员");
            }
        }
        return new MethodResult(MethodResult.FAIL, "参数不能为空");
    }

    /**
     * @Description:删除共享存储信息
     * @param request
     * @param data
     * @throws Exception
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public MethodResult delete(@RequestParam(value = "data", defaultValue = "") String data, HttpServletRequest request) {
        logger.debug("SharedMemoryController.delete()");
        JSONObject json = JSONObject.fromObject(data);
        if (json != null && !json.isEmpty()) {
            try {

                sharedMemoryService.deleteInfo(json.getString("ids").split(","));
                operLogService.addLog("共享存储信息", "删除共享存储信息成功", "1", "1", request);

                // 写入配置文件
                String names = json.getString("names");
                String[] namesArr = names.split(",");
                AddressCache addressCache = AddressCacheManager.singleton().getCache();
                int length = namesArr.length;
                for (int i = 0; i < length; i++) {

                    addressCache.remove(namesArr[i]);
                }
                List<AddressVO> addressVOs = Arrays.asList(addressCache.getAll());
                sharedMemoryService.writeToFile(addressVOs);

                return new MethodResult(MethodResult.SUCCESS, "删除成功");
            } catch (Exception e) {
                e.printStackTrace();
                operLogService.addLog("共享存储信息", "删除共享存储信息失败", "1", "2", request);
                return new MethodResult(MethodResult.FAIL, "删除失败,请联系管理员");
            }
        }
        return new MethodResult(MethodResult.FAIL, "参数不能为空");
    }

    @RequestMapping(value="/{id}/available",method=RequestMethod.GET)
    @ResponseBody
    public MethodResult setAvailable(@PathVariable("id") String id,HttpServletRequest request){
        if( ! new TransFormPrivilegeUtil().isHasPrivilege(request, TransFormPrivilegeConstant.shared_memory_available)){
            return new MethodResult(MethodResult.FAIL,"您没有设置为可用的权限，请联系管理员");
        }
        MethodResult mr = sharedMemoryService.setAvailable(id);
        return mr;
    }
}
