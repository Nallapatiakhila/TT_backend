// package com.example.backend.controller;

// import com.example.backend.entity.User;
// import com.example.backend.repository.UserRepository;
// import com.example.backend.service.OtpService;

// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.List;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {

//     private final UserRepository userRepository;
//     private final OtpService otpService;

//     public AuthController(UserRepository userRepository, OtpService otpService) {
//         this.userRepository = userRepository;
//         this.otpService = otpService;
//     }

//     // REGISTER USER
//     @PostMapping("/register")
//     public Map<String, Object> register(@RequestBody Map<String, String> body) {

//         String name = body.get("name");
//         String email = body.get("email");
//         String phone = body.get("phone");
//         String password = body.get("password");

//         Map<String, Object> response = new HashMap<>();

//         try {

//             User existingUser = userRepository.findByEmail(email);

//             if (existingUser != null) {
//                 response.put("message", "Email already registered");
//                 return response;
//             }

//             User user = new User();
//             user.setName(name);
//             user.setEmail(email);
//             user.setPhone(phone);
//             user.setPassword(password);

//             userRepository.save(user);

//             // send OTP
//             otpService.sendOtp(email);

//             response.put("message", "OTP sent successfully");

//         } catch (Exception e) {

//             e.printStackTrace();
//             response.put("message", "Registration failed. Please try again.");

//         }

//         return response;
//     }


//     // LOGIN USER
//     @PostMapping("/login")
//     public Map<String, Object> login(@RequestBody Map<String, String> body) {

//         String email = body.get("email");
//         String password = body.get("password");

//         Map<String, Object> response = new HashMap<>();

//         try {

//             User user = userRepository.findByEmail(email);

//             if (user == null) {
//                 response.put("message", "User not found");
//                 return response;
//             }

//             if (!user.getPassword().equals(password)) {
//                 response.put("message", "Invalid password");
//                 return response;
//             }

//             // send OTP
//             otpService.sendOtp(email);

//             response.put("message", "OTP sent successfully");

//         } catch (Exception e) {

//             e.printStackTrace();
//             response.put("message", "Login failed. Please try again.");

//         }

//         return response;
//     }


//     // VERIFY OTP
//     @PostMapping("/verify-otp")
//     public Map<String,Object> verifyOtp(@RequestBody Map<String,String> body){

//         String email = body.get("email");
//         String otp = body.get("otp");

//         Map<String,Object> response = new HashMap<>();

//         try {

//             boolean isValid = otpService.verifyOtp(email, otp);

//             if(isValid){
//                 response.put("message","OTP verified successfully");
//             } else {
//                 response.put("message","OTP verification failed");
//             }

//         } catch(Exception e){

//             e.printStackTrace();
//             response.put("message","OTP verification error");

//         }

//         return response;
//     }


//     // DEBUG ENDPOINT (optional)
//     @GetMapping("/users")
//     public List<User> getAllUsers() {
//         return userRepository.findAll();
//     }
// }


package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, String> otpStorage = new HashMap<>();

    @Async
    public void sendOtp(String email) {

        try {
            String otp = String.valueOf(new Random().nextInt(900000) + 100000);

            otpStorage.put(email, otp);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("SmartPlan AI - OTP Verification");
            message.setText("Your OTP is: " + otp);

            mailSender.send(message);

            System.out.println("OTP sent to " + email + " : " + otp);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Email failed but app continues");
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
