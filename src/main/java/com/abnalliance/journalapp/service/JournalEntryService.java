package com.abnalliance.journalapp.service;

import com.abnalliance.journalapp.entity.JournalEntry;
import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j // For logging
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    /**
     * Creates new journal entry and links it to user
     * Uses saveUser() instead of saveOrUpdateUser() because:
     * - User already exists (authenticated)
     * - Password shouldn't be re-encoded
     * - Roles shouldn't be reset to default
     */
    @Transactional
    public void saveJournalEntry(JournalEntry journalEntry, String userName) {
        try {
            // Step 1: Set current date and save journal to journal collection
            journalEntry.setDate(LocalDate.now());
            journalEntryRepository.save(journalEntry);

            // Step 2: Link journal to user's document via DBRef
            Users specificUser = userService.getSpecificUserByUsername(userName);
            if (specificUser == null) {
                throw new RuntimeException("User not found: " + userName);
            }

            // Step 3: Add journal reference to user's journal list
            specificUser.getJournalEntries().add(journalEntry);

            // IMPORTANT: Using saveUser() here, NOT saveOrUpdateUser()
            // Reason: We're only updating journal references, not changing credentials
            // saveOrUpdateUser() would incorrectly re-encode the already-encoded password
            userService.saveUser(specificUser);

        } catch (Exception e) {
            log.error("Failed to save journal entry for user: {}", userName, e);
            throw new RuntimeException("Error saving journal entry: " + e.getMessage());
        }
    }

    /**
     * Updates existing journal entry without user linkage
     * Direct save since entry already linked to user
     */
    public void saveJournalEntry(JournalEntry journalEntry) {
        try {
            journalEntryRepository.save(journalEntry);
        } catch (Exception e) {
            log.error("Failed to update journal entry with id: {}", journalEntry.getId(), e);
            throw new RuntimeException("Error updating journal entry: " + e.getMessage());
        }
    }

    /**
     * Retrieves all journals for a specific user
     * Fetches from user document's journal reference list
     */
    public List<JournalEntry> getAllJournalEntries(String userName) {
        try {
            Users specificUser = userService.getSpecificUserByUsername(userName);
            if (specificUser == null) {
                throw new RuntimeException("User not found: " + userName);
            }
            // Return journals linked via DBRef in user document
            return specificUser.getJournalEntries();
        } catch (Exception e) {
            log.error("Failed to fetch journal entries for user: {}", userName, e);
            throw new RuntimeException("Error fetching journal entries: " + e.getMessage());
        }
    }

    /**
     * Fetches single journal by ID from journal collection
     */
    public Optional<JournalEntry> getSpecificEntryById(ObjectId id) {
        try {
            return journalEntryRepository.findById(id);
        } catch (Exception e) {
            log.error("Failed to fetch journal entry with id: {}", id, e);
            throw new RuntimeException("Error fetching journal entry: " + e.getMessage());
        }
    }

    /**
     * Deletes journal from both collections (maintains referential integrity)
     * Uses saveUser() to preserve user's existing encoded password
     */
    @Transactional
    public void deleteSpecificEntryById(ObjectId id, String userName) {
        try {
            Users specificUser = userService.getSpecificUserByUsername(userName);
            if (specificUser == null) {
                throw new RuntimeException("User not found: " + userName);
            }

            // Step 1: Remove journal reference from user's list
            boolean deleted = specificUser.getJournalEntries().removeIf(x -> x.getId().equals(id));

            if (deleted) {
                // Step 2: Update user document (using saveUser() to avoid password re-encoding)
                userService.saveUser(specificUser);

                // Step 3: Delete actual journal from journal collection
                journalEntryRepository.deleteById(id);
            } else {
                throw new RuntimeException("Journal entry not found in user's entries");
            }
        } catch (Exception e) {
            log.error("Failed to delete journal entry with id: {} for user: {}", id, userName, e);
            throw new RuntimeException("Error deleting journal entry: " + e.getMessage());
        }
    }
}