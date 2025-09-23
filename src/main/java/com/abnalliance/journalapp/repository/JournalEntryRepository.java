package com.abnalliance.journalapp.repository;

import com.abnalliance.journalapp.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

// Repository interface for JournalEntry collection (CRUD ready via MongoRepository)
public interface JournalEntryRepository extends MongoRepository<JournalEntry, ObjectId> {
}
