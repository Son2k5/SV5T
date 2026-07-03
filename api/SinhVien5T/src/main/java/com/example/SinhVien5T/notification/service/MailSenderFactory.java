package com.example.SinhVien5T.notification.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MailSenderFactory {

    public JavaMailSender create(NotificationSettingService.RuntimeNotificationSetting setting) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(setting.smtpHost());
        sender.setPort(setting.smtpPort());
        sender.setUsername(setting.smtpUsername());
        sender.setPassword(setting.smtpPassword());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        return sender;
    }
}
