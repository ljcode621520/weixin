package com.liujie.emos.wx.config.shiro;

import com.liujie.emos.wx.db.pojo.TbUser;
import com.liujie.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 1.拦截请求——>令牌封装成认证对象——>认证授权
 * shiro 实现认证与授权
 * @author liujie
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }
    /**
     * 授权(验证权限时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        TbUser user= (TbUser) principals.getPrimaryPrincipal();
        int userId=user.getId();
        Set<String> permsSet=userService.searchUserPermissions(userId);
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }
    /**
     * 认证(登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //TODO 从令牌中获取userId，然后检测该账户是否被冻结。
        String accessToken=(String)token.getPrincipal();
        int userId=jwtUtil.getUserId(accessToken);
        TbUser user=userService.searchById(userId);

        if(user==null){
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        //TODO 往info对象中添加用户信息、Token字符串
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(user,accessToken,getName());
        return info;
    }
}
