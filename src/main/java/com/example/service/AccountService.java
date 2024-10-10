package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type,String email,String ip);

    String registerEmailAccount(EmailRegisterVo vo);

    String resetConfirm(ConfirmResetVo vo);
    String resetEmailAccountPassword(EmailRestVo vo);

    Account findAccountById(int id);

    String modifyEmail(int id, ModifyEmailVo vo);

    String changePassword(int id, ChangePasswordVo vo);

}
