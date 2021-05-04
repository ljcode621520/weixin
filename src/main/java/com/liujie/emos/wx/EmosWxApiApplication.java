package com.liujie.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.liujie.emos.wx.config.SystemConstants;
import com.liujie.emos.wx.db.dao.SysConfigDao;
import com.liujie.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@ServletComponentScan
@SpringBootApplication
@EnableAsync
@Slf4j
public class EmosWxApiApplication {

    @Resource
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    /**
     * 会在该类的依赖注入完成之后自动被调用
     */
    @PostConstruct
    public void init() {
        new File(imageFolder).mkdirs();
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(one -> {
            String key = one.getParamKey();
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue();
            try {
                Field field = constants.getClass().getDeclaredField(key);
                field.set(constants, value);
            } catch (Exception e) {
                log.error("执行异常", e);
            }
        });
    }
}
