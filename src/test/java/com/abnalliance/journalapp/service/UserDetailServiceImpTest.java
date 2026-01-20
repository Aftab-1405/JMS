package com.abnalliance.journalapp.service;

import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceImp userDetailService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUserName("testuser");
        testUser.setPassword("encodedpassword");
        testUser.setRoles(Arrays.asList("USER"));
    }

    // Test loading user by valid username
    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(testUser);

        // Act
        UserDetails userDetails = userDetailService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    // Test loading user with admin roles
    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnUserDetailsWithAdminRole() {
        // Arrange
        testUser.setRoles(Arrays.asList("USER", "ADMIN"));
        when(userRepository.findByUserName("admin")).thenReturn(testUser);

        // Act
        UserDetails userDetails = userDetailService.loadUserByUsername("admin");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // Test loading non-existent user
    @Test
    void loadUserByUsername_WithNonExistentUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUserName("nonexistent")).thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailService.loadUserByUsername("nonexistent");
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
    }

    // Test loading user with null or empty username
    @ParameterizedTest
    @NullAndEmptySource
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException(String username) {
        // Arrange
        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailService.loadUserByUsername(username);
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    // Test loading user with null roles
    @Test
    void loadUserByUsername_WithUserHavingNullRoles_ShouldReturnUserWithoutAuthorities() {
        // Arrange
        testUser.setRoles(null);
        when(userRepository.findByUserName("testuser")).thenReturn(testUser);

        // Act
        UserDetails userDetails = userDetailService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    // Test loading user with empty roles
    @Test
    void loadUserByUsername_WithUserHavingEmptyRoles_ShouldReturnUserWithoutAuthorities() {
        // Arrange
        testUser.setRoles(Arrays.asList());
        when(userRepository.findByUserName("testuser")).thenReturn(testUser);

        // Act
        UserDetails userDetails = userDetailService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}