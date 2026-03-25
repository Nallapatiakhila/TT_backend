// package com.example.backend.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Random;

// @Service
// public class OtpService {

//     @Autowired
//     private JavaMailSender mailSender;

//     // store OTP temporarily
//     private Map<String, String> otpStorage = new HashMap<>();

//     public void sendOtp(String email) {

//         String otp = String.valueOf(new Random().nextInt(900000) + 100000);

//         otpStorage.put(email, otp);

//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setTo(email);
//         message.setSubject("SmartPlan AI - OTP Verification");
//         message.setText("Your OTP is: " + otp);

//         mailSender.send(message);
//     }

//     public boolean verifyOtp(String email, String otp) {

//         String storedOtp = otpStorage.get(email);

//         if (storedOtp != null && storedOtp.equals(otp)) {
//             otpStorage.remove(email);
//             return true;
//         }

//         return false;
//     }
// }

package com.example.backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${RESEND_FROM_EMAIL:no-reply@smartplanai.com}")
    private String fromEmail;

    // Store OTP temporarily (email -> otp)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public boolean sendOtp(String email) {
        String otp = String.valueOf(RANDOM.nextInt(900000) + 100000);
        otpStorage.put(email, otp);

        try {
            Resend resend = new Resend(resendApiKey);

            SendEmailRequest request = SendEmailRequest.builder()
                    .from(fromEmail)
                    .to(email)
                    .subject("SmartPlan AI - Your Login OTP")
                    .html("""
                        <p>Hello,</p>
                        <p>Your OTP for signing into <strong>SmartPlan AI</strong> is: <b>%s</b></p>
                        <p>This OTP is valid for 10 minutes.</p>
                        <p>If you did not request this, please ignore this email.</p>
                        """.formatted(otp))
                    .build();

            resend.emails().send(request);

            logger.info("✅ OTP sent successfully to: {}", email);
            return true;

        } catch (ResendException e) {
            otpStorage.remove(email);
            logger.error("❌ Resend failed to send OTP to {}: {}", email, e.getMessage());
            return false;
        } catch (Exception e) {
            otpStorage.remove(email);
            logger.error("Unexpected error while sending OTP to {}", email, e);
            return false;
        }
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email);
            logger.info("✅ OTP verified successfully for {}", email);
            return true;
        }
        logger.warn("Invalid OTP for {}", email);
        return false;
    }
}

