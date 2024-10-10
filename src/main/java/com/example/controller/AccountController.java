package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.vo.request.ChangePasswordVo;
import com.example.entity.vo.request.DetailsSaveVo;
import com.example.entity.vo.request.ModifyEmailVo;
import com.example.entity.vo.request.PrivacySaveVo;
import com.example.entity.vo.response.AccountDetailsVo;
import com.example.entity.vo.response.AccountPrivacyVo;
import com.example.entity.vo.response.AccountVo;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/user")
public class AccountController {

    @Resource
    AccountService service;

    @Resource
    AccountDetailsService detailsService;

    @Resource
    AccountPrivacyService privacyService;

    @Resource
    ControllerUtils utils;

    @GetMapping("/info")
    public RestBean<AccountVo> info(@RequestAttribute(Const.ATTR_USER_ID) int id){
        Account account = service.findAccountById(id);//从数据库取出来的dto对象，所以先要转换成vo对象
        return RestBean.success(account.asViewObject(AccountVo.class));
    }

    @GetMapping("/details")
    public RestBean<AccountDetailsVo> details(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDetails details = Optional.ofNullable(detailsService.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        return RestBean.success(details.asViewObject(AccountDetailsVo.class));
    }

    @PostMapping("/save-details")
    public RestBean<Void> saveDetails(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid DetailsSaveVo vo){
        boolean success = detailsService.saveAccountDetails(id,vo);
        return success ? RestBean.success() : RestBean.failure(400,"此用户名已被其它用户使用，请重新更换");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid ModifyEmailVo vo){
        return utils.messageHandle(() -> service.modifyEmail(id,vo));
    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                         @RequestBody @Valid ChangePasswordVo vo){
        return utils.messageHandle(() -> service.changePassword(id,vo));
    }

    @PostMapping("/save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid PrivacySaveVo vo){
        privacyService.savePrivacy(id,vo);
        return RestBean.success();
    }

    @GetMapping("/privacy")
    public RestBean<AccountPrivacyVo> privacy(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(privacyService.accountPrivacy(id).asViewObject(AccountPrivacyVo.class));
    }


}
