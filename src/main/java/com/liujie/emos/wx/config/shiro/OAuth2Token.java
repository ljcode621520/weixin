package com.liujie.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 把令牌封装成认证对象
 * @author liujie
 */
public class OAuth2Token implements AuthenticationToken {

    private String token;

    public OAuth2Token(String token){
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
