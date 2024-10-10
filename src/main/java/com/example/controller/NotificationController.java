package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Notification;
import com.example.entity.vo.response.NotificationVo;
import com.example.service.NotificationService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    public RestBean<List<NotificationVo>> listNotification(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(service.findUserNotification(id));
    }

    @GetMapping("/delete")
    public RestBean<List<NotificationVo>> deleteNotification(@RequestParam @Min(0) int id,
                                                             @RequestAttribute(Const.ATTR_USER_ID) int uid){
        service.deleteUserNotification(id,uid);
        return RestBean.success();
    }

    @GetMapping("/delete-all")
    public RestBean<List<NotificationVo>> deleteAllNotification(@RequestAttribute(Const.ATTR_USER_ID) int uid){
        service.deleteUserAllNotification(uid);
        return RestBean.success();
    }

}
