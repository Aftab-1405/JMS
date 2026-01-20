package com.abnalliance.journalapp.service;

import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private static final PasswordEncoder passwordEncode = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    /**
     * FOR NEW USER REGISTRATION OR PASSWORD UPDATES
     * - Encodes raw password to BCrypt hash
     * - Sets default USER role
     * - Used in: PublicController (registration), UserController (password change)
     */
    public void saveOrUpdateUser(Users user) {
        try {
            // Always encode password when using this method
            user.setPassword(passwordEncode.encode(user.getPassword()));
            // Set default role for new users
            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Failed to save/update user: {}", user.getUserName(), e);
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
    }

    /**
     * FOR EXISTING USER UPDATES (NON-CREDENTIAL)
     * - Does NOT encode password (already encoded)
     * - Does NOT reset roles
     * - Used in: JournalEntryService (adding/removing journal references)
     * Critical: Use this when updating user's journal list to avoid double-encoding password
     */
    public void saveUser(Users user) {
        try {
            // Direct save without password manipulation
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Failed to save user: {}", user.getUserName(), e);
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
    }

    /**
     * Retrieves all users from database
     */
    public List<Users> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Fetches user by username (used for authentication and authorization)
     */
    public Users getSpecificUserByUsername(String userName) {
        try {
            Users user = userRepository.findByUserName(userName);
            if (user == null) {
                log.warn("User not found: {}", userName);
            }
            return user;
        } catch (Exception e) {
            log.error("Failed to fetch user: {}", userName, e);
            throw new RuntimeException("Error fetching user: " + e.getMessage());
        }
    }

    /**
     * Deletes user and cascades to remove all their journals
     */
    public void deleteSpecificUserByUsername(String userName) {
        try {
            userRepository.deleteByUserName(userName);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", userName, e);
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public void saveOrUpdateAdmin(Users user) {
        try {
            // Always encode password when using this method
            user.setPassword(passwordEncode.encode(user.getPassword()));
            // Set default role for new admin users
            user.setRoles(Arrays.asList("USER", "ADMIN"));
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Failed to save/update user: {}", user.getUserName(), e);
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
    }
}