package com.dimple.project.front.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.dimple.framework.aspectj.lang.annotation.VLog;
import com.dimple.framework.web.controller.BaseController;
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
        
        // 查询用户信息
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
}
