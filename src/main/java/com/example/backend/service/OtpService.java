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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private JavaMailSender mailSender;

    // Thread-safe storage for OTPs
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    /**
     * Sends OTP to the given email
     * @return true if OTP was sent successfully, false otherwise
     */
    public boolean sendOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email cannot be null or empty");
            return false;
        }

        String otp = String.valueOf(RANDOM.nextInt(900000) + 100000); // 6-digit OTP
        otpStorage.put(email, otp);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("SmartPlan AI - Your OTP for Login");
            message.setText(
                "Hello,\n\n" +
                "Your OTP for SmartPlan AI login is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nSmartPlan AI Team"
            );

            mailSender.send(message);

            logger.info("✅ OTP sent successfully to: {}", email);
            logger.debug("OTP generated for {} is: {}", email, otp); // Remove in production
            return true;

        } catch (Exception e) {
            // Remove OTP if email failed
            otpStorage.remove(email);

            logger.error("❌ Failed to send OTP to: {}", email, e);

            // Log more details for debugging
            if (e.getMessage() != null) {
                logger.error("Error message: {}", e.getMessage());
            }
            if (e.getCause() != null) {
                logger.error("Cause: {}", e.getCause().getMessage());
            }

            return false;
        }
    }

    /**
     * Verifies the OTP for the given email
     */
    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            return false;
        }

        String storedOtp = otpStorage.get(email);

        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email);
            logger.info("✅ OTP verified successfully for: {}", email);
            return true;
        }

        logger.warn("❌ Invalid OTP attempt for: {}", email);
        return false;
    }
}

