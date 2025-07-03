package com.example.smart_restaurant_management_backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成图形验证码
     */
    public CaptchaResult generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = generateRandomCode(4);
        
        // 生成验证码图片
        String imageBase64 = generateCaptchaImage(captchaCode);
        
        // 存储到Redis，5分钟过期
        redisTemplate.opsForValue().set("captcha:" + captchaKey, captchaCode, 5, TimeUnit.MINUTES);
        
        return new CaptchaResult(captchaKey, imageBase64);
    }

    /**
     * 验证图形验证码
     */
    public boolean verifyCaptcha(String captchaKey, String userInput) {
        String storedCode = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        if (storedCode != null && storedCode.equalsIgnoreCase(userInput)) {
            // 验证成功后删除验证码
            redisTemplate.delete("captcha:" + captchaKey);
            return true;
        }
        return false;
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private String generateCaptchaImage(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        // 绘制验证码
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 20, 25);
        }
        
        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(width), random.nextInt(height), 
                      random.nextInt(width), random.nextInt(height));
        }
        
        g.dispose();
        
        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    public static class CaptchaResult {
        private String key;
        private String image;

        public CaptchaResult(String key, String image) {
            this.key = key;
            this.image = image;
        }

        public String getKey() {
            return key;
        }

        public String getImage() {
            return image;
        }
    }
}