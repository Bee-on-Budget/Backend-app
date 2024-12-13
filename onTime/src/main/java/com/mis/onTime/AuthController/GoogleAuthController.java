package com.mis.onTime.AuthController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mis.onTime.Config.TokenProvider;
import com.mis.onTime.Modal.User;
import com.mis.onTime.Service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);
    private final NetHttpTransport transport = new NetHttpTransport();
    
    @Value("${google.clientId}")
    private String clientId;
    private final TokenProvider tokenProvider;

    @Autowired
    public GoogleAuthController(UserService userService, TokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }
    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody Map<String, String> idTokenData) {
        String idTokenString = idTokenData.get("idToken");
    
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                .setAudience(Collections.singletonList(clientId))
                .setAcceptableTimeSkewSeconds(60)
                .build();
    
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload googlePayload = idToken.getPayload();
                String googleId = googlePayload.getSubject();
                String email = googlePayload.getEmail();
                String name = (String) googlePayload.get("name");
                String pictureUrl = (String) googlePayload.get("picture");
    
                Optional<User> optionalUser = userService.getUserByGoogleId(googleId);
                if (optionalUser.isEmpty()) {
                    optionalUser = userService.getUserByEmail(email);
                }
    
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                    logger.info("Updating existing user with email: {}", email);
                } else {
                    user = new User();
                    user.setEmail(email);
                    logger.info("Creating new user with email: {}", email);
                }
    
                user.setGoogleId(googleId);
                user.setUsername(name);
                user.setProfilePictureUrl(pictureUrl);
    
                try {
                    userService.saveOrUpdateUser(user);
                } catch (Exception e) {
                    logger.error("Error saving user: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving user.");
                }
    
                String jwtToken = tokenProvider.generateToken(user);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
    
                return ResponseEntity.ok(Collections.singletonMap("token", jwtToken));
            } else {
                logger.warn("Invalid ID token received");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error during authentication process: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error.");
        }
    }
}