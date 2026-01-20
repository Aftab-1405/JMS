package com.abnalliance.journalapp.service;

import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private Users testUser;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        testUser = new Users();
        testUser.setUserName("testuser");
        testUser.setPassword("plainpassword");
        testUser.setRoles(Arrays.asList("USER"));
    }

    // Test saving new user with password encoding
    @Test
    void saveOrUpdateUser_WithNewUser_ShouldEncodePasswordAndSetRoles() {
        // Arrange
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        userService.saveOrUpdateUser(testUser);

        // Assert
        verify(userRepository, times(1)).save(testUser);
        assertNotEquals("plainpassword", testUser.getPassword());
        assertTrue(passwordEncoder.matches("plainpassword", testUser.getPassword()));
        assertEquals(Arrays.asList("USER"), testUser.getRoles());
    }

    // Test saving existing user without password re-encoding
    @Test
    void saveUser_WithExistingUser_ShouldNotEncodePassword() {
        // Arrange
        String originalEncodedPassword = passwordEncoder.encode("plainpassword");
        testUser.setPassword(originalEncodedPassword);
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        userService.saveUser(testUser);

        // Assert
        verify(userRepository, times(1)).save(testUser);
        assertEquals(originalEncodedPassword, testUser.getPassword());
    }

    // Test getting all users
    @Test
    void getAllUsers_ShouldReturnUserList() {
        // Arrange
        Users user2 = new Users();
        user2.setUserName("testuser2");
        List<Users> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<Users> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // Test getting all users when no users exist
    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Users> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test getting specific user by username
    @Test
    void getSpecificUserByUsername_WithValidUsername_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(testUser);

        // Act
        Users result = userService.getSpecificUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
    }

    // Test getting non-existent user by username
    @Test
    void getSpecificUserByUsername_WithNonExistentUsername_ShouldReturnNull() {
        // Arrange
        when(userRepository.findByUserName("nonexistent")).thenReturn(null);

        // Act
        Users result = userService.getSpecificUserByUsername("nonexistent");

        // Assert
        assertNull(result);
    }

    // Test getting user with null or empty username
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void getSpecificUserByUsername_WithInvalidUsername_ShouldReturnNull(String username) {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act
        Users result = userService.getSpecificUserByUsername(username);

        // Assert
        assertNull(result);
    }

    // Test deleting existing user
    @Test
    void deleteSpecificUserByUsername_WithValidUsername_ShouldDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteByUserName("testuser");

        // Act
        userService.deleteSpecificUserByUsername("testuser");

        // Assert
        verify(userRepository, times(1)).deleteByUserName("testuser");
    }

    // Test deleting non-existent user
    @Test
    void deleteSpecificUserByUsername_WithNonExistentUsername_ShouldNotThrowException() {
        // Arrange
        doNothing().when(userRepository).deleteByUserName("nonexistent");

        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            userService.deleteSpecificUserByUsername("nonexistent");
        });

        verify(userRepository, times(1)).deleteByUserName("nonexistent");
    }

    // Test saving admin user with appropriate roles
    @Test
    void saveOrUpdateAdmin_ShouldEncodePasswordAndSetAdminRole() {
        // Arrange
        Users adminUser = new Users();
        adminUser.setUserName("admin");
        adminUser.setPassword("adminpassword");
        when(userRepository.save(any(Users.class))).thenReturn(adminUser);

        // Act
        userService.saveOrUpdateAdmin(adminUser);

        // Assert
        verify(userRepository, times(1)).save(adminUser);
        assertNotEquals("adminpassword", adminUser.getPassword());
        assertTrue(passwordEncoder.matches("adminpassword", adminUser.getPassword()));
        assertEquals(Arrays.asList("USER", "ADMIN"), adminUser.getRoles());
    }

    // Test exception handling in repository operations
    @Test
    void saveOrUpdateUser_WhenRepositoryThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        when(userRepository.save(any(Users.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.saveOrUpdateUser(testUser);
        });

        assertTrue(exception.getMessage().contains("Error saving user"));
    }
}