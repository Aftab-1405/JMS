package com.abnalliance.journalapp.service;

import com.abnalliance.journalapp.entity.JournalEntry;
import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private JournalEntry testEntry;
    private Users testUser;
    private ObjectId testId;

    @BeforeEach
    void setUp() {
        testId = new ObjectId();
        testEntry = new JournalEntry();
        testEntry.setId(testId);
        testEntry.setTitle("Test Title");
        testEntry.setContent("Test Content");

        testUser = new Users();
        testUser.setUserName("testuser");
        testUser.setPassword("password");
        testUser.setJournalEntries(Arrays.asList(testEntry));
    }

    // Test saving journal entry with valid user
    @Test
    void saveJournalEntry_WithValidUser_ShouldSaveEntryAndUpdateUser() {
        // Arrange
        when(userService.getSpecificUserByUsername("testuser")).thenReturn(testUser);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(testEntry);

        // Act
        journalEntryService.saveJournalEntry(testEntry, "testuser");

        // Assert
        verify(journalEntryRepository, times(1)).save(testEntry);
        verify(userService, times(1)).saveUser(testUser);
        assertEquals(LocalDate.now(), testEntry.getDate());
        assertTrue(testUser.getJournalEntries().contains(testEntry));
    }

    // Test saving journal entry with non-existent user
    @Test
    void saveJournalEntry_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userService.getSpecificUserByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.saveJournalEntry(testEntry, "nonexistent");
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(journalEntryRepository, never()).save(any());
        verify(userService, never()).saveUser(any());
    }

    // Test saving journal entry with null or empty username
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void saveJournalEntry_WithInvalidUsername_ShouldThrowException(String username) {
        // Arrange
        when(userService.getSpecificUserByUsername(username)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            journalEntryService.saveJournalEntry(testEntry, username);
        });
    }

    // Test updating existing journal entry
    @Test
    void saveJournalEntry_UpdateExistingEntry_ShouldSaveSuccessfully() {
        // Arrange
        when(journalEntryRepository.save(testEntry)).thenReturn(testEntry);

        // Act
        journalEntryService.saveJournalEntry(testEntry);

        // Assert
        verify(journalEntryRepository, times(1)).save(testEntry);
    }

    // Test getting all journal entries for a user
    @Test
    void getAllJournalEntries_WithValidUser_ShouldReturnEntries() {
        // Arrange
        when(userService.getSpecificUserByUsername("testuser")).thenReturn(testUser);

        // Act
        List<JournalEntry> entries = journalEntryService.getAllJournalEntries("testuser");

        // Assert
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(testEntry, entries.get(0));
    }

    // Test getting all journal entries for non-existent user
    @Test
    void getAllJournalEntries_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userService.getSpecificUserByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.getAllJournalEntries("nonexistent");
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
    }

    // Test getting specific journal entry by ID
    @Test
    void getSpecificEntryById_WithValidId_ShouldReturnEntry() {
        // Arrange
        when(journalEntryRepository.findById(testId)).thenReturn(Optional.of(testEntry));

        // Act
        Optional<JournalEntry> result = journalEntryService.getSpecificEntryById(testId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEntry, result.get());
    }

    // Test getting non-existent journal entry by ID
    @Test
    void getSpecificEntryById_WithNonExistentId_ShouldReturnEmpty() {
        // Arrange
        ObjectId nonExistentId = new ObjectId();
        when(journalEntryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<JournalEntry> result = journalEntryService.getSpecificEntryById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
    }

    // Test deleting existing journal entry
    @Test
    void deleteSpecificEntryById_WithValidIdAndUser_ShouldDeleteEntry() {
        // Arrange
        when(userService.getSpecificUserByUsername("testuser")).thenReturn(testUser);

        // Act
        journalEntryService.deleteSpecificEntryById(testId, "testuser");

        // Assert
        verify(journalEntryRepository, times(1)).deleteById(testId);
        verify(userService, times(1)).saveUser(testUser);
        assertFalse(testUser.getJournalEntries().contains(testEntry));
    }

    // Test deleting non-existent journal entry from user's list
    @Test
    void deleteSpecificEntryById_WithNonExistentEntryForUser_ShouldThrowException() {
        // Arrange
        ObjectId nonExistentId = new ObjectId();
        when(userService.getSpecificUserByUsername("testuser")).thenReturn(testUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.deleteSpecificEntryById(nonExistentId, "testuser");
        });

        assertEquals("Journal entry not found in user's entries", exception.getMessage());
        verify(journalEntryRepository, never()).deleteById(any());
        verify(userService, never()).saveUser(any());
    }

    // Test deleting journal entry for non-existent user
    @Test
    void deleteSpecificEntryById_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userService.getSpecificUserByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.deleteSpecificEntryById(testId, "nonexistent");
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(journalEntryRepository, never()).deleteById(any());
        verify(userService, never()).saveUser(any());
    }
}