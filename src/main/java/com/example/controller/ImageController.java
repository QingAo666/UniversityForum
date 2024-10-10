package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("api/image")
public class ImageController {

    @Resource
    ImageService service;

    //帖子图片上传
    @PostMapping("/cache")
    public RestBean<String> uploadImage(@RequestParam MultipartFile file,
                                        @RequestAttribute(Const.ATTR_USER_ID) int id,
                                        HttpServletResponse response) throws IOException {

        if(file.getSize() > 1024 * 1024 * 50)
            return RestBean.failure(400,"帖子图片大小不能超过5MB");
        log.info("正在进行帖子图片上传操作");
        String url = service.uploadImage(file,id);
        if(url != null){
            log.info("帖子图片上传成功，大小为："+file.getSize());
            return RestBean.success(url);
        } else {
            response.setStatus(400);
            return RestBean.failure(400,"帖子照片上传失败，请联系管理员");
        }
    }

    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam MultipartFile file,
                                   @RequestAttribute(Const.ATTR_USER_ID) int id) throws IOException {

        if(file.getSize() > 1024 * 100)
            return RestBean.failure(400,"头像大小不能超过100KB");
        log.info("正在进行头像上传操作");
        String url = service.uploadAvatar(file,id);
        if(url != null){
            log.info("头像上传成功，大小为："+file.getSize());
            return RestBean.success(url);
        } else {
            return RestBean.failure(400,"头像上传失败，请联系管理员");
        }
    }
}
