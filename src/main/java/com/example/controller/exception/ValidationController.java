package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


//@validated来校验数据 如果数据异常则会统一抛出异常，方便异常中心统一处理。
//使用自己的异常处理器
@Slf4j
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException exception){
        log.warn("Resolve [{} : {}]",exception.getClass().getName(),exception.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }
}
