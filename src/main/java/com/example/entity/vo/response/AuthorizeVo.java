package com.example.entity.vo.response;

import lombok.Data;
import java.util.Date;

@Data
public class AuthorizeVo {
    String username;
    String role;
    String token;//存放jwt令牌
    Date expire;

}
