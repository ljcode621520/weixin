package com.liujie.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class DeptInsertForm {
    @NotBlank
    private String deptName;
}