package com.abnalliance.journalapp.repository;

import com.abnalliance.journalapp.entity.Users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

// Repository interface for Users collection (CRUD ready via MongoRepository)
public interface UserRepository extends MongoRepository<Users, ObjectId> {
    public abstract Users findByUserName(String userName);
    public abstract Users deleteByUserName(String userName);
}
