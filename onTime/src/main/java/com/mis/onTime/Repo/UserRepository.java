package com.mis.onTime.Repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Query;

import com.mis.onTime.Modal.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    List<User> findByEmail(String email);
    Optional<User> findByFacebookId(String facebookId); 

    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByUsername(String username);
}

