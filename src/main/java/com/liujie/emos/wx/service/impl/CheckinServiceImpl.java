package com.liujie.emos.wx.service.impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.liujie.emos.wx.config.SystemConstants;
import com.liujie.emos.wx.controller.form.CheckinForm;
import com.liujie.emos.wx.db.dao.*;
import com.liujie.emos.wx.db.pojo.TbCheckin;
import com.liujie.emos.wx.db.pojo.TbFaceModel;
import com.liujie.emos.wx.exception.EmosException;
import com.liujie.emos.wx.service.CheckinService;

import com.liujie.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants systemConstants;
    @Autowired
    private TbHolidaysDao holidaysDao;
    @Autowired
    private TbWorkdayDao workdayDao;
    @Autowired
    private TbCheckinDao checkinDao;
    @Autowired
    private TbFaceModelDao faceModelDao;
    @Autowired
    private TbCityDao cityDao;
    @Autowired
    private TbUserDao userDao;

    @Autowired
    private SystemConstants constants;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.code}")
    private String code;


    @Value("${emos.email.hr}")
    private String hrEmail;

    @Override
    public String validCanCheckIn(int userId, String date) {
        DateTime now = DateUtil.date();
        //组装上班签到的的开始时间，和结束时间
        String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
        String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
        boolean bool = haveCheckin(userId,date,start,end);
        DateTime attendanceStart = DateUtil.parse(start);
        DateTime attendanceEnd = DateUtil.parse(end);
        //组装下班签到的开始时间，和结束时间
        String offStart = DateUtil.today() + " " + systemConstants.closingStartTime;
        String offEnd = DateUtil.today() + " " + systemConstants.closingEndTime;
        DateTime attendanceOffStart = DateUtil.parse(offStart);
        DateTime attendanceOffEnd = DateUtil.parse(offEnd);
        /*if (now.isBefore(attendanceStart)) {
            return "没有到上班考勤开始时间";
        }else if(now.isAfter(attendanceStart)&&now.isBefore(attendanceEnd)){
            return bool ? "今日已经考勤，不用重复考勤" : "可以考勤";
        } else if (now.isAfter(attendanceEnd)&&now.isBefore(attendanceOffStart)) {
            return "不在考勤时间范围";
        }else {*/
            /*if(now.isAfter(attendanceOffStart)&&now.isBefore(attendanceOffEnd))*/
            return "可以考勤";
       /* }*/
    }

    private boolean haveCheckin(int userId, String date, String start, String end) {
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("date", date);
        map.put("start", start);
        map.put("end", end);
        //判断是否已签到
        boolean bool = checkinDao.haveCheckin(map) != null ? true : false;
        return bool;
    }


    @Override
    public void checkin(CheckinForm form, int userId, String path) {
        //判断签到
        Date d1 = DateUtil.date();
        //上班时间
        Date d2 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        //签到结束时间
        Date d3 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
        int status = 1;
        if (d1.compareTo(d2) <= 0) {
            status = 1; // 正常签到
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {
            status = 2; //迟到
        }
        //这里要获取签到地区新冠疫情风险等级
        int risk = 1;
        //查询城市简称
        if (form.getCity() != null && form.getCity().length() > 0 && form.getDistrict() != null && form.getDistrict().length() > 0) {
            String code = cityDao.searchCode(form.getCity());
            //查询地区风险
            try {
                String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + form.getDistrict();
                Document document = Jsoup.connect(url).get();
                Elements elements = document.getElementsByClass(" list-detail");
                for (Element one : elements) {
                    String result = one.text().split(" ")[1];
                    if ("高风险".equals(result)) {
                        risk = 3;
                        HashMap<String, String> map = userDao.searchNameAndDept(userId);
                        String name = map.get("name");
                        String deptName = map.get("dept_name");
                        deptName = deptName != null ? deptName : "";
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(hrEmail);
                        message.setSubject("员工" + name + "身处高风险疫情地区警告");
                        message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月 dd日") + "处于" + form.getAddress() + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                        emailTask.sendAsync(message);
                    } else if ("中风险".equals(result)) {
                        risk = risk < 2 ? 2 : risk;
                    }
                }
            } catch (IOException e) {
                log.error("执行异常", e);
                throw new EmosException("获取风险等级失败");
            }
            //保存签到记录
            TbCheckin entity = new TbCheckin();
            entity.setUserId(userId);
            entity.setAddress(form.getAddress());
            entity.setCountry(form.getCountry());
            entity.setProvince(form.getProvince());
            entity.setCity(form.getCity());
            entity.setDistrict(form.getDistrict());
            entity.setStatus((byte) status);
            entity.setRisk(risk);
            entity.setDate(DateUtil.today());
            entity.setCreateTime(d1);
            checkinDao.insert(entity);
        }
    }


    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long days = checkinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        //特殊节假日
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(param);
        //特殊工作日
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList list = new ArrayList();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            //查看今天是不是假期或者工作日
            String type = "工作日";
            if (one.isWeekend()) {
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {
                type = "工作日";
            }
            String status = "";
            //DateUtil.compare(one, DateUtil.date())<=0 当前之前 判断考勤
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "缺勤";
                boolean flag = false;
                for (HashMap<String, String> map : checkinList) {
                    if (map.containsValue(date)) {
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                    DateTime endTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
                    String today = DateUtil.today();
                    if (date.equals(today) && DateUtil.date().isBefore(endTime) && flag == false) {
                        status = "";
                    }
                }
            }
            HashMap map = new HashMap();
            map.put("date", date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }

}

