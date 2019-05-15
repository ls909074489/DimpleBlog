package com.dimple.project.front.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dimple.common.utils.StringUtils;
import com.dimple.common.utils.reflect.ReflectUtils;
import com.dimple.common.utils.security.ShiroUtils;
import com.dimple.framework.aspectj.lang.annotation.VLog;
import com.dimple.framework.web.controller.BaseController;
import com.dimple.framework.web.domain.AjaxResult;
import com.dimple.project.blog.blog.domain.Blog;
import com.dimple.project.blog.blog.service.BlogService;
import com.dimple.project.blog.category.service.CategoryService;
import com.dimple.project.blog.tag.domain.Tag;
import com.dimple.project.blog.tag.service.TagService;
import com.dimple.project.front.domain.CustomFunc;
import com.dimple.project.front.service.HomeService;
import com.dimple.project.link.service.LinkService;
import com.dimple.project.system.carouselMap.service.CarouselMapService;
import com.dimple.project.system.notice.service.INoticeService;
import com.dimple.project.system.user.domain.User;
import com.dimple.project.system.user.service.IUserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 用户定义
 * @author 90907
 *
 */
@Controller
public class CustomController  extends BaseController {
		
		private static Logger logger = LoggerFactory.getLogger(CustomController.class);

	
		@Autowired
	    HomeService homeService;
	    @Autowired
	    BlogService blogService;
	    @Autowired
	    CategoryService categoryService;
	    @Autowired
	    TagService tagService;
	    @Autowired
	    LinkService linkService;
	    @Autowired
	    INoticeService noticeService;
	    @Autowired
	    CarouselMapService carouselMapService;
	    @Autowired
	    private IUserService userService;

	    /**
	     * 设置前台页面公用的部分代码
	     * 均设置Redis缓存
	     */
	    private void setCommonMessage(Model model,String loginName) {
	        //获取分类下拉项中的分类
//	        model.addAttribute("categories", categoryService.selectSupportCategoryList());
	    	
	    	List<CustomFunc> funcList = new ArrayList<>();
	    	funcList.add(new CustomFunc("/"+loginName+"/index.html", "首页"));
	    	funcList.add(new CustomFunc("/"+loginName+"/images.html", "图片"));
	    	model.addAttribute("funcList", funcList);
	    	
	        //查询所有的标签
	        model.addAttribute("tags", tagService.selectTagList(new Tag()));
	        //查询最近更新的文章
	        model.addAttribute("newestUpdateBlog", blogService.selectNewestUpdateBlog());
	        //查询文章排行
	        model.addAttribute("blogRanking", blogService.selectBlogRanking());
	        //查询推荐博文
	        model.addAttribute("supportBlog", blogService.selectSupportBlog());
	        //查询通知
	        model.addAttribute("notices", noticeService.selectNoticeListDisplay());
	        //获取友链信息
	        model.addAttribute("links", linkService.selectLinkListFront());
	    }
    
    @GetMapping("/{loginName}.html")
    @VLog(title = "用户首页")
    public String defaultIndex(@PathVariable String loginName,Integer pageNum,  Model model) {
        setCommonMessage(model,loginName);
        PageHelper.startPage(pageNum == null ? 1 : pageNum, 12, "create_time desc");
        model.addAttribute("blogs", new PageInfo<>(homeService.selectFrontBlogList(new Blog())));
        //放置轮播图
        model.addAttribute("carouselMaps", carouselMapService.selectCarouselMapListFront());
        model.addAttribute("curUser", ShiroUtils.getSysUser());
        
        User user = userService.selectUserByLoginName(loginName);
        if(user!=null){
        	model.addAttribute("user", user);
        	return "front/custom/index";
        }
        return "front/index";        
    }
    
    @GetMapping("/{loginName}/index.html")
    @VLog(title = "用户首页")
    public String loginNameIndex(@PathVariable String loginName,Integer pageNum,  Model model) {
        return defaultIndex(loginName, pageNum, model);        
    }
    
    
    @VLog(title = "跳转到前端登录页面")
    @GetMapping("/front/toLogin")
    public String toLogin(Model model) {
        return "front/login/login";
    }
    
    @PostMapping("/front/login")
    @ResponseBody
    public AjaxResult frontLogin(String loginName,String password,  Model model) {
    	UsernamePasswordToken token = new UsernamePasswordToken(loginName, password, false);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return success(loginName);
        } catch (AuthenticationException e) {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage())) {
                msg = e.getMessage();
            }
            return error(msg);
        }
    }
    
    @RequestMapping("/front/loginSuc")
    public String loginSuc(Model model) {
    	User user = ShiroUtils.getSysUser();
    	 if(user!=null){
    		 Integer pageNum = 0;
    		setCommonMessage(model,user.getLoginName());
	        PageHelper.startPage(pageNum == null ? 1 : pageNum, 12, "create_time desc");
	        model.addAttribute("blogs", new PageInfo<>(homeService.selectFrontBlogList(new Blog())));
	        //放置轮播图
	        model.addAttribute("carouselMaps", carouselMapService.selectCarouselMapListFront());
    	        
         	model.addAttribute("user", user);
         	return redirect("/front/custom/index");
         }
         return "front/index";        
    }
    
    @RequestMapping("/front/logout")
    public String loginLogout(String loginName,Model model,ServletRequest request, ServletResponse response) {
    	Subject subject = SecurityUtils.getSubject();
        if (subject != null) {
            subject.logout();
        }
    	return defaultIndex(loginName, 0, model);  
    }
    
    
    /**
     * 注册
     * @param loginName
     * @param password
     * @param model
     * @return
     */
    @GetMapping("/front/toReg")
    public String frontToReg(String loginName,String password,  Model model) {
    	  return "front/login/reg";
    }
    
    @PostMapping("/front/reg")
    @ResponseBody
    public AjaxResult frontReg(User user) {
        try {
        	user.setUserName(user.getLoginName());
        	userService.regUser(user);
            return success(user.getLoginName());
        }catch(DuplicateKeyException e){
        		logger.error(e.getMessage());
        	 return error("该用户已注册");
        } catch (Exception e) {
        	logger.error(e.getMessage());
            return error("注册失败，请联系管理员");
        }
    }
    
  
}
