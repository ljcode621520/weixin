package com.liujie.emos.wx.service;


import com.liujie.emos.wx.db.pojo.TbDept;

import java.util.ArrayList;

public interface DeptService {

    ArrayList<TbDept> searchAllDepts();
    void insertDept(String deptName);

    void deleteDept(int id);

    void updateDept(TbDept dept);


}
