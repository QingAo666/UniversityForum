package com.example.entity.vo.response;

import lombok.Data;

@Data
public class AccountVo {
    String username;
    String password;
    String role;
    String registerTime;
}
