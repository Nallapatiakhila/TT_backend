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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public boolean sendOtp(String email) {

        String otp = String.valueOf(RANDOM.nextInt(900000) + 100000);

        otpStorage.put(email, otp);


        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("SmartPlan AI - OTP Verification");
            message.setText("Your OTP is: " + otp);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            otpStorage.remove(email);
            logger.error("Failed to send OTP email to {}", email, e);
            return false;
        }
    }

    public boolean verifyOtp(String email, String otp) {

        String storedOtp = otpStorage.get(email);

        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }

        return false;
    }
}

