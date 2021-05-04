package com.liujie.emos.wx.db.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * sys_config
 * @author 
 */
@Data
public class TbRole implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 权限集合
     */
    private Object permissions;

    private static final long serialVersionUID = 1L;
}