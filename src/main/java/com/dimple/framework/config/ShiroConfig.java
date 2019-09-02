package com.dimple.framework.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.commons.io.IOUtils;
//import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dimple.common.utils.file.FileUploadUtils;
import com.dimple.framework.shiro.realm.UserRealm;
import com.dimple.framework.shiro.session.ShiroSessionListener;
import com.dimple.framework.shiro.web.filter.FrontLogoutFilter;
import com.dimple.framework.shiro.web.filter.KickoutSessionControlFilter;
import com.dimple.framework.shiro.web.filter.LogoutFilter;
import com.dimple.framework.shiro.web.filter.captcha.CaptchaValidateFilter;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;

/**
 * @className: ShiroConfig
 * @description: 权限配置加载
 * @auther: Dimple
 * @Date: 2019/3/13
 * @Version: 1.1
 */
@Configuration
public class ShiroConfig {
    public static final String PREMISSION_STRING = "perms[\"{0}\"]";

    // Session超时时间，单位为毫秒（默认30分钟）
    @Value("${shiro.session.expireTime}")
    private int expireTime;

    // 相隔多久检查一次session的有效性，单位毫秒，默认就是10分钟
    @Value("${shiro.session.validationInterval}")
    private int validationInterval;

    // 验证码开关
    @Value("${shiro.user.captchaEnabled}")
    private boolean captchaEnabled;

    // 验证码类型
    @Value("${shiro.user.captchaType}")
    private String captchaType;

    // 设置Cookie的域名
    @Value("${shiro.cookie.domain}")
    private String domain;

    // 设置cookie的有效访问路径
    @Value("${shiro.cookie.path}")
    private String path;

    // 设置HttpOnly属性
    @Value("${shiro.cookie.httpOnly}")
    private boolean httpOnly;

    // 设置Cookie的过期时间，秒为单位
    @Value("${shiro.cookie.maxAge}")
    private int maxAge;

    // 登录地址
    @Value("${shiro.user.loginUrl}")
    private String loginUrl;

    // 权限认证失败地址
    @Value("${shiro.user.unauthorizedUrl}")
    private String unauthorizedUrl;
    
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

//    @Value("${spring.redis.timeout}")
//    private int timeout;

    @Value("${spring.redis.password}")
    private String password;
    

    /**
     * 缓存管理器 使用Ehcache实现
     */
//    @Bean
//    public EhCacheManager getEhCacheManager() {
//        net.sf.ehcache.CacheManager cacheManager = net.sf.ehcache.CacheManager.getCacheManager("dimple");
//        EhCacheManager em = new EhCacheManager();
//        if (StringUtils.isNull(cacheManager)) {
//            em.setCacheManager(new net.sf.ehcache.CacheManager(getCacheManagerConfigFileInputStream()));
//            return em;
//        } else {
//            em.setCacheManager(cacheManager);
//            return em;
//        }
//    }


    /**
     * 返回配置文件流 避免ehcache配置文件一直被占用，无法完全销毁项目重新部署
     */
    protected InputStream getCacheManagerConfigFileInputStream() {
        String configFile = "classpath:ehcache/ehcache-shiro.xml";
        InputStream inputStream = null;
        try {
            inputStream = ResourceUtils.getInputStreamForPath(configFile);
            byte[] b = IOUtils.toByteArray(inputStream);
            InputStream in = new ByteArrayInputStream(b);
            return in;
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Unable to obtain input stream for cacheManagerConfigFile [" + configFile + "]", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 自定义Realm
     */
//    @Bean
//    public UserRealm userRealm(EhCacheManager cacheManager) {
//        UserRealm userRealm = new UserRealm();
//        userRealm.setCacheManager(cacheManager);
//        return userRealm;
//    }
    
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
//        userRealm.setCacheManager(cacheManager);
        return userRealm;
    }

    /**
     * 自定义sessionDAO会话
     */
//    @Bean
//    public OnlineSessionDAO sessionDAO() {
//        OnlineSessionDAO sessionDAO = new OnlineSessionDAO();
//        return sessionDAO;
//    }

    /**
     * 自定义sessionFactory会话
     */
//    @Bean
//    public OnlineSessionFactory sessionFactory() {
//        OnlineSessionFactory sessionFactory = new OnlineSessionFactory();
//        return sessionFactory;
//    }

    /**
     * 配置Session监听
     */
    @Bean("sessionListener")
    public ShiroSessionListener sessionListener() {
        ShiroSessionListener shiroSessionListener = new ShiroSessionListener();
        return shiroSessionListener;
    }


    /**
     * 自定义sessionFactory调度器
     */
//    @Bean
//    public SpringSessionValidationScheduler sessionValidationScheduler() {
//        SpringSessionValidationScheduler sessionValidationScheduler = new SpringSessionValidationScheduler();
//        // 相隔多久检查一次session的有效性，单位毫秒，默认就是10分钟
//        sessionValidationScheduler.setSessionValidationInterval(validationInterval * 60 * 1000);
//        // 设置会话验证调度器进行会话验证时的会话管理器
//        sessionValidationScheduler.setSessionManager(sessionValidationManager());
//        return sessionValidationScheduler;
//    }

    /**
     * 会话管理器
     */
//    @Bean
//    public OnlineWebSessionManager sessionValidationManager() {
//        OnlineWebSessionManager manager = new OnlineWebSessionManager();
//        Collection<SessionListener> listeners = new ArrayList<SessionListener>();
//        //配置监听
//        listeners.add(sessionListener());
//        manager.setSessionListeners(listeners);
//
//        // 加入缓存管理器
//        manager.setCacheManager(getEhCacheManager());
//        // 删除过期的session
//        manager.setDeleteInvalidSessions(true);
//        // 设置全局session超时时间
//        manager.setGlobalSessionTimeout(expireTime * 60 * 1000);
//        // 去掉 JSESSIONID
//        manager.setSessionIdUrlRewritingEnabled(false);
//        // 是否定时检查session
//        manager.setSessionValidationSchedulerEnabled(true);
//        // 自定义SessionDao
//        manager.setSessionDAO(sessionDAO());
//        // 自定义sessionFactory
//        manager.setSessionFactory(sessionFactory());
//        return manager;
//    }

    /**
     * 会话管理器
     */
//    @Bean
//    public OnlineWebSessionManager sessionManager() {
//        OnlineWebSessionManager manager = new OnlineWebSessionManager();
//        // 加入缓存管理器
//        manager.setCacheManager(getEhCacheManager());
//        // 删除过期的session
//        manager.setDeleteInvalidSessions(true);
//        // 设置全局session超时时间
//        manager.setGlobalSessionTimeout(expireTime * 60 * 1000);
//        // 去掉 JSESSIONID
//        manager.setSessionIdUrlRewritingEnabled(false);
//        // 定义要使用的无效的Session定时调度器
//        manager.setSessionValidationScheduler(sessionValidationScheduler());
//        // 是否定时检查session
//        manager.setSessionValidationSchedulerEnabled(true);
//        // 自定义SessionDao
//        manager.setSessionDAO(sessionDAO());
//        // 自定义sessionFactory
//        manager.setSessionFactory(sessionFactory());
//        return manager;
//    }

    /**
     * 安全管理器
     */
    @Bean
    public SecurityManager securityManager(UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(userRealm);
        // 记住我
        securityManager.setRememberMeManager(rememberMeManager());
        // 注入缓存管理器;
//        securityManager.setCacheManager(getEhCacheManager());
//        // session管理器
//        securityManager.setSessionManager(sessionManager());
        
        // 注入缓存管理器;
        securityManager.setCacheManager(redisCacheManager());
        // session管理器
        securityManager.setSessionManager(sessionManager());
        
        return securityManager;
    }

    /**
     * cacheManager 缓存 redis实现
     * 使用的是shiro-redis开源插件
     * @return
     */
    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        return redisCacheManager;
    }
    
    /**
     * 配置shiro redisManager
     * 使用的是shiro-redis开源插件
     * @return
     */
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        redisManager.setPort(port);
        // 配置缓存过期时间
        redisManager.setExpire(1800);
//        redisManager.setTimeout(timeout);
        redisManager.setPassword(password);
        return redisManager;
    }
    
    /**
     * RedisSessionDAO shiro sessionDao层的实现 通过redis
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        return redisSessionDAO;
    }
    
    /**
     * shiro session的管理
     */
    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO());
        return sessionManager;
    }
    
    /**
     * 退出过滤器
     */
    public LogoutFilter logoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setLoginUrl(loginUrl);
        return logoutFilter;
    }

    /**
     * Shiro过滤器配置
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // Shiro的核心安全接口,这个属性是必须的
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 身份认证失败，则跳转到登录页面的配置
        shiroFilterFactoryBean.setLoginUrl(loginUrl);
        // 权限认证失败，则跳转到指定页面
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        // Shiro连接约束配置，即过滤链的定义
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 对静态资源设置匿名访问
        filterChainDefinitionMap.put("/favicon.ico**", "anon");
        filterChainDefinitionMap.put("/dimple.png**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/docs/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/ajax/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/dimple/**", "anon");
        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put(FileUploadUtils.uploadFilePath+"/**", "anon");
        filterChainDefinitionMap.put("/public/**", "anon");
        
        filterChainDefinitionMap.put("/**.html", "anon");
        filterChainDefinitionMap.put("/**/**.html", "anon");
        
        
        //前台界面设置允许访问
//        setFront(filterChainDefinitionMap);
        filterChainDefinitionMap.put("/f/**", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/about**", "anon");
        filterChainDefinitionMap.put("/f/about**", "anon");
        filterChainDefinitionMap.put("/front/**", "anon");

        filterChainDefinitionMap.put("/captcha/captchaImage**", "anon");
        // 退出 logout地址，shiro去清除session
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/frontLogout", "frontLogout");
        // 不需要拦截的访问
        filterChainDefinitionMap.put("/login", "anon,captchaValidate");
        // 系统权限列表
        // filterChainDefinitionMap.putAll(SpringUtils.getBean(IMenuService.class).selectPermsAll());

        Map<String, Filter> filters = new LinkedHashMap<>();
//        filters.put("onlineSession", onlineSessionFilter());
//        filters.put("syncOnlineSession", syncOnlineSessionFilter());
        filters.put("captchaValidate", captchaValidateFilter());
        filters.put("kickout", kickoutSessionControlFilter());
        
        
        // 注销成功，则跳转到指定页面
        filters.put("logout", logoutFilter());
        FrontLogoutFilter frontLogoutFilter = new FrontLogoutFilter();
        frontLogoutFilter.setLoginUrl("front/custom/index");
        filters.put("frontLogout", frontLogoutFilter);
        shiroFilterFactoryBean.setFilters(filters);

        // 所有请求需要认证
//        filterChainDefinitionMap.put("/**", "user,onlineSession,syncOnlineSession");
        filterChainDefinitionMap.put("/**", "user");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }
    
    
    /**
     * 限制同一账号登录同时登录人数控制
     * @return
     */
    public KickoutSessionControlFilter kickoutSessionControlFilter(){
        KickoutSessionControlFilter kickoutSessionControlFilter = new KickoutSessionControlFilter();
        //使用cacheManager获取相应的cache来缓存用户登录的会话；用于保存用户—会话之间的关系的；
        //这里我们还是用之前shiro使用的redisManager()实现的cacheManager()缓存管理
        //也可以重新另写一个，重新配置缓存时间之类的自定义缓存属性
        kickoutSessionControlFilter.setCacheManager(redisCacheManager());
        //用于根据会话ID，获取会话进行踢出操作的；
        kickoutSessionControlFilter.setSessionManager(sessionManager());
        //是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序。
        kickoutSessionControlFilter.setKickoutAfter(false);
        //同一个用户最大的会话数，默认5；比如5的意思是同一个用户允许最多同时五个人登录；
        kickoutSessionControlFilter.setMaxSession(5);
        //被踢出后重定向到的地址；
        kickoutSessionControlFilter.setKickoutUrl("/kickout");
        return kickoutSessionControlFilter;
    }

    /**
     * 自定义在线用户处理过滤器
     */
//    @Bean
//    public OnlineSessionFilter onlineSessionFilter() {
//        OnlineSessionFilter onlineSessionFilter = new OnlineSessionFilter();
//        onlineSessionFilter.setLoginUrl(loginUrl);
//        return onlineSessionFilter;
//    }

    /**
     * 自定义在线用户同步过滤器
     */
//    @Bean
//    public SyncOnlineSessionFilter syncOnlineSessionFilter() {
//        SyncOnlineSessionFilter syncOnlineSessionFilter = new SyncOnlineSessionFilter();
//        return syncOnlineSessionFilter;
//    }

    /**
     * 自定义验证码过滤器
     */
    @Bean
    public CaptchaValidateFilter captchaValidateFilter() {
        CaptchaValidateFilter captchaValidateFilter = new CaptchaValidateFilter();
        captchaValidateFilter.setCaptchaEnabled(captchaEnabled);
        captchaValidateFilter.setCaptchaType(captchaType);
        return captchaValidateFilter;
    }

    /**
     * cookie 属性设置
     */
    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge * 24 * 60 * 60);
        return cookie;
    }

    /**
     * 记住我
     */
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        cookieRememberMeManager.setCipherKey(Base64.decode("fCq+/xW488hMTCD+cmJ3aQ=="));
        return cookieRememberMeManager;
    }

    /**
     * thymeleaf模板引擎和shiro框架的整合
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

    /**
     * 开启Shiro注解通知器
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            @Qualifier("securityManager") SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
