package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ConfirmResetVo;
import com.example.entity.vo.request.EmailRegisterVo;
import com.example.entity.vo.request.EmailRestVo;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;

    @Resource
    ControllerUtils utils;

    @GetMapping("ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request){
        return utils.messageHandle(()->
            service.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }
    //get请求用requestParam
    //post请求用requestBody
    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVo vo){
        return utils.messageHandle(()->service.registerEmailAccount(vo));

    }

    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVo vo){
        return utils.messageHandle(()->service.resetConfirm(vo));

    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailRestVo vo){
        return utils.messageHandle(()->service.resetEmailAccountPassword(vo));

    }

}
