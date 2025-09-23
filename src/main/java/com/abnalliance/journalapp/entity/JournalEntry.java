package com.abnalliance.journalapp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "journal_db") // Maps this class to MongoDB collection
@Data
@NoArgsConstructor
public class JournalEntry {

    @Id // Marks primary key field
    private ObjectId id;

    @NonNull // This annotation will ensure that title can't be null.
    private String title;

    private String content;
    private LocalDate date;

}
