package com.liujie.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * OAuth2Filter拦截请求——>(更新令牌)TokenAspect——>AOP拦截controller,有令牌，写入ThreadLocalToken——>OAuth2Filter从写入ThreadLocalToken获取返给前端
 * @author liujie
 * 刷新令牌媒介
 */
@Component
public class ThreadLocalToken {

    private ThreadLocal local=new ThreadLocal();

    public void setToken(String token){
        local.set(token);
    }
    public String getToken(){
        return (String) local.get();
    }
    public void clear(){
        local.remove();
    }
}
