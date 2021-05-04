package com.liujie.emos.wx.service.impl;


import com.liujie.emos.wx.db.dao.TbDeptDao;
import com.liujie.emos.wx.db.pojo.TbDept;
import com.liujie.emos.wx.exception.EmosException;
import com.liujie.emos.wx.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@Scope("prototype")
public class DeptServiceImpl implements DeptService {
    @Autowired
    private TbDeptDao deptDao;
    @Override
    public ArrayList<TbDept> searchAllDepts() {
        return deptDao.searchAllDepts();
    }

    @Override
    public void  insertDept(String deptName) {
        int row =  deptDao.insertDept(deptName);
        if (row != 1) {
            throw new EmosException("添加部门失败");
        }
    }

    @Override
    public void  deleteDept(int id) {
        //查询部门下是否有员工，没有则删除

        int row = deptDao.deleteDept(id);
        if (row != 1) {
            throw new EmosException("删除部门失败");
        }
    }

    @Override
    public void updateDept(TbDept dept) {
        int row = deptDao.updateDept(dept);
        if (row != 1) {
            throw new EmosException("更新部门失败");
        }
    }
}
