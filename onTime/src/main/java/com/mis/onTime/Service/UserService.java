package com.mis.onTime.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mis.onTime.Modal.User;
import com.mis.onTime.Repo.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder; 

     @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Optional<User> getUserByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.size() == 1) {
            return Optional.of(users.get(0));
        } else if (users.isEmpty()) {
            return Optional.empty();
        } else {
            logger.error("Multiple users found with the same email: {}", email);
            return Optional.of(users.get(0));
        }
    }
    
    public User saveOrUpdateUser(User user) {
        Optional<User> existingUserOptional = getUserByEmail(user.getEmail());
        
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            
            existingUser.setGoogleId(user.getGoogleId());
            existingUser.setUsername(user.getUsername());
            existingUser.setProfilePictureUrl(user.getProfilePictureUrl());
            existingUser.setFacebookId(user.getFacebookId());
            existingUser.setEnabled(user.isEnabled());
    
            logger.info("Updating user with email: {}", user.getEmail());
            return userRepository.save(existingUser);
        } else {
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                logger.info("No password provided; handling OAuth user creation.");
            }
    
            logger.info("Creating new user with email: {}", user.getEmail());
            return userRepository.save(user);
        }
    }
    
    
    public Optional<User> getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> getUserByFacebookId(String facebookId) {
        return userRepository.findByFacebookId(facebookId);
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByNameOptional(String name) {
        return userRepository.findByUsername(name);
    }
    
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}