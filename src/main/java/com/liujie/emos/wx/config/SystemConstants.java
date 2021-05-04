package com.liujie.emos.wx.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SystemConstants {

    /**
     * 签到开始时间
     */
    public String attendanceStartTime;

    /**
     * 签到结束时间
     */
    public String attendanceEndTime;

    /**
     * 上班时间
     */
    public String attendanceTime;


    /**
     * 下班考勤开始时间
     */
    public String closingStartTime;
    /**
     * 下班时间
     */
    public String closingTime;
    /**
     * 下班考勤结束时间
     */
    public String closingEndTime;
}
