package com.abnalliance.journalapp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class Users {
    @Id // @Id annotation is used to specify PRIMARY KEY
    private ObjectId id;

    @Indexed(unique = true)
    // @Indexed annotation is used to apply indexing on the field.
    // This will not be done automatically, we have to set mongo db configuration in application.properties file.
    @NonNull // This annotation will ensure that userName can't be null.
    private String userName;

    @NonNull // This annotation will ensure that password can't be null.
    private String password;

    @DBRef // Here this annotation will establish reference (link) with JournalEntry.
    private List<JournalEntry> journalEntries = new ArrayList<>();

    private List<String> roles;
}
