package com.abnalliance.journalapp.controller;

import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user") // Protected endpoint - requires authentication
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping
    public ResponseEntity<?> updateSpecificUser(@RequestBody Users user) {
        // Extract authenticated user from Spring Security context
        // SecurityContextHolder maintains auth info in ThreadLocal for current request
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // Fetch existing user to preserve journal references
        Users userInDb = userService.getSpecificUserByUsername(userName);

        // Update credentials only
        userInDb.setUserName(user.getUserName());
        userInDb.setPassword(user.getPassword()); // Raw password will be encoded

        // IMPORTANT: Using saveOrUpdateUser() here because:
        // - Password needs to be encoded (user provided raw password)
        // - This is a credential update operation
        userService.saveOrUpdateUser(userInDb);
        return new ResponseEntity<>(userInDb, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSpecificUser(@RequestBody Users user) {
        // Get authenticated user for self-deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // User can only delete their own account
        userService.deleteSpecificUserByUsername(userName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}