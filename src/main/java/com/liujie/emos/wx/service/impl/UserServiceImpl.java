package com.liujie.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.liujie.emos.wx.db.dao.TbDeptDao;
import com.liujie.emos.wx.db.dao.TbUserDao;
import com.liujie.emos.wx.db.pojo.MessageEntity;
import com.liujie.emos.wx.db.pojo.TbEmployee;
import com.liujie.emos.wx.db.pojo.TbUser;
import com.liujie.emos.wx.exception.EmosException;
import com.liujie.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl  implements UserService {

    public static final String ROOT_REGISTER_CODE = "000000";

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbDeptDao deptDao;


    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo,TbEmployee employee) {
        //如果邀请码是000000，代表是超级管理员
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(code);
        if (registerCode.equals(ROOT_REGISTER_CODE)) {
            //查询超级管理员帐户是否已经绑定
            boolean bool = userDao.haveRootUser();
            if (!bool) {
                //注册管理员
                TbUser user = getUserEntity(true, openId, code, nickname, photo,employee);
                //把当前用户绑定到ROOT帐户
                if(id.equals(null)){
                    userDao.insertUser(user);
                }else{
                    user.setId(id);
                    userDao.updateUserInfo(user);
                }

            } else {
                //如果root已经绑定了，就抛出异常
                throw new EmosException("无法绑定超级管理员账号");
            }
        } else{
            TbUser user = getUserEntity(false, openId, code, nickname, photo,employee);
            if(id.equals(null)) {
                userDao.insertUser(user);
            }else{
                user.setId(id);
                userDao.updateUserInfo(user);
            }
        }
        return userDao.searchIdByOpenId(openId);
    }

    private TbUser getUserEntity(boolean isRootUser, String openId, String code, String nickName, String photo, TbEmployee employee) {

        TbUser user = new TbUser();
        user.setRoot(isRootUser);
        user.setOpenId(openId);
        user.setNickname(nickName);
        user.setPhoto(photo);
        user.setCreateTime(new Date());
        user.setStatus((byte) 1);
        user.setSex(employee.getSex());
        user.setName(employee.getName());
        user.setTel(employee.getTel());
        user.setEmail(employee.getEmail());
        user.setDeptId(employee.getDeptId());
        user.setHiredate(employee.getHiredate());
        user.setRole(employee.getRole());
        return user;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions=userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if (id == null) {
            throw new EmosException("帐户不存在");
        }

        return id;
    }
    @Override
    public TbUser searchById(int userId) {
        TbUser user=userDao.searchById(userId);

        return user;
    }


    @Override
   public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
         return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1=deptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2=userDao.searchUserGroupByDept(keyword);
        for(HashMap map_1:list_1){
            long deptId=(Long)map_1.get("id");
            ArrayList members=new ArrayList();
            for(HashMap map_2:list_2){
                long id=(Long) map_2.get("deptId");
                if(deptId==id){
                    members.add(map_2);
                }
            }
            map_1.put("members",members);
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list=userDao.searchMembers(param);
        return list;
    }

    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        List<HashMap> list=userDao.selectUserPhotoAndName(param);
        return list;
    }

    @Override
    public String searchMemberEmail(int id) {
        String email=userDao.searchMemberEmail(id);
        return email;
    }

    @Override
    public int updateUserInfo(TbUser user) {
        return userDao.updateUserInfo(user);
    }

    @Override
    public int dimissionEmployee(int id) {
        return userDao.dimissionEmployee(id);
    }
}
