package com.liujie.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@ApiModel
public class MeetingSearchUserInMonthForm {
    @Range(min = 2000, max = 9999)
    private Integer year;
    @Range(min = 1, max = 12)
    private Integer month;
}
