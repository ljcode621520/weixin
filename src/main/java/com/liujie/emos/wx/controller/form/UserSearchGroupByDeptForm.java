package com.liujie.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
@ApiModel
public class UserSearchGroupByDeptForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,15}$")
    private String keyword;
}
