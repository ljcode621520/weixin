package com.liujie.emos.wx.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.liujie.emos.wx.common.util.R;
import com.liujie.emos.wx.config.shiro.JwtUtil;
import com.liujie.emos.wx.config.tencent.TLSSigAPIv2;
import com.liujie.emos.wx.controller.form.*;
import com.liujie.emos.wx.db.pojo.TbEmployee;
import com.liujie.emos.wx.db.pojo.TbUser;
import com.liujie.emos.wx.exception.EmosException;
import com.liujie.emos.wx.service.EmployeeService;
import com.liujie.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模块Web接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EmployeeService employeeService;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        //获取邀请码
        int code = Integer.parseInt(form.getRegisterCode());
        TbEmployee employee = employeeService.searchByCode(code);
        if (Objects.isNull(employee)) {
            return R.error("注册码不对，请于管理员联系确认");
        }
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto(),employee);
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        HashMap params = new HashMap();
        params.put("code", form.getRegisterCode());
        params.put("state", 1);
        employeeService.updateState(params);
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);
    }

    private void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }

    @PostMapping("/login")
    @ApiOperation("登陆系统")
    public R login(@Valid @RequestBody LoginForm form) {
        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        return R.ok("登陆成功").put("token", token).put("permission", permsSet);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
         int userId = jwtUtil.getUserId(token);
         HashMap map = userService.searchUserSummary(userId);
         return R.ok().put("result", map);
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("更新用户信息")
    public R updateUserInfo(@RequestHeader("token") String token, @RequestBody String json) {
        int userId = jwtUtil.getUserId(token);
        JSONObject jsonObject = new JSONObject(json);
        TbUser user = new TbUser();
        if (jsonObject.containsKey("id")) {
            user.setId(jsonObject.getInt("id"));
        } else {
            user.setId(userId);
        }
        if (jsonObject.containsKey("tel")) {
            user.setTel(jsonObject.getStr("tel"));
        }
        if (jsonObject.containsKey("name")) {
            user.setName(jsonObject.getStr("name"));
        }
        if (jsonObject.containsKey("email")) {
            user.setEmail(jsonObject.getStr("email"));
        }
        if (jsonObject.containsKey("sex")) {
            user.setSex(jsonObject.getStr("sex"));
        }
        if (jsonObject.containsKey("hiredate")) {
            try {
                user.setHiredate(new SimpleDateFormat("yyyy-MM-DD").parse(jsonObject.getStr("hiredate")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.containsKey("status")) {
            user.setStatus(jsonObject.getInt("status").byteValue());
        }

        if (jsonObject.containsKey("role")) {
            user.setRole(jsonObject.getStr("role"));
        }

        if (jsonObject.containsKey("dept_id")) {
            user.setDeptId(jsonObject.getInt("dept_id"));
        }
        userService.updateUserInfo(user);
        return R.ok();

    }
    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody UserSearchGroupByDeptForm form){
        ArrayList<HashMap> list=userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result",list);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody UserSearchMembersForm form){
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        List param=JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList list=userService.searchMembers(param);
        return R.ok().put("result",list);
    }

    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户姓名和头像")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R selectUserPhotoAndName(@Valid @RequestBody UserSelectPhotoAndNameForm form){
        if(!JSONUtil.isJsonArray(form.getIds())){
            throw new EmosException("参数不是JSON数组");
        }
        List<Integer> param=JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> list=userService.selectUserPhotoAndName(param);
        return R.ok().put("result",list);
    }

    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token){
        int id=jwtUtil.getUserId(token);
        String email=userService.searchMemberEmail(id);
        TLSSigAPIv2 api=new TLSSigAPIv2(appid,key);
        String userSig=api.genUserSig(email,expire);
        return R.ok().put("userSig",userSig).put("email",email);
    }

    @PostMapping("/dimissionEmployee")
    @ApiOperation("离职")
    //@RequiresPermissions(value = {"ROOT",  "EMPLOYEE:UPDATE"}, logical = Logical.OR)
    public R dimissionEmployee(@Valid @RequestBody EmployeeDismissionForm form) {
        userService.dimissionEmployee(form.getId());
        return R.ok().put("result", "success");
    }

}
