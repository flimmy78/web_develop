package com.zhicloud.op.app.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zhicloud.op.core.CoreSpringContextManager;
import com.zhicloud.op.service.AgentService;
 
/**
 * 时间监听器
 * 
 * @author xiaoqun.yi
 */
public class CheckBalanceForAgentListener implements ServletContextListener {
    private Timer timer = new Timer();
    public static ServletContext servletContext;
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
    public void contextInitialized(ServletContextEvent sce) {
        try {
 
            // 第一个参数是要运行的代码，第二个参数是从什么时候开始运行，第三个参数是每隔多久在运行一次。 
            sce.getServletContext().log("检测停用云主机定时器已启动");
            // 设置在每天10:00:00分执行任务
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 10); // 10 ,可以更改时间
            calendar.set(Calendar.MINUTE, 0); // 0可以更改分数
            calendar.set(Calendar.SECOND, 0);// 0 //默认为0,不以秒计
            Date date = calendar.getTime();
            if (date.before(new Date())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                date = calendar.getTime();
            }
            // 第一个参数 是要运行的代码，第二个参数是指定的时间
            timer.schedule(new CheckBalanceForAgent(),date,PERIOD_DAY);
        } catch (Exception e) {
        	
        }
    }
 
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            timer.cancel();
        } catch (Exception e) {
        }
    }
}
 
/**
 * 时间任务器
 * 
 * @author xiaoqun.yi
 */
class CheckBalanceForAgent extends TimerTask {
	AgentService agentService = CoreSpringContextManager.getAgentService();
    /**
     * 把要定时执行的任务就在run中
     */
    public void run() {
        try { 
        	agentService.CheckBalanceForAgent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
