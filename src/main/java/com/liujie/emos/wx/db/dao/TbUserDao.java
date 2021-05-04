package com.liujie.emos.wx.db.dao;

import com.liujie.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {

    public boolean haveRootUser();

    public int insert(HashMap param);

    int insertUser(TbUser user);

    public Integer searchIdByOpenId(String openId);

    public Set<String> searchUserPermissions(int userId);

    public TbUser searchById(int userId);

    public HashMap searchNameAndDept(int userId);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);

    public HashMap searchUserInfo(int userId);

    public int searchDeptManagerId(int id);

    public int searchGmId();

    public List<HashMap> selectUserPhotoAndName(List param);

    public String searchMemberEmail(int id);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);


    int updateUserInfo(TbUser user);

    int dimissionEmployee(int id);
}