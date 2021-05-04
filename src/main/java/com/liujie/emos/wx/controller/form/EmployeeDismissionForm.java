package com.liujie.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
@ApiModel
public class EmployeeDismissionForm {

    @Min(1)
    private int id;
}