package com.dimple.project.dashboard.controller;

import com.dimple.common.constant.BlogConstants;
import com.dimple.common.utils.CookieUtils;
import com.dimple.framework.config.SystemConfig;
import com.dimple.framework.shiro.session.OnlineSessionDAO;
import com.dimple.framework.web.controller.BaseController;
import com.dimple.framework.web.domain.AjaxResult;
import com.dimple.project.blog.blog.service.BlogService;
import com.dimple.project.chart.server.domain.Server;
import com.dimple.project.dashboard.domain.BusinessCommonData;
import com.dimple.project.dashboard.domain.VisitCount;
import com.dimple.project.dashboard.service.DashboardService;
import com.dimple.project.log.visitorLog.service.VisitLogService;
import com.dimple.project.system.menu.domain.Menu;
import com.dimple.project.system.menu.service.IMenuService;
import com.dimple.project.system.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.net.UnknownHostException;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @className: DashboardController
 * @description: 后台首页仪表盘显示Controller
 * @auther: Dimple
 * @date: 04/02/19
 * @version: 1.0
 */
@Controller
public class DashboardController extends BaseController {

    @Autowired
    private IMenuService menuService;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    BlogService blogService;

    @Autowired
    VisitLogService visitLogService;
    @Autowired
    DashboardService dashboardService;
//    @Autowired
//    OnlineSessionDAO onlineSessionDAO;

    // 系统首页
    @GetMapping("/index")
    public String index(ModelMap mmap,HttpServletRequest request,HttpServletResponse response) {
        // 取身份信息
        User user = getSysUser();
        // 根据用户id取出菜单
        List<Menu> menus = menuService.selectMenusByUser(user);
        mmap.put("menus", menus);
        mmap.put("user", user);
        mmap.put("copyrightYear", systemConfig.getCopyrightYear());
        String theme = getTheme(request,response);
        return "index_"+ theme;
    }


    // 图表首页
    @GetMapping("/dashboard/main")
    public String main(Model model) {

        model.addAttribute("version", systemConfig.getVersion());
        model.addAttribute("total", blogService.selectBlogCountByStatus(BlogConstants.BLOG_TOTAL));
        model.addAttribute("published", blogService.selectBlogCountByStatus(BlogConstants.BLOG_PUBLISHED));
        model.addAttribute("draft", blogService.selectBlogCountByStatus(BlogConstants.BLOG_DRAFT));
        model.addAttribute("garbage", blogService.selectBlogCountByStatus(BlogConstants.BLOG_GARBAGE));
        //在线用户数量
//        model.addAttribute("onlineCount", onlineSessionDAO.getActiveSessions().size());
        //访客总人数
        model.addAttribute("totalCount", visitLogService.selectVisitLogTotalCount());
        //本月访客人数
        model.addAttribute("todayCount", visitLogService.selectVisitLogTodayCount());
        //放置最新消息
        model.addAttribute("logMessages", dashboardService.selectLogMessage());
        return "main";
    }


    @GetMapping("/dashboard/spiderData/list")
    @ResponseBody
    public AjaxResult spiderData() {
        List<BusinessCommonData> businessCommonData = dashboardService.selectSpiderData();
        return AjaxResult.success().put("data", businessCommonData);
    }

    @GetMapping("/dashboard/memJvmCpuData/list")
    @ResponseBody
    public AjaxResult memJvmCpuData() throws UnknownHostException {
        Server server = new Server();
        List<Double> data = server.getDashBoardData();
        return AjaxResult.success().put("data", data);
    }

    @GetMapping("/dashboard/visitCount/list")
    @ResponseBody
    public AjaxResult visitCount() {
        List<VisitCount> visitData = dashboardService.getVisitData();
        return AjaxResult.success().put("data", visitData);
    }

    
    /**
   	 * 
   	 * @title: getTheme
   	 * @description: 加载风格
   	 * @param request
   	 * @return
   	 * @return: String
   	 */
   	private String getTheme(HttpServletRequest request,HttpServletResponse response) {
   		// 默认风格
   		String theme = "tab";
   		if (StringUtils.isEmpty(theme)) {
   			theme = "nomal";
   		}
   		// cookies配置中的模版
   		Cookie[] cookies = request.getCookies();
   		for (Cookie cookie : cookies) {
   			if (cookie == null || StringUtils.isEmpty(cookie.getName())) {
   				continue;
   			}
   			if (cookie.getName().equalsIgnoreCase("theme")) {
   				theme = cookie.getValue();
   			}
   		}
   		if(!theme.equals("tab")&&!theme.equals("nomal")){
   			theme = "tab";
   			CookieUtils.setCookie(response, "theme", theme);
   		}
   		return theme;
   	}

   	/**
   	 * Coookie设置
   	 */
   	@ResponseBody
   	@RequestMapping(value = "/theme/{theme}")
   	public AjaxResult getThemeInCookie(@PathVariable String theme, HttpServletRequest request,
   			HttpServletResponse response) {
   		if (!StringUtils.isEmpty(theme)) {
   			CookieUtils.setCookie(response, "theme", theme);
   		} else {
   			theme = CookieUtils.getCookie(request, "theme");
   		}
   		return AjaxResult.success();
   	}
}
