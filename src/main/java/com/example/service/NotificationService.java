package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Notification;
import com.example.entity.vo.response.NotificationVo;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    List<NotificationVo> findUserNotification(int uid);
    void deleteUserNotification(int id,int uid);

    void deleteUserAllNotification(int uid);
    void addNotification(int uid,String title,String content,String type,String url);

}
