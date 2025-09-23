package com.abnalliance.journalapp.controller;

import com.abnalliance.journalapp.entity.JournalEntry;
import com.abnalliance.journalapp.entity.Users;
import com.abnalliance.journalapp.service.JournalEntryService;
import com.abnalliance.journalapp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal") // Protected endpoint - requires authentication
public class JournalEntryController {
    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry journalEntry) {
        // Extract currently logged-in user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // Save journal entry and link it to the authenticated user
        journalEntryService.saveJournalEntry(journalEntry, userName);
        return new ResponseEntity<>(journalEntry, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<?>> getAllJournalEntriesOfUser() {
        // Fetch entries only for the authenticated user (security measure)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // Retrieve user-specific journal entries from their document reference
        List<JournalEntry> entries = journalEntryService.getAllJournalEntries(userName);
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @GetMapping("id/{myId}")
    public ResponseEntity<?> getSpecificJournal(@PathVariable ObjectId myId) {
        // Verify user owns this journal entry before returning it
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        Users user = userService.getSpecificUserByUsername(userName);

        // Security check: Ensure requested journal belongs to authenticated user
        List<JournalEntry> associatedEntry = user.getJournalEntries().stream()
                .filter(x -> x.getId().equals(myId))
                .collect(Collectors.toList());

        if (!associatedEntry.isEmpty()) {
            // User owns this entry, fetch full details from journal collection
            Optional<JournalEntry> specificEntryById = journalEntryService.getSpecificEntryById(myId);
            if (specificEntryById.isPresent()) {
                return new ResponseEntity<>(specificEntryById.get(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{myId}")
    public ResponseEntity<?> deleteSpecificJournal(@PathVariable ObjectId myId) {
        // Get authenticated user for ownership verification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        // Check if journal exists before attempting deletion
        Optional<JournalEntry> entry = journalEntryService.getSpecificEntryById(myId);
        if (entry.isPresent()) {
            // Delete from both journal collection and user's reference list
            journalEntryService.deleteSpecificEntryById(myId, userName);
            return new ResponseEntity<>("The record with id :" + myId + " has been successfully deleted.", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{myId}")
    public ResponseEntity<?> updateSpecificJournal(@PathVariable ObjectId myId, @RequestBody JournalEntry journalEntry) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        Users user = userService.getSpecificUserByUsername(userName);

        // Check ownership
        boolean userOwnsEntry = user.getJournalEntries().stream()
                .anyMatch(entry -> entry.getId().equals(myId));

        if (!userOwnsEntry) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Proceed with update if ownership confirmed
        Optional<JournalEntry> oldEntry = journalEntryService.getSpecificEntryById(myId);
        if (oldEntry.isPresent()) {
            JournalEntry old = oldEntry.get();
            old.setTitle(journalEntry.getTitle() != null && !journalEntry.getTitle().isEmpty()
                    ? journalEntry.getTitle() : old.getTitle());
            old.setContent(journalEntry.getContent() != null && !journalEntry.getContent().isEmpty()
                    ? journalEntry.getContent() : old.getContent());

            journalEntryService.saveJournalEntry(old);
            return new ResponseEntity<>(old, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}