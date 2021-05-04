package com.liujie.emos.wx.service;

import com.liujie.emos.wx.db.pojo.TbEmployee;
import com.liujie.emos.wx.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface UserService {

    int registerUser(String registerCode, String code, String nickname, String photo, TbEmployee employee);

    Set<String> searchUserPermissions(int userId);

    public Integer login(String code);

    public TbUser searchById(int userId);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);

    public List<HashMap> selectUserPhotoAndName(List param);

    public String searchMemberEmail(int id);

    int updateUserInfo(TbUser user);

    int dimissionEmployee(int id);
}
