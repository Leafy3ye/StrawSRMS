package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.Member;
import com.example.smart_restaurant_management_backend.model.TenantEmailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class TenantEmailService {

    private static final Logger logger = LoggerFactory.getLogger(TenantEmailService.class);

    @Autowired
    private JavaMailSender defaultMailSender;
    
    @Autowired
    private TenantEmailConfigService tenantEmailConfigService;

    @Value("${spring.mail.username}")
    private String defaultFromEmail;

    /**
     * 获取租户的邮件发送器，如果租户没有配置则使用默认配置
     * @param tenantId 租户ID
     * @return JavaMailSender
     */
    private JavaMailSender getMailSender(Long tenantId) {
        if (tenantId == null) {
            return defaultMailSender;
        }
        
        TenantEmailConfig config = tenantEmailConfigService.getTenantEmailConfig(tenantId);
        if (config == null || !config.isEnabled()) {
            return defaultMailSender;
        }
        
        // 创建租户专用的邮件发送器
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpHost());
        mailSender.setPort(config.getSmtpPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        
        if (config.getSmtpPort() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        
        return mailSender;
    }
    
    /**
     * 获取发件人邮箱地址
     * @param tenantId 租户ID
     * @return 发件人邮箱
     */
    private String getFromEmail(Long tenantId) {
        if (tenantId == null) {
            return defaultFromEmail;
        }
        
        TenantEmailConfig config = tenantEmailConfigService.getTenantEmailConfig(tenantId);
        if (config == null || !config.isEnabled()) {
            return defaultFromEmail;
        }
        
        return config.getUsername();
    }

    /**
     * 发送欢迎邮件
     * @param memberName 会员姓名
     * @param memberEmail 会员邮箱
     * @param memberLevel 会员等级
     * @param tenantId 租户ID
     */
    public void sendWelcomeEmail(String memberName, String memberEmail, String memberLevel, Long tenantId) {
        try {
            logger.info("开始发送欢迎邮件给: {} (租户ID: {})", memberEmail, tenantId);
            
            JavaMailSender mailSender = getMailSender(tenantId);
            String fromEmail = getFromEmail(tenantId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("🎉 欢迎加入会员大家庭！");
            
            String htmlContent = createWelcomeEmailTemplate(memberName, memberLevel, new Date(), tenantId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("欢迎邮件已发送至: {} (租户ID: {})", memberEmail, tenantId);
            
        } catch (Exception e) {
            logger.error("发送欢迎邮件失败 (租户ID: {}): {}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 发送充值成功邮件
     * @param memberName 会员姓名
     * @param memberEmail 会员邮箱
     * @param rechargeAmount 充值金额
     * @param currentBalance 当前余额
     * @param tenantId 租户ID
     */
    public void sendRechargeSuccessEmail(String memberName, String memberEmail, 
                                       BigDecimal rechargeAmount, BigDecimal currentBalance, Long tenantId) {
        try {
            logger.info("开始发送充值成功邮件给: {} (租户ID: {})", memberEmail, tenantId);
            
            JavaMailSender mailSender = getMailSender(tenantId);
            String fromEmail = getFromEmail(tenantId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("💰 充值成功通知");
            
            String htmlContent = createRechargeEmailTemplate(memberName, rechargeAmount, currentBalance, tenantId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("充值成功邮件已发送至: {} (租户ID: {})", memberEmail, tenantId);
            
        } catch (Exception e) {
            logger.error("发送充值成功邮件失败 (租户ID: {}): {}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 发送消费成功邮件
     * @param member 会员信息
     * @param consumeAmount 消费金额
     * @param tableName 桌位名称
     * @param consumeItems 消费项目
     * @param totalItems 总项目数
     * @param tenantId 租户ID
     */
    public void sendConsumeSuccessEmail(Member member, BigDecimal consumeAmount, 
                                       String tableName, List<String> consumeItems, 
                                       Integer totalItems, Long tenantId) {
        try {
            logger.info("开始发送消费成功邮件给: {} (租户ID: {})", member.getEmail(), tenantId);
            
            JavaMailSender mailSender = getMailSender(tenantId);
            String fromEmail = getFromEmail(tenantId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(member.getEmail());
            helper.setSubject("🍽️ 消费成功通知");
            
            String htmlContent = createConsumeEmailTemplate(member, consumeAmount, tableName, consumeItems, totalItems, tenantId);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("消费成功邮件已发送至: {} (租户ID: {})", member.getEmail(), tenantId);
            
        } catch (Exception e) {
            logger.error("发送消费成功邮件失败 (租户ID: {}): {}", tenantId, e.getMessage(), e);
        }
    }

    // 邮件模板方法（简化版，您可以根据需要自定义）
    private String createWelcomeEmailTemplate(String memberName, String memberLevel, Date joinDate, Long tenantId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String joinDateStr = sdf.format(joinDate);
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h1 style='color: #4CAF50;'>🎉 欢迎加入会员！</h1>" +
                "<p>尊敬的 <strong>" + memberName + "</strong>，</p>" +
                "<p>感谢您成为我们的 <strong>" + memberLevel + "</strong>！</p>" +
                "<p>加入时间：" + joinDateStr + "</p>" +
                "<p>期待为您提供优质的服务！</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String createRechargeEmailTemplate(String memberName, BigDecimal rechargeAmount, BigDecimal currentBalance, Long tenantId) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h1 style='color: #4CAF50;'>💰 充值成功</h1>" +
                "<p>尊敬的 <strong>" + memberName + "</strong>，</p>" +
                "<p>您的充值已成功完成：</p>" +
                "<p>充值金额：<strong style='color: #4CAF50;'>¥" + rechargeAmount + "</strong></p>" +
                "<p>当前余额：<strong style='color: #2196F3;'>¥" + currentBalance + "</strong></p>" +
                "<p>感谢您的支持！</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String createConsumeEmailTemplate(Member member, BigDecimal consumeAmount, String tableName, List<String> consumeItems, Integer totalItems, Long tenantId) {
        StringBuilder itemsHtml = new StringBuilder();
        if (consumeItems != null) {
            for (String item : consumeItems) {
                itemsHtml.append("<li>").append(item).append("</li>");
            }
        }
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h1 style='color: #FF9800;'>🍽️ 消费成功</h1>" +
                "<p>尊敬的 <strong>" + member.getName() + "</strong>，</p>" +
                "<p>您刚刚完成了一次消费：</p>" +
                "<p>消费桌位：<strong>" + tableName + "</strong></p>" +
                "<p>消费金额：<strong style='color: #FF9800;'>¥" + consumeAmount + "</strong></p>" +
                "<p>剩余余额：<strong style='color: #4CAF50;'>¥" + member.getBalance() + "</strong></p>" +
                "<p>消费项目：</p>" +
                "<ul>" + itemsHtml.toString() + "</ul>" +
                "<p>感谢您的光临！</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}