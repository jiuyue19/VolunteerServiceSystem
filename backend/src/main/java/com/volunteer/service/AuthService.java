package com.volunteer.service;

import com.volunteer.entity.Admin;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.AdminMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired(required = false)
    private AliyunSmsService aliyunSmsService;
    
    // 发件人地址，使用 spring.mail.username
    @Value("${spring.mail.username:}")
    private String mailFrom;

    // 忘记密码验证码缓存（key: type:identifier, value: code）
    private final Map<String, String> resetCodeCache = new ConcurrentHashMap<>();
    
    private String buildResetKey(String type, String identifier) {
        return type + ":" + identifier;
    }
    
    // 生成6位数字验证码
    private String generateNumericCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    public String adminLogin(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 兼容明文密码和BCrypt加密密码
        boolean passwordMatch = admin.getPassword().startsWith("$2a$")
            ? passwordEncoder.matches(password, admin.getPassword())
            : password.equals(admin.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return jwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), admin.getRole(), "admin");
    }
    
    public String volunteerLogin(String username, String password) {
        Volunteer volunteer = volunteerMapper.selectByUsername(username);
        if (volunteer == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 兼容明文密码和BCrypt加密密码
        boolean passwordMatch = volunteer.getPassword().startsWith("$2a$")
            ? passwordEncoder.matches(password, volunteer.getPassword())
            : password.equals(volunteer.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (volunteer.getStatus() != null && volunteer.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return jwtUtil.generateAccessToken(volunteer.getId(), volunteer.getUsername(), "VOLUNTEER", "volunteer");
    }
    
    public void volunteerRegister(Volunteer volunteer) {
        if (volunteerMapper.selectByUsername(volunteer.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        volunteer.setPassword(passwordEncoder.encode(volunteer.getPassword()));
        volunteer.setStatus(1);
        volunteer.setPoints(0);
        volunteer.setCertificationStatus(0);
        volunteerMapper.insert(volunteer);
    }

    /**
     * 发送志愿者忘记密码验证码（通过手机号或邮箱）
     * @return 返回验证码（仅用于开发调试，生产环境不应返回）
     */
    public String sendVolunteerResetCode(String type, String phone, String email) {
        if (!"phone".equals(type) && !"email".equals(type)) {
            throw new RuntimeException("不支持的找回方式");
        }
        Volunteer volunteer;
        String identifier;
        if ("phone".equals(type)) {
            if (phone == null || phone.trim().isEmpty()) {
                throw new RuntimeException("手机号不能为空");
            }
            volunteer = volunteerMapper.selectByPhone(phone);
            identifier = phone;
        } else {
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("邮箱不能为空");
            }
            volunteer = volunteerMapper.selectByEmail(email);
            identifier = email;
        }
        if (volunteer == null) {
            throw new RuntimeException("用户不存在");
        }
        String code = generateNumericCode();
        resetCodeCache.put(buildResetKey(type, identifier), code);
        // 通过邮箱发送验证码
        if ("email".equals(type)) {
            if (mailSender == null) {
                throw new RuntimeException("邮件服务未配置，请联系管理员");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isEmpty()) {
                message.setFrom(mailFrom);
            }
            message.setTo(email);
            message.setSubject("志愿者服务系统密码重置验证码");
            message.setText("您的验证码是：" + code + "，用于重置密码，请在10分钟内完成操作。");
            mailSender.send(message);
        }
        // 通过阿里云短信发送验证码（手机号方式）
        if ("phone".equals(type)) {
            if (aliyunSmsService == null) {
                throw new RuntimeException("短信服务未配置，请联系管理员");
            }
            try {
                aliyunSmsService.sendResetCode(phone, code);
            } catch (Exception e) {
                throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
            }
        }
        return code;
    }

    /**
     * 通过验证码重置志愿者密码
     */
    public void resetVolunteerPasswordByCode(String type, String phone, String email, String code, String newPassword) {
        if (!"phone".equals(type) && !"email".equals(type)) {
            throw new RuntimeException("不支持的找回方式");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        Volunteer volunteer;
        String identifier;
        if ("phone".equals(type)) {
            if (phone == null || phone.trim().isEmpty()) {
                throw new RuntimeException("手机号不能为空");
            }
            volunteer = volunteerMapper.selectByPhone(phone);
            identifier = phone;
        } else {
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("邮箱不能为空");
            }
            volunteer = volunteerMapper.selectByEmail(email);
            identifier = email;
        }
        if (volunteer == null) {
            throw new RuntimeException("用户不存在");
        }

        String key = buildResetKey(type, identifier);
        String cachedCode = resetCodeCache.get(key);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        volunteerMapper.updatePassword(volunteer.getId(), encodedPassword);
        resetCodeCache.remove(key);
    }

    /**
     * 志愿者通过当前密码修改密码
     */
    public void changeVolunteerPassword(Long userId, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new RuntimeException("当前密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        Volunteer volunteer = volunteerMapper.selectById(userId);
        if (volunteer == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验当前密码（兼容明文和BCrypt加密）
        boolean passwordMatch = volunteer.getPassword() != null && volunteer.getPassword().startsWith("$2a$")
            ? passwordEncoder.matches(currentPassword, volunteer.getPassword())
            : currentPassword.equals(volunteer.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("当前密码错误");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        volunteerMapper.updatePassword(userId, encodedPassword);
    }
}