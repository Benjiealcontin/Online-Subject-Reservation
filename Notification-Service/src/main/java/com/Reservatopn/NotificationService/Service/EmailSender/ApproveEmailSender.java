package com.Reservatopn.NotificationService.Service.EmailSender;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class ApproveEmailSender {
    private final JavaMailSender mailSender;

    private final Configuration config;

    public ApproveEmailSender(JavaMailSender mailSender, Configuration config) {
        this.mailSender = mailSender;
        this.config = config;
    }

    public void approveEmailSender(String email, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Template t1 = config.getTemplate("approve-template.ftl");
            String reserveHtml = FreeMarkerTemplateUtils.processTemplateIntoString(t1, model);

            helper.setTo(email);
            helper.setText(reserveHtml, true);
            helper.setSubject("Approve Notification");
            helper.setFrom("benjiealcontin23@gmail.com");
            mailSender.send(message);
            System.out.println("Approve Mail Sent for Student is successfully");
        } catch (MessagingException | TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
