package com.volunteer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 阿里云短信服务封装，仅用于发送重置密码验证码。
 *
 * 使用前请在 application.yml 中配置 aliyun.sms 相关字段。
 */
@Service
public class AliyunSmsService {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.region-id:cn-hangzhou}")
    private String regionId;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    private boolean isConfigured() {
        return notBlank(accessKeyId)
                && notBlank(accessKeySecret)
                && notBlank(signName)
                && notBlank(templateCode);
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 发送重置密码验证码短信
     */
    public void sendResetCode(String phone, String code) {
        if (!notBlank(phone)) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!isConfigured()) {
            throw new RuntimeException("短信服务未配置，请联系管理员");
        }
    }
}
