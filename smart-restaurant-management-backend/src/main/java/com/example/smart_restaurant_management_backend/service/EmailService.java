package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送会员充值成功邮件
     * @param memberName 会员姓名
     * @param memberEmail 会员邮箱
     * @param rechargeAmount 充值金额
     * @param currentBalance 当前余额
     */
    public void sendRechargeSuccessEmail(String memberName, String memberEmail, 
                                       BigDecimal rechargeAmount, BigDecimal currentBalance) {
        try {
            logger.info("开始发送邮件给: {}", memberEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("小杜的咖啡馆 - 充值成功通知");
            
            // 创建HTML邮件内容
            String htmlContent = createRechargeEmailTemplate(memberName, rechargeAmount, currentBalance);
            helper.setText(htmlContent, true);
            
            logger.info("邮件内容准备完成，开始发送...");
            mailSender.send(message);
            logger.info("充值成功邮件已发送至: {}", memberEmail);
            
        } catch (MessagingException e) {
            logger.error("发送邮件失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("邮件发送过程中出现未知错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建充值成功邮件模板
     */
    private String createRechargeEmailTemplate(String memberName, BigDecimal rechargeAmount, BigDecimal currentBalance) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String currentTime = sdf.format(new Date());
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                "        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }" +
                "        .amount { font-size: 24px; font-weight: bold; color: #4CAF50; text-align: center; margin: 20px 0; }" +
                "        .info-table { width: 100%; border-collapse: collapse; margin: 20px 0; }" +
                "        .info-table td { padding: 10px; border-bottom: 1px solid #ddd; }" +
                "        .info-table .label { font-weight: bold; width: 30%; }" +
                "        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>☕ 小杜的咖啡馆</h1>" +
                "            <h2>充值成功通知</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>尊敬的 <strong>" + memberName + "</strong> 会员，您好！</p>" +
                "            <p>您的账户充值已成功完成，以下是本次充值的详细信息：</p>" +
                "            <div class='amount'>充值金额：¥" + rechargeAmount.toString() + "</div>" +
                "            <table class='info-table'>" +
                "                <tr>" +
                "                    <td class='label'>充值时间：</td>" +
                "                    <td>" + currentTime + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td class='label'>充值金额：</td>" +
                "                    <td style='color: #4CAF50; font-weight: bold;'>¥" + rechargeAmount.toString() + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td class='label'>当前余额：</td>" +
                "                    <td style='color: #2196F3; font-weight: bold;'>¥" + currentBalance.toString() + "</td>" +
                "                </tr>" +
                "            </table>" +
                "            <p>感谢您对我们咖啡店的支持！如有任何疑问，请随时联系我们。</p>" +
                "            <div class='footer'>" +
                "                <p>此邮件由系统自动发送，请勿回复</p>" +
                "                <p>小杜的咖啡馆 © 2024</p>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * 发送会员消费成功邮件
     */
    public void sendConsumeSuccessEmail(Member member, BigDecimal consumeAmount, 
                                       String tableName, List<String> consumeItems, 
                                       Integer totalItems) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);  // 使用配置的邮箱地址，而不是硬编码
            helper.setTo(member.getEmail());
            helper.setSubject("☕ 消费成功通知 - " + member.getName());
            
            // 构建消费项目列表HTML
            StringBuilder itemsHtml = new StringBuilder();
            for (String item : consumeItems) {
                itemsHtml.append("<li style='margin: 5px 0; color: #666;'>")
                        .append(item)
                        .append("</li>");
            }
            
            String htmlContent = 
                "<div style='max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;'>" +
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0; font-size: 28px;'>☕ 消费成功</h1>" +
                "<p style='margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;'>感谢您的光临！</p>" +
                "</div>" +
                "<div style='background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;'>" +
                "<p style='font-size: 18px; color: #333; margin-bottom: 25px;'>尊贵的会员 <strong>" + member.getName() + "</strong>，您好！</p>" +
                "<p style='color: #666; margin-bottom: 25px;'>您刚刚在我们咖啡馆完成了一次消费，以下是本次消费详情：</p>" +
                "<table style='width: 100%; border-collapse: collapse; margin: 20px 0; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "<tr style='background-color: #667eea; color: white;'>" +
                "<td style='padding: 15px; font-weight: bold;'>消费信息</td>" +
                "<td style='padding: 15px; font-weight: bold;'>详情</td>" +
                "</tr>" +
                "<tr style='border-bottom: 1px solid #eee;'>" +
                "<td style='padding: 12px 15px; color: #666;'>消费桌位</td>" +
                "<td style='padding: 12px 15px; font-weight: bold; color: #333;'>" + tableName + "</td>" +
                "</tr>" +
                "<tr style='border-bottom: 1px solid #eee;'>" +
                "<td style='padding: 12px 15px; color: #666;'>消费金额</td>" +
                "<td style='padding: 12px 15px; font-weight: bold; color: #e74c3c; font-size: 18px;'>¥" + consumeAmount + "</td>" +
                "</tr>" +
                "<tr style='border-bottom: 1px solid #eee;'>" +
                "<td style='padding: 12px 15px; color: #666;'>消费项目</td>" +
                "<td style='padding: 12px 15px; color: #333;'>共 " + totalItems + " 项</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 12px 15px; color: #666;'>剩余余额</td>" +
                "<td style='padding: 12px 15px; font-weight: bold; color: #27ae60; font-size: 18px;'>¥" + member.getBalance() + "</td>" +
                "</tr>" +
                "</table>" +
                "<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "<h3 style='color: #333; margin-bottom: 15px; border-bottom: 2px solid #667eea; padding-bottom: 10px;'>🍽️ 本次消费项目</h3>" +
                "<ul style='list-style: none; padding: 0; margin: 0;'>" +
                itemsHtml.toString() +
                "</ul>" +
                "</div>" +
                "<div style='text-align: center; margin-top: 30px; padding: 20px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); border-radius: 8px; color: white;'>" +
                "<p style='margin: 0; font-size: 16px;'>感谢您选择小杜的咖啡馆！</p>" +
                "<p style='margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;'>期待您的下次光临 ☕</p>" +
                "</div>" +
                "<p style='color: #999; font-size: 12px; text-align: center; margin-top: 20px;'>此邮件由系统自动发送，请勿回复</p>" +
                "</div>" +
                "</div>";
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            logger.info("消费成功邮件已发送给会员: {} ({})", member.getName(), member.getEmail());
            
        } catch (Exception e) {
            logger.error("发送消费成功邮件失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送新会员欢迎邮件
     * @param memberName 会员姓名
     * @param memberEmail 会员邮箱
     * @param memberLevel 会员等级
     */
    public void sendWelcomeEmail(String memberName, String memberEmail, String memberLevel) {
        try {
            logger.info("开始发送欢迎邮件给: {}", memberEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(memberEmail);
            helper.setSubject("🎉 欢迎加入小杜的咖啡馆会员！");
            
            // 创建HTML邮件内容，使用当前时间作为加入日期
            String htmlContent = createWelcomeEmailTemplate(memberName, memberLevel, new Date());
            helper.setText(htmlContent, true);
            
            logger.info("欢迎邮件内容准备完成，开始发送...");
            mailSender.send(message);
            logger.info("欢迎邮件已发送至: {}", memberEmail);
            
        } catch (MessagingException e) {
            logger.error("发送欢迎邮件失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("欢迎邮件发送过程中出现未知错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建欢迎邮件模板
     */
    private String createWelcomeEmailTemplate(String memberName, String memberLevel, Date joinDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String joinDateStr = sdf.format(joinDate);
        
        // 根据会员等级设置不同的颜色和特权说明
        String levelColor = "#4CAF50";
        String levelBenefits = "";
        
        switch (memberLevel) {
            case "银卡会员":
                levelColor = "#9E9E9E";
                levelBenefits = "<li>享受95折优惠</li><li>生日当月免费饮品一杯</li><li>积分双倍累积</li>";
                break;
            case "金卡会员":
                levelColor = "#FFD700";
                levelBenefits = "<li>享受9折优惠</li><li>每月免费饮品两杯</li><li>积分三倍累积</li><li>专属客服服务</li>";
                break;
            case "钻石会员":
                levelColor = "#E91E63";
                levelBenefits = "<li>享受85折优惠</li><li>每月免费饮品三杯</li><li>积分五倍累积</li><li>VIP专属座位</li><li>新品优先体验</li>";
                break;
            default: // 普通会员
                levelColor = "#4CAF50";
                levelBenefits = "<li>积分累积享优惠</li><li>会员专属活动通知</li><li>生日祝福及小礼品</li>";
                break;
        }
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                "        .container { max-width: 650px; margin: 0 auto; background: #fff; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 32px; font-weight: bold; }" +
                "        .header p { margin: 10px 0 0 0; font-size: 18px; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; background: #f8f9fa; }" +
                "        .welcome-card { background: white; border-radius: 12px; padding: 30px; margin-bottom: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }" +
                "        .member-info { background: linear-gradient(135deg, " + levelColor + " 0%, " + levelColor + "CC 100%); color: white; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }" +
                "        .member-level { font-size: 24px; font-weight: bold; margin-bottom: 10px; }" +
                "        .join-date { font-size: 16px; opacity: 0.9; }" +
                "        .benefits { background: white; border-radius: 8px; padding: 25px; margin: 20px 0; border-left: 4px solid " + levelColor + "; }" +
                "        .benefits h3 { color: " + levelColor + "; margin-top: 0; font-size: 20px; }" +
                "        .benefits ul { margin: 15px 0; padding-left: 20px; }" +
                "        .benefits li { margin: 8px 0; color: #555; }" +
                "        .cta-section { text-align: center; margin: 30px 0; }" +
                "        .cta-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; }" +
                "        .footer { background: #2c3e50; color: white; padding: 25px 30px; text-align: center; }" +
                "        .footer p { margin: 5px 0; }" +
                "        .contact-info { background: #34495e; padding: 15px; border-radius: 8px; margin-top: 15px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🎉 欢迎加入我们！</h1>" +
                "            <p>咖啡管理系统会员大家庭</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='welcome-card'>" +
                "                <h2 style='color: #2c3e50; margin-top: 0;'>亲爱的 " + memberName + "，</h2>" +
                "                <p style='font-size: 16px; color: #555; line-height: 1.8;'>" +
                "                    非常感谢您选择加入我们的咖啡管理系统！我们很高兴能为您提供优质的咖啡体验和贴心的会员服务。" +
                "                    从今天开始，您将享受到专属的会员权益和个性化服务。" +
                "                </p>" +
                "            </div>" +
                "            <div class='member-info'>" +
                "                <div class='member-level'>🏆 " + memberLevel + "</div>" +
                "                <div class='join-date'>加入时间：" + joinDateStr + "</div>" +
                "            </div>" +
                "            <div class='benefits'>" +
                "                <h3>🎁 您的专属权益</h3>" +
                "                <ul>" +
                levelBenefits +
                "                </ul>" +
                "            </div>" +
                "            <div class='cta-section'>" +
                "                <p style='font-size: 18px; color: #2c3e50; margin-bottom: 20px;'>立即开始您的咖啡之旅！</p>" +
                "                <a href='#' class='cta-button'>探索更多优惠</a>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p style='font-size: 18px; font-weight: bold;'>☕ 咖啡管理系统</p>" +
                "            <p>为您提供最优质的咖啡体验</p>" +
                "            <div class='contact-info'>" +
                "                <p>📞 客服热线：暂无</p>" +
                "                <p>📧 邮箱：暂无</p>" +
                "                <p>🕒 营业时间：周一至周日 7:00-22:00</p>" +
                "            </div>" +
                "            <p style='margin-top: 20px; font-size: 12px; opacity: 0.8;'>此邮件由系统自动发送，请勿回复</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}