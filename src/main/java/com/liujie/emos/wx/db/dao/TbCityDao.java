package com.liujie.emos.wx.db.dao;

import com.liujie.emos.wx.db.pojo.TbCity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbCityDao {
    public String searchCode(String city);
}