package com.abnalliance.journalapp.controller;

import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public") // Open endpoint - no authentication required
public class PublicController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody Users user) {
        // NEW USER REGISTRATION FLOW:
        // Using saveOrUpdateUser() because:
        // 1. Raw password needs BCrypt encoding
        // 2. Default "USER" role needs to be assigned
        // 3. This is initial user creation, not an update
        userService.saveOrUpdateUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}