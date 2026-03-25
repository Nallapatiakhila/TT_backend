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


package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.OtpService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final OtpService otpService;

    public AuthController(UserRepository userRepository, OtpService otpService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    // ====================== REGISTER ======================
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String name = body.get("name");
            String email = body.get("email");
            String phone = body.get("phone");
            String password = body.get("password");

            if (name == null || email == null || password == null) {
                response.put("message", "Name, email and password are required");
                return response;
            }

            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                response.put("message", "Email already registered");
                return response;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPassword(password);   // Note: In production, hash the password!

            userRepository.save(user);

            boolean otpSent = otpService.sendOtp(email);

            if (otpSent) {
                response.put("message", "Account created successfully. OTP sent to your email.");
                response.put("success", true);
            } else {
                response.put("message", "Account created, but failed to send OTP. Please try login to resend OTP.");
                response.put("success", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("success", false);
        }

        return response;
    }

    // ====================== LOGIN ======================
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || password == null) {
                response.put("message", "Email and password are required");
                return response;
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                response.put("message", "User not found");
                return response;
            }

            if (!user.getPassword().equals(password)) {
                response.put("message", "Invalid password");
                return response;
            }

            boolean otpSent = otpService.sendOtp(email);

            if (otpSent) {
                response.put("message", "OTP sent successfully to your email");
                response.put("success", true);
                response.put("email", email);   // helpful for frontend
            } else {
                response.put("message", "Login successful, but failed to send OTP. Please try again.");
                response.put("success", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Login failed");
            response.put("success", false);
        }

        return response;
    }

    // ====================== VERIFY OTP ======================
    @PostMapping("/verify-otp")
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = body.get("email");
            String otp = body.get("otp");

            if (email == null || otp == null) {
                response.put("message", "Email and OTP are required");
                return response;
            }

            boolean isValid = otpService.verifyOtp(email, otp);

            if (isValid) {
                response.put("message", "OTP verified successfully");
                response.put("success", true);
                // TODO: In future, generate JWT token here and return it
            } else {
                response.put("message", "Invalid or expired OTP");
                response.put("success", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "OTP verification failed");
            response.put("success", false);
        }

        return response;
    }

    // Optional: Get all users (for testing only)
    @GetMapping("/users")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
