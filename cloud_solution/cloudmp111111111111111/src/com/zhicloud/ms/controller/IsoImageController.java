package com.zhicloud.ms.controller;

import java.io.BufferedReader;
import java.io.File; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; 
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

 





import com.zhicloud.ms.app.pool.IsoImagePool;
import com.zhicloud.ms.app.pool.IsoImagePool.IsoImageData;
import com.zhicloud.ms.app.pool.IsoImagePoolManager; 
import com.zhicloud.ms.common.util.StringUtil;
import com.zhicloud.ms.constant.AppInconstant;
import com.zhicloud.ms.remote.MethodResult;
import com.zhicloud.ms.service.IOperLogService;
import com.zhicloud.ms.service.ImageUploadAddressService;
import com.zhicloud.ms.service.IsoImageService;
import com.zhicloud.ms.service.SharedMemoryService;
import com.zhicloud.ms.service.impl.EmailTemplateServiceImpl;
import com.zhicloud.ms.transform.constant.TransFormPrivilegeConstant;
import com.zhicloud.ms.transform.util.TransFormLoginHelper;
import com.zhicloud.ms.transform.util.TransFormPrivilegeUtil;
import com.zhicloud.ms.vo.SharedMemoryVO;
@Controller
@RequestMapping("/image")
public class IsoImageController {
    
    public static final Logger logger = Logger.getLogger(EmailTemplateServiceImpl.class);

    
    @Resource
    private IOperLogService operLogService;
    
    @Resource
    private IsoImageService isoImageService;
    
    @Resource
    private SharedMemoryService sharedMemoryService;
    
    @Resource
    private ImageUploadAddressService imageUploadAddressService;
     
    
    
    @RequestMapping(value="/isoimage/all",method=RequestMethod.GET)
    public String toAll(Model model,HttpServletRequest request, HttpServletResponse response) throws IOException
    { 
        if( ! new TransFormPrivilegeUtil().isHasPrivilege(request, TransFormPrivilegeConstant.iso_image_query)){
            return "not_have_access";
        }
//        IsoImageProgressPool pool = IsoImageProgressPoolManager.singleton().getPool();
        IsoImagePool pool = IsoImagePoolManager.getSingleton().getIsoImagePool();
        List<IsoImageData> isoArray = pool.getAllIsoImageData();
        model.addAttribute("isoArray", isoArray); 
        model.addAttribute("clientIP", TransFormLoginHelper.getClientIP(request));
        model.addAttribute("serverIP", imageUploadAddressService.getAvailableAddress(request));
        model.addAttribute("chunkSize", AppInconstant.chunkSize);
        return "isoimage/iso_image_manage";
    }
    
    @RequestMapping(value="/isoimage/add",method=RequestMethod.GET)
    public String addPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!new TransFormPrivilegeUtil().isHasPrivilege(request, TransFormPrivilegeConstant.iso_image_add)) {
            return "not_have_access";
        }
        return "isoimage/iso_image_add";
    }
    
    @RequestMapping(value="/isoimage/add",method=RequestMethod.POST)
    @ResponseBody
    public MethodResult add(@RequestParam("file")MultipartFile attach,String imagename,String type,String description,HttpServletRequest request, HttpServletResponse response) throws IOException
    { 
        if( ! new TransFormPrivilegeUtil().isHasPrivilege(request, TransFormPrivilegeConstant.iso_image_add)){
            return new MethodResult(MethodResult.FAIL,"您没有上传镜像的权限，请联系管理员");
        }
        SharedMemoryVO vo = sharedMemoryService.queryAvailable();
        if(vo == null){
            return new MethodResult(MethodResult.FAIL,"路径未定义");
        }
        MultipartHttpServletRequest  multipartRequest = (MultipartHttpServletRequest) request;
         
        multipartRequest.getFile("filename");
        String uuid = StringUtil.generateUUID();
         
        //获取文件名
        String fileName = new String(attach.getOriginalFilename().getBytes("ISO-8859-1"), "UTF-8");
         
        //定义上传路径
        String filePath = "/image/iso_image/system/";
        
        //若无该文件夹自动创建
        File fp = new File(filePath);
        
        if(!fp.exists()){
            fp.mkdirs();
        }
        
        File file = new File(filePath+"/"+uuid+".iso");
        
        // 上传文件
        FileUtils.copyInputStreamToFile(attach.getInputStream(), file);
        MethodResult result = isoImageService.upload(imagename, uuid, "iso_image/system/"+uuid+".iso", type, description);
        if(result.isSuccess()){
            operLogService.addLog("iso镜像", "上传镜像"+imagename, "1", "1", multipartRequest);
        }else{
            operLogService.addLog("iso镜像", "上传镜像"+imagename, "1", "2", multipartRequest);
        }
        return result;
    }
    
    @RequestMapping(value="/isoimage/checkNas",method=RequestMethod.GET)
    @ResponseBody
    public MethodResult checknas(String url) throws IOException
    { 
          String[] ip = StringUtil.getIpFromUrl(url);
        if(ip == null || ip.length <= 0){
            return new MethodResult(MethodResult.FAIL,"未挂载");
        }
        Process pro = Runtime.getRuntime().exec("showmount -e "+ip[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
         int i = 0;
        String str = br.readLine();
        while(!StringUtils.isBlank(str)){
            if(i == 0){
                continue;
            }else{
                String [] ips = StringUtil.getIpFromUrl(str); 
                if(ips != null && ips.length >= 1){
                    if(ip[0].equals(ips)){
                        
                    } 
                } 
            }                 
            str = br.readLine();
        }
        MethodResult result =  new MethodResult(MethodResult.SUCCESS,"已经挂载");
        
        return result;
    }
     
 
    /**
     * 
    * @Title: executeShell 
    * @Description: 查询绑定情况
    * @param @return
    * @param @throws IOException      
    * @return Map<String,String>     
    * @throws
     */
    public Map<String,String> executeShellOfQueryNas(String ip) throws IOException { 
        Map<String,String> temp = new HashMap<String,String>();
        try {
            Process pro = Runtime.getRuntime().exec("showmount -e "+ip);
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
             int i = 0;
            String str = br.readLine();
            while(!StringUtils.isBlank(str)){
                if(i == 0){
                    continue;
                }else{
                    String [] ips = StringUtil.getIpFromUrl(str); 
                    if(ips != null && ips.length >= 1){
                        temp.put(ips[0], str.replace(ips[0], ""));
                    } 
                }                 
                str = br.readLine();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        } 
        return temp;
     } 
    
    public int executeShellOfMount(String url) throws IOException { 
        Map<String,String> temp = new HashMap<String,String>();
        try {
            Process pro = Runtime.getRuntime().exec("mount -t nfs "+url+" /image/");
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
             int i = 0;
            String str = br.readLine();
            while(!StringUtils.isBlank(str)){
                if(i == 0){
                    continue;
                }else{
                    String [] ips = StringUtil.getIpFromUrl(str); 
                    if(ips != null && ips.length >= 1){
                        temp.put(ips[0], str.replace(ips[0], ""));
                    } 
                }                 
                str = br.readLine();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }  
        return 1;
     } 
    /**
     * 
    * @Title: startCloudHost 
    * @Description: 删除镜像 
    * @param @param id
    * @param @param request
    * @param @return      
    * @return MethodResult     
    * @throws
     */
    @RequestMapping(value="/isoimage/{id}/delete",method=RequestMethod.GET)
    @ResponseBody
    public MethodResult startCloudHost(@PathVariable("id") String id,HttpServletRequest request){
        if( ! new TransFormPrivilegeUtil().isHasPrivilege(request, TransFormPrivilegeConstant.iso_image_delete)){
            return new MethodResult(MethodResult.FAIL,"您没有删除光盘镜像的权限，请联系管理员");
        }
        IsoImagePool pool = IsoImagePoolManager.getSingleton().getIsoImagePool();
        IsoImageData data = pool.getByRealImageId(id);
        MethodResult mr = isoImageService.delete(id);
        if(mr.isSuccess()){
            operLogService.addLog("iso镜像", "删除镜像"+data.getName(), "1", "1", request);
        }else{
            operLogService.addLog("iso镜像", "删除镜像"+data.getName(), "1", "2", request);
        }
        return mr;
    }
    
}
