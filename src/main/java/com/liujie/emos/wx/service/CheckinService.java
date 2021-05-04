package com.liujie.emos.wx.service;

import com.liujie.emos.wx.controller.form.CheckinForm;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {

    public String validCanCheckIn(int userId, String date);

    public void checkin(CheckinForm form, int userId, String path) ;

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    public ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
