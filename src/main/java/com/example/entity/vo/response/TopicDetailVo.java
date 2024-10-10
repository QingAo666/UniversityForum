package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class TopicDetailVo {
    Integer id;
    String title;
    String text;
    Integer type;
    Date time;
    User user;
    Interact interact;
    Long comments;

    @Data
    @AllArgsConstructor
    public static class Interact{
        Boolean like;
        Boolean collect;
    }

    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        Integer gender;
        String qq;
        String wx;
        String phone;
        String email;
    }
}
