package com.liujie.emos.wx.controller;

import com.liujie.emos.wx.common.util.R;
import com.liujie.emos.wx.config.shiro.JwtUtil;

import com.liujie.emos.wx.controller.form.MessageDeleteRefByIdForm;
import com.liujie.emos.wx.controller.form.MessageSearchByIdForm;
import com.liujie.emos.wx.controller.form.MessageSearchByPageForm;
import com.liujie.emos.wx.controller.form.MessageUpdateUnreadForm;
import com.liujie.emos.wx.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/message")
@Api("消息模块网络接口")
public class MessageController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;


    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody MessageSearchByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody MessageSearchByIdForm form) {
        HashMap map = messageService.searchMessageById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUnreadMessage")
    @ApiOperation("未读消息更新成已读消息")
    public R updateUnreadMessage(@Valid @RequestBody MessageUpdateUnreadForm form) {
        long rows = messageService.updateUnreadMessage(form.getId());
        return R.ok().put("result", rows == 1 ? true : false);
    }

    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public R deleteMessageRefById(@Valid @RequestBody MessageDeleteRefByIdForm form) {
        long rows = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result", rows == 1 ? true : false);
    }

    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户消息")
    public R refreshMessage(@RequestHeader("token") String token){
        /*int userId=jwtUtil.getUserId(token);
        messageTask.receiveAsync(userId+"");
        long lastRows=messageService.searchLastCount(userId);
        long unreadRows=messageService.searchUnreadCount(userId);*/
        return R.ok().put("lastRows",0).put("unreadRows",0);
    }

}
