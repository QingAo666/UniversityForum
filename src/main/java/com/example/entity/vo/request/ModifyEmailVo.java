package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ModifyEmailVo {
    @Email
    String email;
    @Length(min = 6, max = 6)
    String code;
}
