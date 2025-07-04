package com.example.smart_restaurant_management_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    // Redis key前缀
    private static final String EMAIL_CODE_PREFIX = "email_code:";
    
    // 发送频率限制（秒）
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final String EMAIL_SEND_LIMIT_PREFIX = "email_send_limit:";

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @throws Exception 发送失败时抛出异常
     */
    public void sendEmailCode(String email) throws Exception {
        // 检查发送频率限制
        String limitKey = EMAIL_SEND_LIMIT_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new Exception("发送过于频繁，请稍后再试");
        }
        
        // 生成6位数字验证码
        String code = generateCode();
        
        try {
            // 发送邮件
            sendCodeEmail(email, code);
            
            // 存储验证码到Redis，设置5分钟过期
            String codeKey = EMAIL_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 设置发送频率限制，60秒内不能重复发送
            redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
            
            logger.info("验证码邮件已发送至: {}", email);
            
        } catch (Exception e) {
            logger.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new Exception("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证是否成功
     */
    public boolean verifyEmailCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        
        String codeKey = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            logger.info("邮箱验证码验证成功: {}", email);
            return true;
        }
        
        logger.warn("邮箱验证码验证失败: {}, 输入的验证码: {}", email, code);
        return false;
    }

    /**
     * 生成随机验证码
     * @return 6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送验证码邮件
     * @param email 邮箱地址
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    private void sendCodeEmail(String email, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("【智能餐厅管理系统】邮箱验证码");
        
        // 创建HTML邮件内容
        String htmlContent = createEmailCodeTemplate(code);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * 创建验证码邮件模板
     * @param code 验证码
     * @return HTML邮件内容
     */
    private String createEmailCodeTemplate(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                "        .container { max-width: 600px; margin: 0 auto; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                "        .header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; }" +
                "        .code-section { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); border-radius: 12px; padding: 30px; text-align: center; margin: 25px 0; }" +
                "        .code-title { color: white; font-size: 18px; margin-bottom: 15px; font-weight: bold; }" +
                "        .verification-code { background: white; color: #333; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 20px; border-radius: 8px; margin: 15px 0; font-family: 'Courier New', monospace; }" +
                "        .code-note { color: white; font-size: 14px; opacity: 0.9; }" +
                "        .info-box { background: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; border-radius: 0 8px 8px 0; }" +
                "        .info-box h3 { color: #667eea; margin-top: 0; font-size: 18px; }" +
                "        .info-box ul { margin: 10px 0; padding-left: 20px; }" +
                "        .info-box li { margin: 8px 0; color: #555; }" +
                "        .warning { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0; }" +
                "        .warning-icon { color: #f39c12; font-size: 18px; margin-right: 8px; }" +
                "        .footer { background: #2c3e50; color: white; padding: 25px 30px; text-align: center; }" +
                "        .footer p { margin: 5px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔐 邮箱验证</h1>" +
                "            <p>智能餐厅管理系统</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p style='font-size: 16px; color: #555; margin-bottom: 25px;'>" +
                "                您好！您正在进行邮箱验证，请使用以下验证码完成验证：" +
                "            </p>" +
                "            <div class='code-section'>" +
                "                <div class='code-title'>您的验证码</div>" +
                "                <div class='verification-code'>" + code + "</div>" +
                "                <div class='code-note'>请在5分钟内使用此验证码</div>" +
                "            </div>" +
                "            <div class='info-box'>" +
                "                <h3>📋 使用说明</h3>" +
                "                <ul>" +
                "                    <li>此验证码仅用于本次邮箱验证</li>" +
                "                    <li>验证码有效期为5分钟</li>" +
                "                    <li>请勿将验证码告知他人</li>" +
                "                    <li>如果您没有进行此操作，请忽略此邮件</li>" +
                "                </ul>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <span class='warning-icon'>⚠️</span>" +
                "                <strong>安全提醒：</strong>为了您的账户安全，请不要将验证码泄露给任何人。我们的工作人员不会主动向您索要验证码。" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p style='font-size: 18px; font-weight: bold;'>🍽️ 智能餐厅管理系统</p>" +
                "            <p>为您提供安全可靠的服务</p>" +
                "            <p style='margin-top: 15px; font-size: 12px; opacity: 0.8;'>此邮件由系统自动发送，请勿回复</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 检查邮箱是否可以发送验证码（是否在发送频率限制内）
     * @param email 邮箱地址
     * @return 是否可以发送
     */
    public boolean canSendCode(String email) {
        String limitKey = EMAIL_SEND_LIMIT_PREFIX + email;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(limitKey));
    }

    /**
     * 获取验证码剩余有效时间（秒）
     * @param email 邮箱地址
     * @return 剩余时间，-1表示验证码不存在或已过期
     */
    public long getCodeExpireTime(String email) {
        String codeKey = EMAIL_CODE_PREFIX + email;
        Long expire = redisTemplate.getExpire(codeKey, TimeUnit.SECONDS);
        return expire != null ? expire : -1;
    }

    /**
     * 删除验证码（用于清理）
     * @param email 邮箱地址
     */
    public void deleteCode(String email) {
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(codeKey);
        logger.info("已删除邮箱验证码: {}", email);
    }

    // 重置密码验证码前缀
    private static final String RESET_PASSWORD_CODE_PREFIX = "reset_password_code:";
    private static final String RESET_PASSWORD_SEND_LIMIT_PREFIX = "reset_password_send_limit:";

    /**
     * 发送重置密码验证码
     * @param email 邮箱地址
     * @throws Exception 发送失败时抛出异常
     */
    public void sendResetPasswordCode(String email) throws Exception {
        // 检查发送频率限制
        String limitKey = RESET_PASSWORD_SEND_LIMIT_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new Exception("发送过于频繁，请稍后再试");
        }
        
        // 生成6位数字验证码
        String code = generateCode();
        
        try {
            // 发送邮件
            sendResetPasswordEmail(email, code);
            
            // 存储验证码到Redis，设置5分钟过期
            String codeKey = RESET_PASSWORD_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 设置发送频率限制，60秒内不能重复发送
            redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
            
            logger.info("重置密码验证码邮件已发送至: {}", email);
            
        } catch (Exception e) {
            logger.error("发送重置密码验证码邮件失败: {}", e.getMessage(), e);
            throw new Exception("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 验证重置密码验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证是否成功
     */
    public boolean verifyResetPasswordCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        
        String codeKey = RESET_PASSWORD_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            logger.info("重置密码验证码验证成功: {}", email);
            return true;
        }
        
        logger.warn("重置密码验证码验证失败: {}, 输入的验证码: {}", email, code);
        return false;
    }

    /**
     * 发送重置密码验证码邮件
     * @param email 邮箱地址
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    private void sendResetPasswordEmail(String email, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(email);
        helper.setSubject("【智能餐厅管理系统】密码重置验证码");
        
        // 创建HTML邮件内容
        String htmlContent = createResetPasswordEmailTemplate(code);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * 创建重置密码验证码邮件模板
     * @param code 验证码
     * @return HTML邮件内容
     */
    private String createResetPasswordEmailTemplate(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                "        .container { max-width: 600px; margin: 0 auto; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }" +
                "        .header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 40px 30px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                "        .header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; }" +
                "        .code-section { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); border-radius: 12px; padding: 30px; text-align: center; margin: 25px 0; }" +
                "        .code-title { color: white; font-size: 18px; margin-bottom: 15px; font-weight: bold; }" +
                "        .verification-code { background: white; color: #333; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 20px; border-radius: 8px; margin: 15px 0; font-family: 'Courier New', monospace; }" +
                "        .code-note { color: white; font-size: 14px; opacity: 0.9; }" +
                "        .info-box { background: #f8f9fa; border-left: 4px solid #ff6b6b; padding: 20px; margin: 25px 0; border-radius: 0 8px 8px 0; }" +
                "        .info-box h3 { color: #ff6b6b; margin-top: 0; font-size: 18px; }" +
                "        .info-box ul { margin: 10px 0; padding-left: 20px; }" +
                "        .info-box li { margin: 8px 0; color: #555; }" +
                "        .warning { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0; }" +
                "        .warning-icon { color: #f39c12; font-size: 18px; margin-right: 8px; }" +
                "        .footer { background: #2c3e50; color: white; padding: 25px 30px; text-align: center; }" +
                "        .footer p { margin: 5px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔒 密码重置</h1>" +
                "            <p>智能餐厅管理系统</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p style='font-size: 16px; color: #555; margin-bottom: 25px;'>" +
                "                您好！您正在重置密码，请使用以下验证码完成密码重置：" +
                "            </p>" +
                "            <div class='code-section'>" +
                "                <div class='code-title'>密码重置验证码</div>" +
                "                <div class='verification-code'>" + code + "</div>" +
                "                <div class='code-note'>请在5分钟内使用此验证码</div>" +
                "            </div>" +
                "            <div class='info-box'>" +
                "                <h3>📋 使用说明</h3>" +
                "                <ul>" +
                "                    <li>此验证码仅用于本次密码重置</li>" +
                "                    <li>验证码有效期为5分钟</li>" +
                "                    <li>请勿将验证码告知他人</li>" +
                "                    <li>如果您没有进行此操作，请立即联系客服</li>" +
                "                </ul>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <span class='warning-icon'>⚠️</span>" +
                "                <strong>安全提醒：</strong>为了您的账户安全，请不要将验证码泄露给任何人。如果您没有申请密码重置，请忽略此邮件并立即检查账户安全。" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p style='font-size: 18px; font-weight: bold;'>🍽️ 智能餐厅管理系统</p>" +
                "            <p>为您提供安全可靠的服务</p>" +
                "            <p style='margin-top: 15px; font-size: 12px; opacity: 0.8;'>此邮件由系统自动发送，请勿回复</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}