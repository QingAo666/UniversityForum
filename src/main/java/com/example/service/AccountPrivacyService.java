package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.PrivacySaveVo;

public interface AccountPrivacyService extends IService<AccountPrivacy> {
    void savePrivacy(int id, PrivacySaveVo vo);
    AccountPrivacy accountPrivacy(int id);
}
