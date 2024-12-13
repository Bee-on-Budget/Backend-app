// package com.mis.onTime.AuthController;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.bind.annotation.*;

// import com.mis.onTime.Config.TokenProvider;
// import com.mis.onTime.Modal.User;
// import com.mis.onTime.Service.UserService;
// import com.restfb.DefaultFacebookClient;
// import com.restfb.FacebookClient;
// import com.restfb.Version;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;

// import java.util.Collections;
// import java.util.Map;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/auth")
// public class FacebookAuthController {

//     private final UserService userService;
//     private static final Logger logger = LoggerFactory.getLogger(FacebookAuthController.class);

//     @Value("${facebook.appSecret}")
//     private String facebookAppSecret;

//     private final TokenProvider tokenProvider;

//     @Autowired
//     public FacebookAuthController(UserService userService, TokenProvider tokenProvider) {
//         this.userService = userService;
//         this.tokenProvider = tokenProvider;
//     }

//     @PostMapping("/facebook")
//     public ResponseEntity<?> authenticateFacebook(@RequestBody Map<String, String> accessTokenData) {
//         String accessToken = accessTokenData.get("accessToken");

//         try {
//             FacebookClient facebookClient = new DefaultFacebookClient(accessToken, facebookAppSecret, Version.LATEST);
//             com.restfb.types.User fbUser = facebookClient.fetchObject("me", com.restfb.types.User.class, 
//                     com.restfb.Parameter.with("fields", "id,name,email,picture"));

//             if (fbUser != null) {
//                 String facebookId = fbUser.getId();
//                 String email = fbUser.getEmail();
//                 String name = fbUser.getName();
//                 String pictureUrl = fbUser.getPicture().getUrl();

//                 User user;
//                 try {
//                     Optional<User> optionalUser = userService.getUserByFacebookId(facebookId);
//                     user = optionalUser.orElseGet(() -> {
//                         User newUser = new User();
//                         newUser.setFacebookId(facebookId);
//                         newUser.setEmail(email);
//                         newUser.setUsername(name);
//                         newUser.setProfilePictureUrl(pictureUrl);
//                         return userService.saveOrUpdateUser(newUser);
//                     });
//                 } catch (Exception e) {
//                     logger.error("Error fetching or saving user", e);
//                     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User processing error.");
//                 }

//                 if (!pictureUrl.equals(user.getProfilePictureUrl())) {
//                     try {
//                         user.setProfilePictureUrl(pictureUrl);
//                         userService.saveOrUpdateUser(user);
//                     } catch (Exception e) {
//                         logger.error("Error updating user profile picture", e);
//                         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Profile update error.");
//                     }
//                 }

//                 String jwtToken;
//                 try {
//                     jwtToken = tokenProvider.generateToken(user);
//                 } catch (Exception e) {
//                     logger.error("Token generation error", e);
//                     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token generation error.");
//                 }

//                 try {
//                     UsernamePasswordAuthenticationToken authentication =
//                         new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
//                     SecurityContextHolder.getContext().setAuthentication(authentication);
//                 } catch (Exception e) {
//                     logger.error("Security context setting error", e);
//                     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Security context error.");
//                 }

//                 return ResponseEntity.ok(Collections.singletonMap("token", jwtToken));
//             } else {
//                 logger.warn("Invalid Access Token received");
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access Token.");
//             }
//         } catch (Exception e) {
//             logger.error("Unexpected error during authentication process", e);
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error.");
//         }
//     }
// }